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
import androidx.sqlite.db.SupportSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQueryBuilder;

import android.telephony.SmsMessage;
import android.widget.Toast;

import com.money.manager.ex.Constants;
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
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.money.manager.ex.transactions.EditTransactionActivityConstants;
import com.money.manager.ex.transactions.EditTransactionCommonFunctions;
import com.money.manager.ex.utils.MmxDate;
import com.squareup.sqlbrite3.BriteDatabase;

import javax.inject.Inject;

import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

import static androidx.core.content.ContextCompat.startActivity;
import static java.lang.Integer.*;

public class SmsReceiverTransactions extends BroadcastReceiver {

    private Context mContext;

    @Inject
    BriteDatabase database;

    // Db setup
    public static MmxOpenHelper openHelper;

    private EditTransactionCommonFunctions mCommon;

    static String[] fromAccountDetails;
    static String[] toAccountDetails;

    public static String CHANNEL_ID = "SmsTransaction_NotificationChannel";
    private static final long ID_NOTIFICATION = 0x000A;

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context.getApplicationContext();

        final BehaviourSettings behav_settings = new BehaviourSettings(mContext);
        final GeneralSettings gen_settings = new GeneralSettings(mContext);
        final AppSettings app_settings = new AppSettings(mContext);
        final PreferenceConstants prf_const = new PreferenceConstants();

        //App Settings
        long baseCurencyID, fromCurrencyID, toCurrencyID;
        long baseAccountID, fromAccountID, toAccountID;

        String baseCurrencySymbl, fromAccCurrencySymbl, toAccCurrencySymbl;
        String baseAccountName, fromAccountName, toAccountName;

        Boolean autoTransactionStatus = false;
        Boolean skipSaveTrans = false;

