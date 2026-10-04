/*
 * Copyright (C) 2012-2025 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.money.manager.ex.notifications;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.domainmodel.Currency;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.settings.BehaviourSettings;
import com.money.manager.ex.utils.MmxDate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

/**
 * Records a parsed Samsung Card SMS into the account the user linked to that card.
 */
public class SamsungCardSmsProcessor {

    public static final String EXTRA_ISSUER = "CardSms:issuer";
    public static final String EXTRA_LAST4 = "CardSms:last4";
    public static final String EXTRA_SMS_BODY = "CardSms:smsBody";
    public static final String EXTRA_SMS_SENDER = "CardSms:smsSender";

    private final Context mContext;

    public SamsungCardSmsProcessor(Context context) {
        mContext = context.getApplicationContext();
    }

    public void process(SamsungCardSmsParser.Result card, String rawBody, String sender) {
        try {
            final Long accountId = new CardSmsLinkStore(mContext).getAccountId(card.issuer, card.cardLast4);
            if (accountId == null) {
                notifyNotLinked(card, rawBody, sender);
                return;
            }
            record(card, accountId, rawBody, sender);
        } catch (Exception e) {
            Timber.e(e, "processing card sms");
        }
    }

    private void record(SamsungCardSmsParser.Result card, long accountId, String rawBody, String sender) {
        BehaviourSettings settings = new BehaviourSettings(mContext);
        boolean useNotification = settings.getSmsTransStatusNotification();

        Date date = SamsungCardSmsParser.resolveDate(card, new Date());
        String refBase = "SS" + card.cardLast4 + (card.cancel ? "C" : "A")
                + new SimpleDateFormat("yyyyMMddHHmm", Locale.US).format(date);
        int months = card.installmentMonths;
        String firstRef = months > 1 ? refBase + "-1/" + months : refBase;

        if (transactionExists(accountId, firstRef)) {
            String text = mContext.getString(R.string.card_sms_already_exists, firstRef);
            if (useNotification) {
                Intent openApp = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
                SmsTransactionHelper.showNotification(mContext, openApp != null ? openApp : new Intent(),
                        text, sender, "Already Exists", "");
            } else {
                Toast.makeText(mContext, "MMEX: " + text, Toast.LENGTH_LONG).show();
            }
            return;
        }

        TransactionTypes type = card.cancel ? TransactionTypes.Deposit : TransactionTypes.Withdrawal;
        Payee payee = findOrCreatePayee(card.merchant);
        long categoryId = payee != null && payee.hasCategory() ? payee.getCategoryId() : Constants.NOT_SET;

        // Convert the card's currency into the account's currency.
        CurrencyConverter converter = new CurrencyConverter(accountId, card.currencyCode);

        List<AccountTransaction> txns = new ArrayList<>();
        List<BigDecimal> parts = SamsungCardSmsParser.splitInstallments(card.amount, months, card.amount.scale());
        for (int i = 0; i < parts.size(); i++) {
            String notes = rawBody;
            String ref = refBase;
            if (months > 1) {
                notes = mContext.getString(R.string.card_sms_installment_note, i + 1, months) + "\n" + rawBody;
                ref = refBase + "-" + (i + 1) + "/" + months;
            }
            txns.add(createTransaction(accountId, type, payee, categoryId,
                    converter.convert(parts.get(i)),
                    new MmxDate(date).plusMonths(i).toDate(), notes, ref));
        }

        AccountTransaction first = txns.get(0);
        String validation = converter.isValid()
                ? SmsTransactionHelper.validate(mContext, first)
                : mContext.getString(R.string.card_sms_currency_missing, card.currencyCode);

        if (!SmsTransactionHelper.PASS.equals(validation)) {
            // Don't create a partial set of installments: hand the full amount to the user to edit.
            AccountTransaction single = first;
            if (months > 1) {
                single = createTransaction(accountId, type, payee, categoryId,
                        converter.convert(card.amount), date,
                        mContext.getString(R.string.card_sms_installment_months_note, months) + "\n" + rawBody,
                        refBase);
            }
            Intent intent = SmsTransactionHelper.buildEditIntent(mContext, single, payeeName(payee));
            intent.setAction(Intent.ACTION_INSERT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            reportFailure(useNotification, intent, rawBody, sender, "Auto Failed", validation);
            return;
        }

        for (AccountTransaction txn : txns) {
            String saveStatus = SmsTransactionHelper.save(mContext, txn);
            if (!SmsTransactionHelper.PASS.equals(saveStatus)) {
                Intent intent = SmsTransactionHelper.buildEditIntent(mContext, txn, payeeName(payee));
                intent.setAction(Intent.ACTION_INSERT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                reportFailure(useNotification, intent, rawBody, sender, "Save Failed", saveStatus);
                return;
            }
        }

        Intent intent = SmsTransactionHelper.buildEditIntent(mContext, first, payeeName(payee));
        intent.setAction(Intent.ACTION_EDIT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (useNotification) {
            SmsTransactionHelper.showNotification(mContext, intent, rawBody, sender, "Successful", "");
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.card_sms_recorded,
                    card.merchant, first.getAmount().toString(), txns.size()), Toast.LENGTH_LONG).show();
        }
    }

    private void reportFailure(boolean useNotification, Intent intent, String rawBody, String sender,
                               String status, String error) {
        if (useNotification) {
            SmsTransactionHelper.showNotification(mContext, intent, rawBody, sender, status, " - " + error);
        } else {
            mContext.startActivity(intent);
            Toast.makeText(mContext, "MMEX " + status + " : " + error, Toast.LENGTH_LONG).show();
        }
    }

    private void notifyNotLinked(SamsungCardSmsParser.Result card, String rawBody, String sender) {
        Intent intent = new Intent(mContext, CardSmsLinkActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_ISSUER, card.issuer);
        intent.putExtra(EXTRA_LAST4, card.cardLast4);
        intent.putExtra(EXTRA_SMS_BODY, rawBody);
        intent.putExtra(EXTRA_SMS_SENDER, sender);

        SmsTransactionHelper.showCustomNotification(mContext, intent,
                mContext.getString(R.string.card_sms_not_linked_title, card.cardLast4),
                mContext.getString(R.string.card_sms_not_linked_subtext),
                rawBody,
                "Not Linked");
    }

    private AccountTransaction createTransaction(long accountId, TransactionTypes type, Payee payee,
                                                 long categoryId, Money amount, Date date,
                                                 String notes, String ref) {
        AccountTransaction txn = AccountTransaction.create();
        txn.setAccountId(accountId);
        txn.setToAccountId(Constants.NOT_SET);
        txn.setTransactionType(type);
        txn.setPayeeId(payee != null ? payee.getId() : Constants.NOT_SET);
        txn.setCategoryId(categoryId);
        txn.setAmount(amount);
        txn.setToAmount(amount);
        txn.setStatus("");
        txn.setDate(date);
        txn.setNotes(notes);
        txn.setTransactionNumber(ref);
        return txn;
    }

    private boolean transactionExists(long accountId, String ref) {
        long count = new AccountTransactionRepository(mContext).count(
                AccountTransaction.TRANSACTIONNUMBER + "=? AND " + AccountTransaction.ACCOUNTID + "=?"
                        + " AND (DELETEDTIME IS NULL OR DELETEDTIME = '')",
                new String[]{ref, String.valueOf(accountId)});
        return count > 0;
    }

    private Payee findOrCreatePayee(String name) {
        PayeeRepository repo = new PayeeRepository(mContext);
        Payee payee = repo.loadByName(name);
        if (payee != null) return payee;

        payee = new Payee();
        payee.setName(name);
        long id = repo.add(payee);
        if (id <= 0) return null;

        payee.setId(id);
        return payee;
    }

    private static String payeeName(Payee payee) {
        return payee != null ? payee.getName() : "";
    }

    /**
     * Converts amounts from the SMS currency to the account currency, rounded to its decimals.
     */
    private class CurrencyConverter {
        private final Long fromCurrencyId;
        private final long toCurrencyId;
        private final int decimals;
        private final CurrencyService service = new CurrencyService(mContext);

        CurrencyConverter(long accountId, String fromCode) {
            toCurrencyId = new AccountRepository(mContext).loadCurrencyIdFor(accountId);
            fromCurrencyId = findCurrencyId(fromCode);

            Currency toCurrency = service.getCurrency(toCurrencyId);
            Integer scale = toCurrency != null ? toCurrency.getScale() : null;
            decimals = scale != null && scale > 0
                    ? (int) Math.round(Math.log10(scale))
                    : 2;
        }

        boolean isValid() {
            return fromCurrencyId != null;
        }

        Money convert(BigDecimal amount) {
            Money value = MoneyFactory.fromBigDecimal(amount);
            if (fromCurrencyId == null || fromCurrencyId == toCurrencyId) return value;

            Money converted = service.doCurrencyExchange(toCurrencyId, value, fromCurrencyId);
            return MoneyFactory.fromBigDecimal(
                    new BigDecimal(converted.toString()).setScale(decimals, RoundingMode.HALF_UP));
        }

        private Long findCurrencyId(String code) {
            try {
                Long id = service.getIdForCode(code);
                return id != null && id != Constants.NOT_SET ? id : null;
            } catch (Exception e) {
                // currency not defined in this database
                return null;
            }
        }
    }
}
