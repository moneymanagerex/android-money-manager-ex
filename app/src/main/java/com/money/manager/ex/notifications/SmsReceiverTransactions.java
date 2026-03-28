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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.*;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.sqlite.db.SupportSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQueryBuilder;

import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.settings.BehaviourSettings;
import com.money.manager.ex.settings.GeneralSettings;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.money.manager.ex.transactions.EditTransactionActivityConstants;
import com.money.manager.ex.transactions.EditTransactionCommonFunctions;
import com.money.manager.ex.utils.MmxDate;
import com.squareup.sqlbrite3.BriteDatabase;

import javax.inject.Inject;

import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

public class SmsReceiverTransactions extends BroadcastReceiver {

    private Context mContext;

    @Inject
    BriteDatabase database;

    private MmxOpenHelper mOpenHelper;

    private EditTransactionCommonFunctions mCommon;

    private String[] fromAccountDetails;
    private String[] toAccountDetails;

    public static String CHANNEL_ID = "SmsTransaction_NotificationChannel";

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context.getApplicationContext();

        // Initialize injection
        if (mContext instanceof MmexApplication) {
            ((MmexApplication) mContext).iocComponent.inject(this);
        }

        final BehaviourSettings behavSettings = new BehaviourSettings(mContext);
        if (!behavSettings.getBankSmsTrans()) return;

        try {
            Bundle bundle = intent.getExtras();
            if (bundle == null) return;

            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus == null) return;

            String format = bundle.getString("format");
            StringBuilder msgBodyBuilder = new StringBuilder();
            String msgSender = "";