        try {
            //------- if settings enabled the parse the sms and create trans ---------------
            if (behav_settings.getBankSmsTrans()) {

                //---get the SMS message passed in---
                Bundle bundle = intent.getExtras();
                SmsMessage[] msgs = null;
                String msgBody = "";
                String msgSender = "";

                if (bundle != null) { //---retrieve the SMS message received---

                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];

                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        msgSender = msgs[i].getOriginatingAddress();
                        msgBody += msgs[i].getMessageBody();
                    }

                    //Must be commented in released version
                    //msgSender = "AT-SIBSMS";

                    if(isTransactionSms(msgSender) && !msgBody.toLowerCase().contains("otp")) {
                        // Transaction Sms sender will have format like this AT-SIBSMS, South Indian Bank
                        // Promotional sms will have sender like AT-012345
                        // Not sure how this format will be in out side of India. I may need to update if I get sample

                        // MMEX Helper
                        openHelper = new MmxOpenHelper(mContext, app_settings.getDatabaseSettings().getDatabasePath());
                        boolean smsTxnStatusNotificationSetting = behav_settings.getSmsTransStatusNotification();

                        ITransactionEntity model = AccountTransaction.create();
                        mCommon = new EditTransactionCommonFunctions(null, model, database);

                        // find out the trans type using reg ex
                        String[] key_credit_search = {"(credited)", "(received)", "(added)", "(reloaded)", "(deposited)", "(refunded)",
                                "(debited)(.*?)(towards)(\\s)", "(\\s)(received)(.*?)(in(\\s)your)(\\s)", "(sent)(.*?)(to)(\\s)", "(debited)(.*?)(to)(\\s)",
                                "(credited)(.*?)(in)(\\s)", "(credited)(.*?)(to)(\\s)", "(recharge)"};

                        // - Sales Draft added for LBP currency. Request from HussienH
                        String[] key_debit_search = {"(made)", "(debited)", "(using)", "(paid)", "(purchase)", "(withdrawn)", "(done)",
                                "(credited)(.*?)(from)(\\s)", "(sent)(.*?)(from)(\\s)", "(\\s)(received)(.*?)(from)(\\s)",
                                "(sales\\sdraft)", "(spent)"}; //

                        String transType = "";

                        //Handle the string
                        msgBody = msgBody.replaceAll("[\\t\\n\\r]+"," ");
                        msgBody = msgBody.replaceAll("  "," ");

                        Boolean isDeposit = validateTransType(key_credit_search, msgBody.toLowerCase());
                        Boolean isWithdrawal = validateTransType(key_debit_search, msgBody.toLowerCase());

                        if (isDeposit) {

                            if (isWithdrawal) {
                                transType = "Transfer";
                                String[] transCategory = getCategoryOrSubCategoryByName("Transfer");

                                if (!transCategory[0].isEmpty()) {
                                    mCommon.transactionEntity.setCategoryId(Long.parseLong(transCategory[0]));
                                }

                                mCommon.transactionEntity.setTransactionType(TransactionTypes.Transfer);

                            } else {
                                transType = "Deposit";
                                String[] incomeCategory = getCategoryOrSubCategoryByName("Income");

                                if (!incomeCategory[0].isEmpty()) {
                                    mCommon.transactionEntity.setCategoryId(Long.parseLong(incomeCategory[0]));
                                }

                                mCommon.transactionEntity.setTransactionType(TransactionTypes.Deposit);
                            }

                        } else if (isWithdrawal) {
                            transType = "Withdrawal";
                            mCommon.transactionEntity.setTransactionType(TransactionTypes.Withdrawal);
                        }

                        mCommon.transactionEntity.setStatus("");
                        mCommon.payeeName = "";

                        // if it is blank, then nothing to do with sms
                        if (!transType.isEmpty()) {

                            //Create the intent that’ll fire when the user taps the notification//
                            Intent t_intent = new Intent(mContext, CheckingTransactionEditActivity.class);

                            baseCurencyID = gen_settings.getBaseCurrencyId();
                            baseAccountID = gen_settings.getDefaultAccountId();
                            baseAccountName = "";
                            fromAccountID = -1;
                            fromCurrencyID = -1;
                            fromAccountName = "";

                            //if default account id selected
                            if (baseAccountID > 0) {
                                fromAccountID = baseAccountID;
                                fromAccountName = baseAccountName;
                                fromCurrencyID = baseCurencyID;
                            }

                            //Get the base currency sysmbl
                            baseCurrencySymbl = getCurrencySymbl(baseCurencyID);
                            fromAccCurrencySymbl = baseCurrencySymbl;

                            //get te from acount details
                            extractAccountDetails(msgBody, transType);

                            if (!fromAccountDetails[0].isEmpty()) {
                                fromAccountID = Long.parseLong(fromAccountDetails[0]);
                                fromAccountName = fromAccountDetails[1];
                                fromCurrencyID = Long.parseLong(fromAccountDetails[2]);
                                fromAccCurrencySymbl = fromAccountDetails[3];
                                mCommon.transactionEntity.setAccountId(fromAccountID);
                            }

                            mCommon.transactionEntity.setNotes(msgBody);
                            mCommon.transactionEntity.setDate(new MmxDate().toDate());

                            //get the trans amount
                            String transAmount = extractTransAmount(0, msgBody, fromAccCurrencySymbl);
                            String balanceAmount = extractTransAmount(1, msgBody, fromAccCurrencySymbl);
                            String[] transPayee = extractTransPayee(msgBody);

                            //If there is no account no. or payee in the msg & no amt, then this is not valid sms to do transaction
                            if ((!fromAccountDetails[6].isEmpty() || !toAccountDetails[6].isEmpty() ||
                                    !transPayee[0].isEmpty()) && !transAmount.isEmpty()) {

                                mCommon.transactionEntity.setAmount(MoneyFactory.fromString(transAmount));

                                String transRefNo = extractTransRefNo(msgBody);

                                //get the ref no. if doesn't exits
                                if(transRefNo.isEmpty()){
                                    transRefNo = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                                }

                                //set the txn no
                                mCommon.transactionEntity.setTransactionNumber(transRefNo);

                                long txnId = getTxnId(transRefNo.trim(), mCommon.transactionEntity.getDateString());

                                //Update existing transaction
                                if (txnId == 0) { //add new trnsaction

                                    if (transType.equals("Transfer")) { //if it is transfer

                                        if (!toAccountDetails[0].isEmpty()) { // if id exists then considering as account transfer

                                            toAccountID = Long.parseLong(toAccountDetails[0]);
                                            toAccountName = toAccountDetails[1];
                                            toCurrencyID = Long.parseLong(toAccountDetails[2]);
                                            toAccCurrencySymbl = toAccountDetails[3];

                                            mCommon.transactionEntity.setToAccountId(toAccountID);

                                            //convert the to amount from the both currency details
                                            CurrencyService currencyService = new CurrencyService(mContext);
                                            mCommon.transactionEntity.setToAmount(currencyService.doCurrencyExchange(fromCurrencyID,
                                                    mCommon.transactionEntity.getAmount(),
                                                    toCurrencyID));

                                            mCommon.transactionEntity.setPayeeId(Constants.NOT_SET);

                                        } else { // if not, then may be IMPS transfer to 3rd party

                                            //if there is no to account found from mmex db, then check for payee
                                            //This will helps me to handle 3rd party transfer thru IMPS
                                            if (!toAccountDetails[6].isEmpty() && transPayee[0].isEmpty()) {
                                                transPayee = getPayeeDetails(toAccountDetails[6].trim());
                                            }
                                        }
                                    }

                                    if (!transPayee[0].isEmpty()) {

                                        transType = "Withdrawal";

                                        mCommon.transactionEntity.setTransactionType(TransactionTypes.Withdrawal);
                                        mCommon.transactionEntity.setToAccountId(Constants.NOT_SET);
                                        mCommon.transactionEntity.setToAmount(MoneyFactory.fromString(transAmount));

                                        mCommon.transactionEntity.setPayeeId(Long.parseLong(transPayee[0]));
                                        mCommon.payeeName = transPayee[1];
                                        mCommon.transactionEntity.setCategoryId(Long.parseLong(transPayee[2]));
                                    }

                                    t_intent.setAction(Intent.ACTION_INSERT); //Set the action

                                } else {

                                    AccountTransactionRepository repo = new AccountTransactionRepository(mContext);
                                    AccountTransaction txn = repo.load(txnId);

                                    if (txn != null) {

                                        if (txn.getTransactionType() != TransactionTypes.Transfer) {

                                            AccountRepository accountRepository = new AccountRepository(mContext);

                                            if (txn.getTransactionType() == TransactionTypes.Deposit) { //transType = "Deposit";
                                                toAccountID = txn.getAccountId();
                                                toCurrencyID = accountRepository.loadCurrencyIdFor(txn.getAccountId());
                                            } else { //transType = "Withdrawal";
                                                toAccountID = fromAccountID;
                                                toCurrencyID = fromCurrencyID;
                                                fromCurrencyID = accountRepository.loadCurrencyIdFor(txn.getAccountId());
                                            }

                                            mCommon.transactionEntity = txn;
                                            // check if this can be removed mCommon.transactionEntity.setTransactionType(TransactionTypes.Transfer);
                                            mCommon.transactionEntity.setAccountId(fromAccountID);
                                            // check if this can be removed mCommon.transactionEntity.setToAccountId(toAccountID);

                                            //convert the to amount from the both currency details
                                            CurrencyService currencyService = new CurrencyService(mContext);
                                            mCommon.transactionEntity.setToAmount(currencyService.doCurrencyExchange(fromCurrencyID,
                                                    mCommon.transactionEntity.getAmount(),
                                                    toCurrencyID));

                                            mCommon.transactionEntity.setPayeeId(Constants.NOT_SET);

                                            String[] transCategory = getCategoryOrSubCategoryByName("Transfer");
                                            if (!transCategory[0].isEmpty()) {
                                                mCommon.transactionEntity.setCategoryId(Long.parseLong(transCategory[0]));
                                            }

                                            mCommon.transactionEntity.setNotes(mCommon.transactionEntity.getNotes() + "\n\n" + msgBody);

                                            t_intent.setAction(Intent.ACTION_EDIT); //Set the action

                                        } else { //if transfer already exists, then do nothing

                                            mCommon.transactionEntity = txn;
                                            t_intent.setAction(Intent.ACTION_EDIT); //Set the action

                                            skipSaveTrans = true;
                                        }

                                    }
                                }

                                // Capture the details the for Toast
                                String strExtracted = "Account = " + fromAccountName + "-" + fromAccountDetails[6] + "\n"
                                        + "Trans Amt = " + fromAccCurrencySymbl + " " + transAmount + ",\n"
                                        + "Payyee Name= " + transPayee[1] + "\n"
                                        + "Category ID = " + transPayee[2] + "\n"
                                        + "Trans Ref No. = " + transRefNo + "\n"
                                        + "Trans Type = " + transType + "\n";

                                //Must be commented for released version
                                //mCommon.transactionEntity.setNotes(strExtracted);

                                // Set the content for a transaction);
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_SOURCE, "SmsReceiverTransactions.java");
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_ID, mCommon.transactionEntity.getId());
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_ACCOUNT_ID, String.valueOf(mCommon.transactionEntity.getAccountId()));
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TO_ACCOUNT_ID, String.valueOf(mCommon.transactionEntity.getToAccountId()));
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_CODE, mCommon.getTransactionType());
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_PAYEE_ID, String.valueOf(mCommon.transactionEntity.getPayeeId()));
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_PAYEE_NAME, mCommon.payeeName);
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_CATEGORY_ID, String.valueOf(mCommon.transactionEntity.getCategoryId()));
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_AMOUNT, String.valueOf(mCommon.transactionEntity.getAmount()));
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_NOTES, mCommon.transactionEntity.getNotes());
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_DATE, mCommon.transactionEntity.getDate());
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_NUMBER, mCommon.transactionEntity.getTransactionNumber());

                                // validate and save the transaction
                                if(!skipSaveTrans) {

                                    t_intent.addFlags((Intent.FLAG_ACTIVITY_NEW_TASK)); // Fix for https://github.com/moneymanagerex/android-money-manager-ex/issues/2210
                                    String validationStatus = validateData();

                                    if (validationStatus.equals("PASS")) {
                                        String saveStatus = saveTransaction();

                                        if (saveStatus.equals("PASS")) {

                                            autoTransactionStatus = true;

                                            if (smsTxnStatusNotificationSetting) {
                                                t_intent.setAction(Intent.ACTION_EDIT);
                                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_ID, mCommon.transactionEntity.getId());

                                                showNotification(t_intent, msgBody, msgSender, "Successful", "");
                                            }
                                            else {
                                                Toast.makeText(context, "MMEX: Bank Transaction Processed for: \n\n" + strExtracted, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        else {
                                            if (smsTxnStatusNotificationSetting) {
                                                showNotification(t_intent, msgBody, msgSender, "Save Failed", " - " + saveStatus);
                                            }
                                            else {
                                                startActivity(mContext, t_intent, null);
                                                Toast.makeText(context, "AMMEX Save Failed : " + saveStatus, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }

                                    //if transaction is not created automatically, then invoke notification or activity screen
                                    if (!autoTransactionStatus) {

                                        if (smsTxnStatusNotificationSetting) {
                                            showNotification(t_intent, msgBody, msgSender, "Auto Failed", " - " + validationStatus);
                                        }
                                        else {
                                            startActivity(mContext, t_intent, null);
                                            Toast.makeText(context, "AMMEX Auto Failed : " + validationStatus, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }
                                else {
                                    if (smsTxnStatusNotificationSetting) {
                                        showNotification(t_intent, "MMEX: Skiping Bank Transaction updates SMS, because transaction exists with ref. no. " + transRefNo, msgSender, "Already Exists", "");
                                    }
                                    else {
                                        Toast.makeText(context, "MMEX: Skiping Bank Transaction updates SMS, because transaction exists with ref. no. " + transRefNo, Toast.LENGTH_LONG).show();
                                    }
                                }

                                //reset the value
                                msgBody = "";
                                msgSender = "";
                                bundle = null;
                                msgs = null;
                                mCommon = null;
                                skipSaveTrans = false;

                            }
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            Timber.e(e, "MMEX: Bank Transaction Process EXCEPTION");
        }
    }

    @SuppressLint("Range")
    private static String getCurrencySymbl(long currencyID)
    {
        //Get the currency sysmbl
        String currencySymbl = "";
        String[] reqCurrFields = {"CURRENCYID", "DECIMAL_POINT", "GROUP_SEPARATOR",  "CURRENCY_SYMBOL"};

        String tableName = "CURRENCYFORMATS_V1";
        String selection = "CURRENCYID = ?";
        String[] selectionArgs = new String[]{String.valueOf(currencyID)};

        SupportSQLiteQueryBuilder queryBuilder = SupportSQLiteQueryBuilder.builder(tableName);
        SupportSQLiteQuery query = queryBuilder.selection(selection, selectionArgs)
                .columns(reqCurrFields)
                .orderBy(null)
                .create();
        try {
            Cursor currencyCursor = openHelper.getReadableDatabase().query(query);

            if (currencyCursor.moveToFirst()) {
                currencySymbl = currencyCursor.getString(currencyCursor.getColumnIndexOrThrow("CURRENCY_SYMBOL"));
            }

            currencyCursor.close();
        } catch (Exception e) {
            Timber.e(e, "getCurrencySymbl");
        }

        return  currencySymbl;

    }

    private static boolean isTransactionSms(String smsSender)
    {
        boolean reqMatch = true;

        try
        {
            Pattern p = Pattern.compile("(-?\\d+)");
            Matcher m = p.matcher(smsSender);

            if (m != null) {
                while(m.find()) {
                    reqMatch = false;
                    break;
                }
            }
        }
        catch(Exception e)
        {
            Timber.e(e, "isTransactionSms");
        }

        return reqMatch;
    }

    private static boolean validateTransType(String[] keySearch, String smsMsg)
    {
        boolean reqMatch = false;

        try
        {
            for(int i=0; i<=keySearch.length-1; i++) {
                Pattern p = Pattern.compile(keySearch[i]);
                Matcher m = p.matcher(smsMsg);

                if (m != null && !reqMatch) {
                    while(m.find()) {
                        reqMatch = true;
                        break;
                    }
                }
            }
        }
        catch(Exception e) {
            Timber.e(e, "validateTransType");
        }

        return reqMatch;
    }

    private static void extractAccountDetails(String smsMsg, String transType)
    {
        String[] reqMatch =  new String[]{"", ""};

        fromAccountDetails = new String[]{"", "", "", "", "", "", ""};
        toAccountDetails = new String[]{"", "", "", "", "", "", ""};

        int[] mIndx;

        if(transType == "Transfer") { mIndx = new int[] {1, 2}; }
        else { mIndx = new int[] {1}; }

        try
        {
            //find the match for UPI transfer which has "from" or "to" string
            boolean isUPI = smsMsg.contains("@");

            switch (String.valueOf(isUPI)) {

                case "false": //find the match for non UPI transfer or credit or debit

                    for(int j=0; j<=mIndx.length-1; j++) {
                        reqMatch[j] = searchForAccountNum(smsMsg, mIndx[j]);
                    }
                    break;

                case "true": //UPI transfer based on from and to account

                    String fromString = " from";
                    String toString = " to";
                    String nonUPIMsg = smsMsg;

                    int fromIndex = smsMsg.indexOf(fromString);
                    int toIndex = smsMsg.indexOf(toString);

                    // sometime str "to" not exists, in place use str "in your"
                    if(toIndex == -1){
                        toString = " in your";
                        toIndex = smsMsg.indexOf(toString);
                    }

                    if(fromIndex > 0) {
                        if(fromIndex > toIndex) {
                            reqMatch[0] = searchForAccountNum(smsMsg.substring(fromIndex), 1);
                            if(toIndex == -1) { nonUPIMsg = smsMsg.substring(0, fromIndex); }
                        }else{
                            reqMatch[0] = searchForAccountNum(smsMsg.substring(fromIndex, toIndex), 1);
                            nonUPIMsg = smsMsg.substring(0, fromIndex);
                        }
                    }

                    if(toIndex > 0) {
                        if(toIndex > fromIndex) {
                            reqMatch[1] = searchForAccountNum(smsMsg.substring(toIndex), 1);
                            if(toIndex == -1) { nonUPIMsg = smsMsg.substring(0, toIndex); }
                        }else{
                            reqMatch[1] = searchForAccountNum(smsMsg.substring(toIndex, fromIndex), 1);
                            nonUPIMsg = smsMsg.substring(0, toIndex);
                        }
                    }

                    if(fromIndex == -1) { reqMatch[0] = searchForAccountNum(nonUPIMsg, 1); }
                    if(toIndex == -1) { reqMatch[1] = searchForAccountNum(nonUPIMsg, 1); }

                    //if both the str are same then, reset 2nd index
                    if(reqMatch[0].contains(reqMatch[1])) { reqMatch[1] = ""; }

                    break;

                default:
                    Timber.e("extractAccountDetails: Unable to find either UPI or non UPI txns...");
                    break;
            }

            getAccountDetails(reqMatch);

        }
        catch(Exception e) {
            Timber.e(e, "extractAccountDetails");
        }
    }

    private static String searchForAccountNum(String smsMsg, long mIndx)
    {
        String reqMatch =  "";

        // - ((\s)using\scard\s(.*?)\s.emaining) added for LBP currency. Request from HussienH
        String[] searchFor =
                {
                        "((\\s)?((\\d+)?[Xx\\*]+(\\d+))(\\s)?)", "(no\\.(.*?)\\sis)", "(for\\s(.*?)\\son)",
                        "((\\s)?Account\\s?No(.*?)\\s?(\\d+)(\\s)?)", "((\\s)?A/.\\s?No(.*?)\\s?(\\d+)(\\s)?)",
                        "[N-n][O-o](.)?(:)?(\\s)?'(.*?)'", "((\\s)using\\scard\\s(.*?)\\s.emaining)",
                        "([\\(]((.*?)[@](.*?))[\\)])", "(from((.*?)@(.*?))[.])", "(linked((.*?)@(.*?))[.])",
                        "((\\s)virtual(\\s)address((.*?)@(.*?))(\\s))", "(your\\s(.*?)\\s+using)",
                        "([\\[](\\d+)[\\]])", "(using(.*?)(\\.))", "(.ay.m\\s.allet)"
                };

        int[] getGroup =
                {
                        5, 2, 2,
                        4, 4,
                        4, 3,
                        2, 2, 2,
                        4, 2,
                        2, 2, 1
                };

        long mFound;

        try
        {
            for(int i=0; i<=searchFor.length-1; i++) {
                mFound = 1;

                Pattern p = Pattern.compile(searchFor[i]);
                Matcher m = p.matcher(smsMsg);

                if (m != null && reqMatch.isEmpty()) {
                    while(m.find()) {
                        if(mFound == mIndx) {
                            // Append X with acc no, bcz start with X for non UPI trans
                            if (m.group(getGroup[i]).trim().matches("\\d+") &&
                                    !m.group(getGroup[i]).trim().matches("[a-zA-Z@]+"))
                            {
                                reqMatch = "X" + m.group(getGroup[i]).trim();
                            }
                            else{
                                reqMatch = m.group(getGroup[i]).trim();
                            }
                            break;
                        }
                        else { mFound = mFound + 1; }
                    }
                }
            }
        }
        catch(Exception e)
        {
            Timber.e(e, "searchForAccountNum");
        }

        return reqMatch;
    }

    private static String extractTransAmount(long indexOfAmt, String smsMsg, String fromAccCurrencySymbl)
    {
        String reqMatch = "";
        smsMsg = smsMsg.replace(",", "");
        String searchFor = "((\\s)?##SEARCH4CURRENCY##(.)?(\\s)?((\\d+)(\\.\\d+)?))";
        int[] getGroup = {5};
        long indx = 0;

        //Handle multiple symbol for currency
        String[] searchCurrency;

        if (fromAccCurrencySymbl.contentEquals("INR")) {
            searchCurrency = new String[]{"INR", "Rs"};
        } else {
            searchCurrency = new String[]{fromAccCurrencySymbl};
        }

        try
        {
            for(int i=0; i<=searchCurrency.length-1; i++) {
                Pattern p = Pattern.compile(searchFor.replace("##SEARCH4CURRENCY##", searchCurrency[i]));
                Matcher m = p.matcher(smsMsg);

                if (m != null && reqMatch.isEmpty()) {
                    while(m.find()) {
                        if (indx==indexOfAmt){
                            reqMatch = m.group(getGroup[0]).trim();
                            break;
                        }

                        indx = indx + 1;
                    }
                }
            }

        }
        catch(Exception e)
        {
            Timber.e(e, "extractTransAmount");
        }

        return reqMatch;
    }

    private static String[] extractTransPayee(String smsMsg)
    {
        // - ((\s)at\s(.*?)\s+using) added for LBP currency. Request from HussienH
        String[] searchFor = {
                "((\\s)at\\s(.*?)\\s+(?:on|for))", "((\\s)favoring\\s(.*?)\\s+is)",
                "((\\s)to\\s(.*?)\\s+at)", "((\\s)to\\s(.*?)[.])",
                "((\\s)at\\s(.*?)[.])", "([\\*](.*?)[.])",
                "((\\s)FROM\\s(.*?)\\s+\\d)", "(from\\s(.*?)\\s(\\())", "(([a-zA-Z]+)(\\s)has(\\s)added)",
                "((\\s)paid\\s(.*?)\\s)",
                "((\\s)at\\s(.*?)\\s+using)", "(-(.*?)\\son\\s(.*?)[.])", "((\\d+)/(.*)[a-zA-Z](.*)/)",
                "((\\d)\\s(?:from|FROM)\\s((.*?)\\s(.*?))(\\.))", "(\\d,(.*)(\\s)credited)",
                "((?:at|on)\\s([a-zA-Z]((.*?)(\\w+)))\\.)", "(\\son(.*?)\\*(.*?)\\.)"};

        int[] getGroup = {3, 3, 3, 3, 3, 2, 3, 2, 2, 3, 3, 3, 3, 3, 2, 2, 3};
        String[] reqMatch = new String[]{"", "", "", ""};

        try
        {
            for(int i=0; i<=searchFor.length-1; i++) {
                Pattern p = Pattern.compile(searchFor[i]);
                Matcher m = p.matcher(smsMsg);

                if (m != null && reqMatch[0].isEmpty()) {
                    while(m.find()) {
                        reqMatch = getPayeeDetails(m.group(getGroup[i]).trim());

                        if(!reqMatch[0].isEmpty()){
                            break;
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            Timber.e(e, "extractTransPayee");
        }

        return reqMatch;
    }

    private static String extractTransRefNo(String smsMsg)
    {
        String reqMatch = "";
        String[] searchFor = {"(Cheque\\sNo[.*?](\\d+))", "(Ref\\s[Nn]o([.:])?\\s(\\d+))", "(\\s(\\d+(.*?)\\d+)TXN\\s)",
                "(I[D//d](.)?(:)?(\\s)?((.*?)\\w+))", "(I[D//d](.)?(:)?)(\\s)?(\\d+)", "(id(\\s)is(\\s)?(:)?(\\d+))",
                "((Reference:)(\\s)?(\\d+))",  "([\\*](\\d+)[\\*])", "([\\*](.*?)(\\d+)?[\\.]\\s?)",
                "((reference number)(.*?)(\\d+))", "(\\s)?#(\\s?)(\\d+)(\\s?)",  "([A-Za-z\\*]\\/+(\\d+)+\\/[A-Za-z\\*])",
                "((?:UPI|IMPS)\\s?(?::|/)\\s?(\\d+)\\s?)", "(Info(:)+(.*?)(\\d+)?[\\.:-]?)", "(I[Dd]\\s?([.:])\\s?((.*?)(\\d+))\\s)"};

        int[] getGroup = {2, 3, 2,
                          5, 5, 5,
                          4, 2, 3,
                          4, 3, 2,
                          2, 4, 3};

        try
        {
            for(int i=0; i<=searchFor.length-1; i++)
            {
                Pattern p = Pattern.compile(searchFor[i]);
                Matcher m = p.matcher(smsMsg);

                if (m != null && reqMatch.isEmpty()) {
                    while(m.find()) {
                        try {
                            Long.parseLong(m.group(getGroup[i]).trim()); // Can be Integer.parseInt(str) if checking for integers
                            reqMatch = m.group(getGroup[i]).trim();
                            break;
                        } catch (NumberFormatException e) {
                            //
                        }

                    }
                }
            }
        }
        catch(Exception e)
        {
            Timber.e(e, "extractTransRefNo");
        }

        return reqMatch;
    }

    @SuppressLint("Range")
    private static String[] getPayeeDetails(String payeeName)
    {
        String[] payeeDetails = new String[]{"", payeeName.trim(), ""};

        try
        {
            if(!payeeName.trim().isEmpty()) {

                String sql = "SELECT PAYEEID, PAYEENAME, CATEGID FROM PAYEE_V1 " +
                                "WHERE PAYEENAME LIKE '%" + payeeName + "%' " +
                                "ORDER BY PAYEENAME LIMIT 1";

                Cursor payeeCursor = openHelper.getReadableDatabase().query(sql);

                if(payeeCursor.moveToFirst()) {
                    payeeDetails = new String[] {
                            payeeCursor.getString(payeeCursor.getColumnIndexOrThrow("PAYEEID")),
                            payeeCursor.getString(payeeCursor.getColumnIndexOrThrow("PAYEENAME")),
                            payeeCursor.getString(payeeCursor.getColumnIndexOrThrow("CATEGID"))
                    };
                }

                payeeCursor.close();
            }
        }
        catch(Exception e)
        {
            Timber.e(e, "getPayeeDetails");
        }

        return payeeDetails;
    }

    @SuppressLint("Range")
    private static Long getTxnId(String refNumber, String transDate)
    {
        long txnId = 0;

        try
        {
            if(!refNumber.trim().isEmpty()) {

                String sql =
                        "SELECT TRANSID " +
                                "FROM CHECKINGACCOUNT_V1 " +
                                "WHERE TRANSACTIONNUMBER  LIKE '%" + refNumber + "%' " +
                                "AND TRANSDATE ='" + transDate + "' " +
                                "ORDER BY TRANSID LIMIT 1";

                Cursor txnCursor = openHelper.getReadableDatabase().query(sql);

                if(txnCursor.moveToFirst()) {
                    txnId = Long.parseLong(txnCursor.getString(txnCursor.getColumnIndexOrThrow("TRANSID")));
                }

                txnCursor.close();
            }
        }
        catch(Exception e)
        {
            Timber.e(e, "getTxnId");
        }

        return txnId;
    }

    @SuppressLint("Range")
    private static String[] getCategoryOrSubCategoryByName(String searchName)
    {
        String[] cTran = new String[]{"", ""};

        try
        {
            if(!searchName.trim().isEmpty()) {

                String sql =
                        "SELECT CATEGID, CATEGNAME, PARENTID FROM CATEGORY_V1  " +
                                "WHERE CATEGNAME = '" + searchName + "' " +
                                "ORDER BY PARENTID desc, CATEGNAME asc  LIMIT 1";

                //Log.d("SQL", sql);
                Cursor cCursor = openHelper.getReadableDatabase().query(sql);

                if(cCursor.moveToFirst()) {
                    cTran = new String[]{
                            cCursor.getString(cCursor.getColumnIndexOrThrow("CATEGID")),
                            cCursor.getString(cCursor.getColumnIndexOrThrow("PARENTID"))
                    };
                }

                cCursor.close();
            }
        }
        catch(Exception e)
        {
            Timber.e(e, "getCategoryOrSubCategoryByName");
        }

        return cTran;
    }

    @SuppressLint("Range")
    private static void getAccountDetails(String[] reqMatch)
    {
        String[] accountDetails = new String[]{"", "", "", "", "", "", ""};

        try
        {
            for(int j=0; j<=reqMatch.length-1; j++) {
                if (reqMatch[j] != "") {

                    accountDetails = new String[] {"", "", "", "", "", "", reqMatch[j] };

                    String sql =
                            "SELECT A.ACCOUNTID, A.ACCOUNTNAME, A.ACCOUNTNUM, A.CURRENCYID, " +
                                    "C.CURRENCY_SYMBOL, C.DECIMAL_POINT, C.GROUP_SEPARATOR " +
                                    "FROM ACCOUNTLIST_V1 A " +
                                    "INNER JOIN CURRENCYFORMATS_V1 C ON C.CURRENCYID = A.CURRENCYID " +
                                    "WHERE A.STATUS='Open' AND A.ACCOUNTNUM LIKE '%" + reqMatch[j] + "%' " +
                                    "ORDER BY A.ACCOUNTID " +
                                    "LIMIT 1";

                    Cursor accountCursor = openHelper.getReadableDatabase().query(sql);

                    if(accountCursor.moveToFirst()) {
                        accountDetails = new String[] {
                                accountCursor.getString(accountCursor.getColumnIndexOrThrow("ACCOUNTID")),
                                accountCursor.getString(accountCursor.getColumnIndexOrThrow("ACCOUNTNAME")),
                                accountCursor.getString(accountCursor.getColumnIndexOrThrow("CURRENCYID")),
                                accountCursor.getString(accountCursor.getColumnIndexOrThrow("CURRENCY_SYMBOL")),
                                accountCursor.getString(accountCursor.getColumnIndexOrThrow("DECIMAL_POINT")),
                                accountCursor.getString(accountCursor.getColumnIndexOrThrow("GROUP_SEPARATOR")),
                                reqMatch[j]
                        };
                    }

                    switch (j)
                    {
                        case 0: //from account
                            fromAccountDetails = accountDetails;
                            break;
                        case 1: //to account
                            toAccountDetails = accountDetails;
                            break;
                    }

                    accountCursor.close();
                }
            }
        }
        catch(Exception e)
        {
            Timber.e(e, "getAccountDetails");
        }
    }

    public String validateData() {

        if (mCommon.transactionEntity.getAccountId().equals(Constants.NOT_SET)) {
            return mContext.getString(R.string.error_fromaccount_not_selected);
        }

        // Amount is required and must be positive. Sign is determined by transaction type.
        if (mCommon.transactionEntity.getAmount().toDouble() <= 0 ) {
            return mContext.getString(R.string.error_amount_must_be_positive);
        }

        if (mCommon.transactionEntity.getTransactionType().equals(TransactionTypes.Transfer)) {

            if (mCommon.transactionEntity.getToAccountId().equals(Constants.NOT_SET)) {
                return mContext.getString(R.string.error_toaccount_not_selected);
            }

            if (mCommon.transactionEntity.getToAccountId().equals(mCommon.transactionEntity.getAccountId())) {
                return mContext.getString(R.string.error_transfer_to_same_account);
            }

            // Amount To is required and has to be positive.
            if (mCommon.transactionEntity.getToAmount().toDouble() <= 0 ) {
                return mContext.getString(R.string.error_amount_must_be_positive);
            }
        } else { // payee required for automatic transactions.
            if (!mCommon.transactionEntity.hasPayee()) {
                return mContext.getString(R.string.error_payee_not_selected);
            }
        }

        // Category is required if tx is not a split or transfer.
        if (!mCommon.transactionEntity.hasCategory()) {
            return mContext.getString(R.string.error_category_not_selected);
        }

        return "PASS";
    }

    public String saveTransaction() {

        AccountTransactionRepository repo = new AccountTransactionRepository(mContext);

        if (!mCommon.transactionEntity.hasId()) { // insert
            mCommon.transactionEntity = repo.insert((AccountTransaction) mCommon.transactionEntity);

            if (!mCommon.transactionEntity.hasId()) { //Insert new transaction failed!
                return mContext.getString(R.string.db_checking_insert_failed);
            }
        } else { // update
            if (!repo.update((AccountTransaction) mCommon.transactionEntity)) { //Update transaction failed!
                return mContext.getString(R.string.db_checking_update_failed);
            }
        }
        return "PASS";
    }

    /**
     * Note: Check the new NotificationUtils for creation of notification channel and the code that
     * utilizes it.
     * @param intent
     * @param notificationText
     */
    private void showNotification(Intent intent, String notificationText, String msgSender, String txnStatus, String errorMsg) {

        try {

            String GROUP_KEY_AMMEX = "com.android.example.MoneyManagerEx";
            int ID_NOTIFICATION =  (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, ID_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

            NotificationManager notificationManager = (NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            // Create the NotificationChannel
            NotificationChannel nChannel = new NotificationChannel(CHANNEL_ID, "AMMEXSMS", NotificationManager.IMPORTANCE_DEFAULT);
            nChannel.setDescription(mContext.getString(R.string.notification_process_sms_channel_description));

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(nChannel);

            Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(mContext.getString(R.string.notification_process_sms_transaction_status) + ": " + txnStatus + errorMsg)
                    .setSubText(mContext.getString(R.string.notification_click_to_edit_transaction))
                    .setSmallIcon(R.drawable.ic_stat_notification)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(msgSender + " : " + notificationText))
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                    .setGroup(GROUP_KEY_AMMEX)
                    .build();

            // Change the notification color based on the status
            switch(txnStatus) {
                case "Auto Failed":
                    notification.color = mContext.getResources().getColor(R.color.md_red);
                    break;  //optional
                case "Already Exists":
                    notification.color = mContext.getResources().getColor(R.color.md_indigo);
                    break;  //optional
                default:
                    notification.color = mContext.getResources().getColor(R.color.md_primary);
            }

            // notify
            notificationManager.notify(ID_NOTIFICATION, notification);

            } catch (Exception e) {
                Timber.e(e, "showing notification for sms transaction");
            }
    }
}