            for (Object pdu : pdus) {
                SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdu, format);
                msgSender = msg.getOriginatingAddress();
                msgBodyBuilder.append(msg.getMessageBody());
            }
            String msgBody = msgBodyBuilder.toString();

            if (isTransactionSms(msgSender) && !msgBody.toLowerCase().contains("otp")) {
                processSms(context, msgSender, msgBody);
            }
        } catch (Exception e) {
            Timber.e(e, "MMEX: Bank Transaction Process EXCEPTION");
        }
    }

    private void processSms(Context context, String msgSender, String msgBody) {
        final BehaviourSettings behavSettings = new BehaviourSettings(mContext);
        final GeneralSettings genSettings = new GeneralSettings(mContext);
        final AppSettings appSettings = new AppSettings(mContext);

        mOpenHelper = new MmxOpenHelper(mContext, appSettings.getDatabaseSettings().getDatabasePath());
        boolean smsTxnStatusNotificationSetting = behavSettings.getSmsTransStatusNotification();

        ITransactionEntity model = AccountTransaction.create();
        mCommon = new EditTransactionCommonFunctions(null, model, database);

        // Clean up the message body
        msgBody = msgBody.replaceAll("[\\t\\n\\r]+", " ").replaceAll(" +", " ");

        String transType = determineTransactionType(msgBody);
        if (TextUtils.isEmpty(transType)) return;

        // Common transaction properties
        mCommon.transactionEntity.setStatus("");
        mCommon.payeeName = "";
        mCommon.transactionEntity.setNotes(msgBody);
        mCommon.transactionEntity.setDate(new MmxDate().toDate());

        // Account and currency details
        long baseCurrencyId = genSettings.getBaseCurrencyId();
        long fromAccountId = genSettings.getDefaultAccountId() > 0 ? genSettings.getDefaultAccountId() : -1;
        long fromCurrencyId = baseCurrencyId;
        String fromAccCurrencySymbl = getCurrencySymbl(baseCurrencyId);
        String fromAccountName = "";

        extractAccountDetails(msgBody, transType);

        if (!fromAccountDetails[0].isEmpty()) {
            fromAccountId = Long.parseLong(fromAccountDetails[0]);
            fromAccountName = fromAccountDetails[1];
            fromCurrencyId = Long.parseLong(fromAccountDetails[2]);
            fromAccCurrencySymbl = fromAccountDetails[3];
            mCommon.transactionEntity.setAccountId(fromAccountId);
        }

        String transAmount = extractTransAmount(msgBody, fromAccCurrencySymbl);
        String[] transPayee = extractTransPayee(msgBody);

        if ((!fromAccountDetails[6].isEmpty() || !toAccountDetails[6].isEmpty() || !transPayee[0].isEmpty()) && !transAmount.isEmpty()) {
            mCommon.transactionEntity.setAmount(MoneyFactory.fromString(transAmount));
            String transRefNo = extractTransRefNo(msgBody);

            if (transRefNo.isEmpty()) {
                transRefNo = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
            }
            mCommon.transactionEntity.setTransactionNumber(transRefNo);

            long txnId = getTxnId(transRefNo.trim(), mCommon.transactionEntity.getDateString());
            Intent tIntent = new Intent(mContext, CheckingTransactionEditActivity.class);

            if (txnId == 0) {
                handleNewTransaction(transType, transAmount, fromCurrencyId, transPayee, tIntent);
            } else {
                handleExistingTransaction(txnId, fromAccountId, fromCurrencyId, msgBody, tIntent);
            }

            String strExtracted = "Account = " + fromAccountName + "-" + fromAccountDetails[6] + "\n"
                    + "Trans Amt = " + fromAccCurrencySymbl + " " + transAmount + ",\n"
                    + "Payyee Name= " + transPayee[1] + "\n"
                    + "Category ID = " + transPayee[2] + "\n"
                    + "Trans Ref No. = " + transRefNo + "\n"
                    + "Trans Type = " + transType + "\n";

            prepareIntentExtras(tIntent);

            if (!"EXISTS".equals(tIntent.getAction())) {
                tIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                String validationStatus = validateData();

                if ("PASS".equals(validationStatus)) {
                    String saveStatus = saveTransaction();
                    if ("PASS".equals(saveStatus)) {
                        if (smsTxnStatusNotificationSetting) {
                            tIntent.setAction(Intent.ACTION_EDIT);
                            tIntent.putExtra(EditTransactionActivityConstants.KEY_TRANS_ID, mCommon.transactionEntity.getId());
                            showNotification(tIntent, msgBody, msgSender, "Successful", "");
                        } else {
                            Toast.makeText(context, "MMEX: Bank Transaction Processed for: \n\n" + strExtracted, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        handleFailure(context, tIntent, msgBody, msgSender, "Save Failed", saveStatus, smsTxnStatusNotificationSetting);
                    }
                } else {
                    handleFailure(context, tIntent, msgBody, msgSender, "Auto Failed", validationStatus, smsTxnStatusNotificationSetting);
                }
            } else {
                if (smsTxnStatusNotificationSetting) {
                    showNotification(tIntent, "MMEX: Skipping Bank Transaction updates SMS, because transaction exists with ref. no. " + transRefNo, msgSender, "Already Exists", "");
                } else {
                    Toast.makeText(context, "MMEX: Skipping Bank Transaction updates SMS, because transaction exists with ref. no. " + transRefNo, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private String determineTransactionType(String msgBody) {
        String[] keyCreditSearch = {"(credited)", "(received)", "(added)", "(reloaded)", "(deposited)", "(refunded)",
                "(debited)(.*?)(towards)(\\s)", "(\\s)(received)(.*?)(in(\\s)your)(\\s)", "(sent)(.*?)(to)(\\s)", "(debited)(.*?)(to)(\\s)",
                "(credited)(.*?)(in)(\\s)", "(credited)(.*?)(to)(\\s)", "(recharge)"};

        String[] keyDebitSearch = {"(made)", "(debited)", "(using)", "(paid)", "(purchase)", "(withdrawn)", "(done)",
                "(credited)(.*?)(from)(\\s)", "(sent)(.*?)(from)(\\s)", "(\\s)(received)(.*?)(from)(\\s)",
                "(sales\\sdraft)", "(spent)"};

        boolean isDeposit = validateTransType(keyCreditSearch, msgBody.toLowerCase());
        boolean isWithdrawal = validateTransType(keyDebitSearch, msgBody.toLowerCase());

        if (isDeposit) {
            if (isWithdrawal) {
                setCategoryByName("Transfer");
                mCommon.transactionEntity.setTransactionType(TransactionTypes.Transfer);
                return "Transfer";
            } else {
                setCategoryByName("Income");
                mCommon.transactionEntity.setTransactionType(TransactionTypes.Deposit);
                return "Deposit";
            }
        } else if (isWithdrawal) {
            mCommon.transactionEntity.setTransactionType(TransactionTypes.Withdrawal);
            return "Withdrawal";
        }
        return "";
    }

    private void setCategoryByName(String categoryName) {
        String[] category = getCategoryOrSubCategoryByName(categoryName);
        if (!category[0].isEmpty()) {
            mCommon.transactionEntity.setCategoryId(Long.parseLong(category[0]));
        }
    }

    private void handleNewTransaction(String transType, String transAmount, long fromCurrencyId, String[] transPayee, Intent intent) {
        if ("Transfer".equals(transType)) {
            if (!toAccountDetails[0].isEmpty()) {
                long toAccountId = Long.parseLong(toAccountDetails[0]);
                long toCurrencyId = Long.parseLong(toAccountDetails[2]);
                mCommon.transactionEntity.setToAccountId(toAccountId);
                CurrencyService currencyService = new CurrencyService(mContext);
                mCommon.transactionEntity.setToAmount(currencyService.doCurrencyExchange(fromCurrencyId, mCommon.transactionEntity.getAmount(), toCurrencyId));
                mCommon.transactionEntity.setPayeeId(Constants.NOT_SET);
            } else if (!toAccountDetails[6].isEmpty() && transPayee[0].isEmpty()) {
                transPayee = getPayeeDetails(toAccountDetails[6].trim());
            }
        }

        if (!transPayee[0].isEmpty()) {
            mCommon.transactionEntity.setTransactionType(TransactionTypes.Withdrawal);
            mCommon.transactionEntity.setToAccountId(Constants.NOT_SET);
            mCommon.transactionEntity.setToAmount(MoneyFactory.fromString(transAmount));
            mCommon.transactionEntity.setPayeeId(Long.parseLong(transPayee[0]));
            mCommon.payeeName = transPayee[1];
            mCommon.transactionEntity.setCategoryId(Long.parseLong(transPayee[2]));
        }
        intent.setAction(Intent.ACTION_INSERT);
    }

    private void handleExistingTransaction(long txnId, long fromAccountId, long fromCurrencyId, String msgBody, Intent intent) {
        AccountTransactionRepository repo = new AccountTransactionRepository(mContext);
        AccountTransaction txn = repo.load(txnId);
        if (txn != null) {
            if (txn.getTransactionType() != TransactionTypes.Transfer) {
                AccountRepository accountRepository = new AccountRepository(mContext);
                long toCurrencyId;
                if (txn.getTransactionType() == TransactionTypes.Deposit) {
                    toCurrencyId = accountRepository.loadCurrencyIdFor(txn.getAccountId());
                } else {
                    toCurrencyId = fromCurrencyId;
                    fromCurrencyId = accountRepository.loadCurrencyIdFor(txn.getAccountId());
                }

                mCommon.transactionEntity = txn;
                mCommon.transactionEntity.setAccountId(fromAccountId);
                CurrencyService currencyService = new CurrencyService(mContext);
                mCommon.transactionEntity.setToAmount(currencyService.doCurrencyExchange(fromCurrencyId, mCommon.transactionEntity.getAmount(), toCurrencyId));
                mCommon.transactionEntity.setPayeeId(Constants.NOT_SET);

                setCategoryByName("Transfer");
                mCommon.transactionEntity.setNotes(mCommon.transactionEntity.getNotes() + "\n\n" + msgBody);
                intent.setAction(Intent.ACTION_EDIT);
            } else {
                mCommon.transactionEntity = txn;
                intent.setAction("EXISTS");
            }
        }
    }

    private void prepareIntentExtras(Intent intent) {
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_SOURCE, "SmsReceiverTransactions.java");
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_ID, mCommon.transactionEntity.getId());
        intent.putExtra(EditTransactionActivityConstants.KEY_ACCOUNT_ID, String.valueOf(mCommon.transactionEntity.getAccountId()));
        intent.putExtra(EditTransactionActivityConstants.KEY_TO_ACCOUNT_ID, String.valueOf(mCommon.transactionEntity.getToAccountId()));
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_CODE, mCommon.getTransactionType());
        intent.putExtra(EditTransactionActivityConstants.KEY_PAYEE_ID, String.valueOf(mCommon.transactionEntity.getPayeeId()));
        intent.putExtra(EditTransactionActivityConstants.KEY_PAYEE_NAME, mCommon.payeeName);
        intent.putExtra(EditTransactionActivityConstants.KEY_CATEGORY_ID, String.valueOf(mCommon.transactionEntity.getCategoryId()));
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_AMOUNT, String.valueOf(mCommon.transactionEntity.getAmount()));
        intent.putExtra(EditTransactionActivityConstants.KEY_NOTES, mCommon.transactionEntity.getNotes());
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_DATE, mCommon.transactionEntity.getDate());
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_NUMBER, mCommon.transactionEntity.getTransactionNumber());
    }

    private void handleFailure(Context context, Intent intent, String msgBody, String msgSender, String status, String detail, boolean notify) {
        if (notify) {
            showNotification(intent, msgBody, msgSender, status, " - " + detail);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Toast.makeText(context, "AMMEX " + status + " : " + detail, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("Range")
    private String getCurrencySymbl(long currencyId) {
        String currencySymbl = "";
        String[] reqCurrFields = {"CURRENCY_SYMBOL"};
        String selection = "CURRENCYID = ?";
        String[] selectionArgs = new String[]{String.valueOf(currencyId)};

        SupportSQLiteQuery query = SupportSQLiteQueryBuilder.builder("CURRENCYFORMATS_V1")
                .columns(reqCurrFields)
                .selection(selection, selectionArgs)
                .create();
        try (Cursor cursor = mOpenHelper.getReadableDatabase().query(query)) {
            if (cursor != null && cursor.moveToFirst()) {
                currencySymbl = cursor.getString(cursor.getColumnIndexOrThrow("CURRENCY_SYMBOL"));
            }
        } catch (Exception e) {
            Timber.e(e, "getCurrencySymbl");
        }
        return currencySymbl;
    }

    private boolean isTransactionSms(String msgSender) {
        if (TextUtils.isEmpty(msgSender)) return false;
        try {
            Pattern p = Pattern.compile("(-?\\d+)");
            Matcher m = p.matcher(msgSender);
            return !m.find();
        } catch (Exception e) {
            Timber.e(e, "isTransactionSms");
        }
        return true;
    }

    private boolean validateTransType(String[] keySearch, String smsMsg) {
        try {
            for (String pattern : keySearch) {
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(smsMsg);
                if (m.find()) return true;
            }
        } catch (Exception e) {
            Timber.e(e, "validateTransType");
        }
        return false;
    }

    private void extractAccountDetails(String smsMsg, String transType) {
        String[] reqMatch = new String[]{"", ""};
        fromAccountDetails = new String[]{"", "", "", "", "", "", ""};
        toAccountDetails = new String[]{"", "", "", "", "", "", ""};

        int[] mIndx = "Transfer".equals(transType) ? new int[]{1, 2} : new int[]{1};

        try {
            boolean isUPI = smsMsg.contains("@");
            if (!isUPI) {
                for (int j = 0; j < mIndx.length; j++) {
                    reqMatch[j] = searchForAccountNum(smsMsg, mIndx[j]);
                }
            } else {
                extractUpiAccountDetails(smsMsg, reqMatch);
            }
            getAccountDetails(reqMatch);
        } catch (Exception e) {
            Timber.e(e, "extractAccountDetails");
        }
    }

    private void extractUpiAccountDetails(String smsMsg, String[] reqMatch) {
        String fromString = " from";
        String toString = " to";
        String nonUPIMsg = smsMsg;

        int fromIndex = smsMsg.indexOf(fromString);
        int toIndex = smsMsg.indexOf(toString);

        if (toIndex == -1) {
            toString = " in your";
            toIndex = smsMsg.indexOf(toString);
        }

        if (fromIndex > 0) {
            if (toIndex == -1 || fromIndex <= toIndex) {
                reqMatch[0] = searchForAccountNum(smsMsg.substring(fromIndex), 1);
            } else {
                reqMatch[0] = searchForAccountNum(smsMsg.substring(fromIndex, toIndex), 1);
                nonUPIMsg = smsMsg.substring(0, fromIndex);
            }
        }

        if (toIndex > 0) {
            if (fromIndex == -1 || toIndex <= fromIndex) {
                reqMatch[1] = searchForAccountNum(smsMsg.substring(toIndex), 1);
            } else {
                reqMatch[1] = searchForAccountNum(smsMsg.substring(toIndex, fromIndex), 1);
                nonUPIMsg = smsMsg.substring(0, toIndex);
            }
        }

        if (fromIndex == -1) reqMatch[0] = searchForAccountNum(nonUPIMsg, 1);
        if (toIndex == -1) reqMatch[1] = searchForAccountNum(nonUPIMsg, 1);

        if (!TextUtils.isEmpty(reqMatch[0]) && reqMatch[0].contains(reqMatch[1])) reqMatch[1] = "";
    }

    private String searchForAccountNum(String smsMsg, long mIndx) {
        String[] searchFor = {
                "((\\s)?((\\d+)?[Xx*]+(\\d+))(\\s)?)", "(no\\.(.*?)\\sis)", "(for\\s(.*?)\\son)",
                "((\\s)?Account\\s?No(.*?)\\s?(\\d+)(\\s)?)", "((\\s)?A/\\.\\s?No(.*?)\\s?(\\d+)(\\s)?)",
                "[N-n][O-o](.)?(:)?(\\s)?'(.*?)'", "((\\s)using\\scard\\s(.*?)\\s.emaining)",
                "(\\(((.*?)[@](.*?))\\))", "(from((.*?)@(.*?))[.])", "(linked((.*?)@(.*?))[.])",
                "((\\s)virtual(\\s)address((.*?)@(.*?))(\\s))", "(your\\s(.*?)\\s+using)",
                "(\\[(\\d+)\\])", "(using(.*?)(\\.(?!\\d)))", "(.ay.m\\s.allet)"
        };

        int[] getGroup = {5, 2, 2, 4, 4, 4, 3, 2, 2, 2, 4, 2, 2, 2, 1};

        try {
            long mFound = 1;
            for (int i = 0; i < searchFor.length; i++) {
                Pattern p = Pattern.compile(searchFor[i]);
                Matcher m = p.matcher(smsMsg);
                while (m.find()) {
                    if (mFound == mIndx) {
                        String match = m.group(getGroup[i]);
                        if (match == null) continue;
                        String group = match.trim();
                        if (group.matches("\\d+") && !group.matches("[a-zA-Z@]+")) {
                            return "X" + group;
                        } else {
                            return group;
                        }
                    }
                    mFound++;
                }
            }
        } catch (Exception e) {
            Timber.e(e, "searchForAccountNum");
        }
        return "";
    }

    private String extractTransAmount(String smsMsg, String fromAccCurrencySymbl) {
        smsMsg = smsMsg.replace(",", "");
        String searchFor = "((\\s)?##SEARCH4CURRENCY##(.)?(\\s)?((\\d+)(\\.\\d+)?))";
        String[] searchCurrency = "INR".equals(fromAccCurrencySymbl) ? new String[]{"INR", "Rs"} : new String[]{fromAccCurrencySymbl};

        try {
            for (String currency : searchCurrency) {
                Pattern p = Pattern.compile(searchFor.replace("##SEARCH4CURRENCY##", currency));
                Matcher m = p.matcher(smsMsg);
                if (m.find()) {
                    String match = m.group(5);
                    return match != null ? match.trim() : "";
                }
            }
        } catch (Exception e) {
            Timber.e(e, "extractTransAmount");
        }
        return "";
    }

    private String[] extractTransPayee(String smsMsg) {
        String[] searchFor = {
                "((\\s)at\\s(.*?)\\s+(?:on|for))", "((\\s)favoring\\s(.*?)\\s+is)",
                "((\\s)to\\s(.*?)\\s+at)", "((\\s)to\\s(.*?)[.])",
                "((\\s)at\\s(.*?)[.])", "(\\*(.*?)[.])",
                "((\\s)FROM\\s(.*?)\\s+\\d)", "(from\\s(.*?)\\s(\\())", "(([a-zA-Z]+)(\\s)has(\\s)added)",
                "((\\s)paid\\s(.*?)\\s)",
                "((\\s)at\\s(.*?)\\s+using)", "(-(.*?)\\son\\s(.*?)[.])", "((\\d+)/(.*)[a-zA-Z](.*)/)",
                "((\\d)\\s(?:from|FROM)\\s((.*?)\\s(.*?))(\\.))", "(\\d,(.*)(\\s)credited)",
                "((?:at|on)\\s([a-zA-Z]((.*?)(\\w+)))\\.)", "(\\son(.*?)\\*(.*?)\\.)"};

        int[] getGroup = {3, 3, 3, 3, 3, 2, 3, 2, 2, 3, 3, 3, 3, 3, 2, 2, 3};

        try {
            for (int i = 0; i < searchFor.length; i++) {
                Pattern p = Pattern.compile(searchFor[i]);
                Matcher m = p.matcher(smsMsg);
                while (m.find()) {
                    String match = m.group(getGroup[i]);
                    if (match == null) continue;
                    String[] details = getPayeeDetails(match.trim());
                    if (!details[0].isEmpty()) return details;
                }
            }
        } catch (Exception e) {
            Timber.e(e, "extractTransPayee");
        }
        return new String[]{"", "", "", ""};
    }

    private String extractTransRefNo(String smsMsg) {
        String[] searchFor = {"(Cheque\\sNo[.*?](\\d+))", "(Ref\\s[Nn]o([.:])?\\s(\\d+))", "(\\s(\\d+(.*?)\\d+)TXN\\s)",
                "(I[D/d](.)?(:)?(\\s)?((.*?)\\w+))", "(I[D/d](.)?(:)?)(\\s)?(\\d+)", "(id(\\s)is(\\s)?(:)?(\\d+))",
                "((Reference:)(\\s)?(\\d+))", "(\\*(\\d+)\\*)", "(\\*(.*?)(\\d+)?[.]\\s?)",
                "((reference number)(.*?)(\\d+))", "(\\s)?#(\\s?)(\\d+)(\\s?)", "([A-Za-z*]/+(\\d+)+/[A-Za-z*])",
                "((?:UPI|IMPS)\\s?(?::|/)\\s?(\\d+)\\s?)", "(Info(:)+(.*?)(\\d+)?[.:-]?)", "(I[Dd]\\s?([.:])\\s?((.*?)(\\d+))\\s)"};

        int[] getGroup = {2, 4, 2, 5, 5, 5, 4, 2, 3, 4, 3, 2, 2, 4, 3};

        try {
            for (int i = 0; i < searchFor.length; i++) {
                Pattern p = Pattern.compile(searchFor[i]);
                Matcher m = p.matcher(smsMsg);
                while (m.find()) {
                    String match = m.group(getGroup[i]);
                    if (match == null) continue;
                    String ref = match.trim();
                    try {
                        Long.parseLong(ref);
                        return ref;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception e) {
            Timber.e(e, "extractTransRefNo");
        }
        return "";
    }

    @SuppressLint("Range")
    private String[] getPayeeDetails(String payeeName) {
        String[] payeeDetails = new String[]{"", payeeName.trim(), ""};
        if (payeeName.trim().isEmpty()) return payeeDetails;

        String sql = "SELECT PAYEEID, PAYEENAME, CATEGID FROM PAYEE_V1 WHERE PAYEENAME LIKE ? ORDER BY PAYEENAME LIMIT 1";
        String[] selectionArgs = new String[]{"%" + payeeName + "%"};

        try (Cursor cursor = mOpenHelper.getReadableDatabase().query(sql, selectionArgs)) {
            if (cursor != null && cursor.moveToFirst()) {
                payeeDetails = new String[]{
                        cursor.getString(cursor.getColumnIndexOrThrow("PAYEEID")),
                        cursor.getString(cursor.getColumnIndexOrThrow("PAYEENAME")),
                        cursor.getString(cursor.getColumnIndexOrThrow("CATEGID"))
                };
            }
        } catch (Exception e) {
            Timber.e(e, "getPayeeDetails");
        }
        return payeeDetails;
    }

    @SuppressLint("Range")
    private Long getTxnId(String refNumber, String transDate) {
        if (refNumber.trim().isEmpty()) return 0L;

        String sql = "SELECT TRANSID FROM CHECKINGACCOUNT_V1 WHERE TRANSACTIONNUMBER LIKE ? AND TRANSDATE = ? ORDER BY TRANSID LIMIT 1";
        String[] selectionArgs = new String[]{"%" + refNumber + "%", transDate};

        try (Cursor cursor = mOpenHelper.getReadableDatabase().query(sql, selectionArgs)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow("TRANSID"));
            }
        } catch (Exception e) {
            Timber.e(e, "getTxnId");
        }
        return 0L;
    }

    @SuppressLint("Range")
    private String[] getCategoryOrSubCategoryByName(String searchName) {
        String[] result = new String[]{"", ""};
        if (searchName.trim().isEmpty()) return result;

        String sql = "SELECT CATEGID, PARENTID FROM CATEGORY_V1 WHERE CATEGNAME = ? ORDER BY PARENTID DESC, CATEGNAME ASC LIMIT 1";
        String[] selectionArgs = new String[]{searchName};

        try (Cursor cursor = mOpenHelper.getReadableDatabase().query(sql, selectionArgs)) {
            if (cursor != null && cursor.moveToFirst()) {
                result = new String[]{
                        cursor.getString(cursor.getColumnIndexOrThrow("CATEGID")),
                        cursor.getString(cursor.getColumnIndexOrThrow("PARENTID"))
                };
            }
        } catch (Exception e) {
            Timber.e(e, "getCategoryOrSubCategoryByName");
        }
        return result;
    }

    @SuppressLint("Range")
    private void getAccountDetails(String[] reqMatch) {
        fromAccountDetails = new String[]{"", "", "", "", "", "", ""};
        toAccountDetails = new String[]{"", "", "", "", "", "", ""};

        for (int j = 0; j < reqMatch.length; j++) {
            if (reqMatch[j].isEmpty()) continue;

            String[] accountDetails = new String[]{"", "", "", "", "", "", reqMatch[j]};
            String selectionArg = "%" + reqMatch[j] + "%";

            String sql = "SELECT A.ACCOUNTID, A.ACCOUNTNAME, A.ACCOUNTNUM, A.CURRENCYID, " +
                    "C.CURRENCY_SYMBOL, C.DECIMAL_POINT, C.GROUP_SEPARATOR " +
                    "FROM ACCOUNTLIST_V1 A " +
                    "INNER JOIN CURRENCYFORMATS_V1 C ON C.CURRENCYID = A.CURRENCYID " +
                    "WHERE A.STATUS='Open' AND A.ACCOUNTNUM LIKE ? " +
                    "ORDER BY A.ACCOUNTID LIMIT 1";

            try (Cursor cursor = mOpenHelper.getReadableDatabase().query(sql, new String[]{selectionArg})) {
                if (cursor != null && cursor.moveToFirst()) {
                    accountDetails = new String[]{
                            cursor.getString(cursor.getColumnIndexOrThrow("ACCOUNTID")),
                            cursor.getString(cursor.getColumnIndexOrThrow("ACCOUNTNAME")),
                            cursor.getString(cursor.getColumnIndexOrThrow("CURRENCYID")),
                            cursor.getString(cursor.getColumnIndexOrThrow("CURRENCY_SYMBOL")),
                            cursor.getString(cursor.getColumnIndexOrThrow("DECIMAL_POINT")),
                            cursor.getString(cursor.getColumnIndexOrThrow("GROUP_SEPARATOR")),
                            reqMatch[j]
                    };
                }
            } catch (Exception e) {
                Timber.e(e, "getAccountDetails");
            }

            if (j == 0) fromAccountDetails = accountDetails;
            else if (j == 1) toAccountDetails = accountDetails;
        }
    }

    public String validateData() {
        if (mCommon.transactionEntity.getAccountId().equals(Constants.NOT_SET)) {
            return mContext.getString(R.string.error_fromaccount_not_selected);
        }
        if (mCommon.transactionEntity.getAmount().toDouble() <= 0) {
            return mContext.getString(R.string.error_amount_must_be_positive);
        }
        if (mCommon.transactionEntity.getTransactionType().equals(TransactionTypes.Transfer)) {
            if (mCommon.transactionEntity.getToAccountId().equals(Constants.NOT_SET)) {
                return mContext.getString(R.string.error_toaccount_not_selected);
            }
            if (mCommon.transactionEntity.getToAccountId().equals(mCommon.transactionEntity.getAccountId())) {
                return mContext.getString(R.string.error_transfer_to_same_account);
            }
            if (mCommon.transactionEntity.getToAmount().toDouble() <= 0) {
                return mContext.getString(R.string.error_amount_must_be_positive);
            }
        } else {
            if (!mCommon.transactionEntity.hasPayee()) {
                return mContext.getString(R.string.error_payee_not_selected);
            }
        }
        if (!mCommon.transactionEntity.hasCategory()) {
            return mContext.getString(R.string.error_category_not_selected);
        }
        return "PASS";
    }

    public String saveTransaction() {
        AccountTransactionRepository repo = new AccountTransactionRepository(mContext);
        if (!mCommon.transactionEntity.hasId()) {
            mCommon.transactionEntity = repo.insert((AccountTransaction) mCommon.transactionEntity);
            if (!mCommon.transactionEntity.hasId()) {
                return mContext.getString(R.string.db_checking_insert_failed);
            }
        } else {
            if (!repo.update((AccountTransaction) mCommon.transactionEntity)) {
                return mContext.getString(R.string.db_checking_update_failed);
            }
        }
        return "PASS";
    }

    private void showNotification(Intent intent, String notificationText, String msgSender, String txnStatus, String errorMsg) {
        try {
            int notificationId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "AMMEXSMS", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(mContext.getString(R.string.notification_process_sms_channel_description));
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(mContext.getString(R.string.notification_process_sms_transaction_status) + ": " + txnStatus + errorMsg)
                    .setSubText(mContext.getString(R.string.notification_click_to_edit_transaction))
                    .setSmallIcon(R.drawable.ic_stat_notification)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(msgSender + " : " + notificationText))
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                    .setGroup("com.android.example.MoneyManagerEx");

            int colorResId;
            switch (txnStatus) {
                case "Auto Failed": colorResId = R.color.md_red; break;
                case "Already Exists": colorResId = R.color.md_indigo; break;
                default: colorResId = R.color.md_primary; break;
            }
            builder.setColor(ContextCompat.getColor(mContext, colorResId));

            notificationManager.notify(notificationId, builder.build());
        } catch (Exception e) {
            Timber.e(e, "showing notification for sms transaction");
        }
    }
}
