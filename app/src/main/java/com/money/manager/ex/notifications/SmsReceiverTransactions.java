/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

/************** Change Logs *****************
 * Created by velmuruganc on 11/24/2017.
 *
 * Modification:
 * 2017/12/12 - velmuruganc :
 */

package com.money.manager.ex.notifications;

import java.util.Date;
import java.util.regex.*;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
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
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import androidx.core.content.ContextCompat;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

import static androidx.core.content.ContextCompat.startActivity;
import static java.lang.Integer.*;

public class SmsReceiverTransactions extends BroadcastReceiver {

    private Context mContext;

    @Inject
    BriteDatabase database;

    private EditTransactionCommonFunctions mCommon;

    /// Db setup
    public static MmxOpenHelper MmxHelper;
    public static SQLiteDatabase db;

    static String[] fromAccountDetails;
    static String[] toAccountDetails;

    public static String CHANNEL_ID = "SmsTransaction_NotificationChannel";
    private static final int ID_NOTIFICATION = 0x000A;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context.getApplicationContext();

        final BehaviourSettings behav_settings = new BehaviourSettings(mContext);
        final GeneralSettings gen_settings = new GeneralSettings(mContext);
        final AppSettings app_settings = new AppSettings(mContext);
        final PreferenceConstants prf_const = new PreferenceConstants();

        //App Settings
        int baseCurencyID, fromCurrencyID, toCurrencyID;
        int baseAccountID, fromAccountID, toAccountID;

        String baseCurrencySymbl, fromAccCurrencySymbl, toAccCurrencySymbl;
        String baseAccountName, fromAccountName, toAccountName;

        Boolean autoTransactionStatus = false;
        Boolean skipSaveTrans = false;

        try {
            //------- if settings enabled the parse the sms and create trans ---------------
            if (behav_settings.getBankSmsTrans() == true) {

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
                        msgBody += msgs[i].getMessageBody().toString();
                    }

                    //msgSender = "AT-SIBSMS";

                    if(isTransactionSms(msgSender)) {
                        // Transaction Sms sender will have format like this AT-SIBSMS,
                        // Promotional sms will have sender like AT-012345
                        // Not sure how this format will be in out side of India. I may need to update if I get sample

                        ITransactionEntity model = AccountTransaction.create();
                        mCommon = new EditTransactionCommonFunctions(null, model, database);

                        // find out the trans type using reg ex
                        String[] key_credit_search = {"(credited)", "(received)", "(added)", "(reloaded)", "(deposited)", "(refunded)",
                                "(debited)(.*?)(towards)(\\s)", "(\\s)(received)(.*?)(in(\\s)your)(\\s)", "(sent)(.*?)(to)(\\s)", "(debited)(.*?)(to)(\\s)",
                                "(credited)(.*?)(in)(\\s)", "(credited)(.*?)(to)(\\s)"};

                        // - Sales Draft added for LBP currency. Request from HussienH
                        String[] key_debit_search = {"(made)", "(debited)", "(using)", "(paid)", "(purchase)", "(withdrawn)", "(done)",
                                "(credited)(.*?)(from)(\\s)", "(sent)(.*?)(from)(\\s)", "(\\s)(received)(.*?)(from)(\\s)",
                                "(Sales\\sDraft)", "(transferred to)"}; //

                        String transType = "";

                        //Handle the string
                        msgBody = msgBody.replaceAll("[\\t\\n\\r]+"," ");
                        msgBody = msgBody.replaceAll("  "," ");

                        Boolean isDeposit = validateTransType(key_credit_search, msgBody.toLowerCase());
                        Boolean isWithdrawal = validateTransType(key_debit_search, msgBody.toLowerCase());

                        if (isDeposit == true)
                        {
                            if (isWithdrawal == true)
                            {
                                transType = "Transfer";
                                String[] transCategory = getCategoryOrSubCategoryByName("Transfer");

                                if (!transCategory[0].isEmpty()) {
                                    mCommon.transactionEntity.setCategoryId(parseInt(transCategory[0]));
                                }

                                if (!transCategory[1].isEmpty()) {
                                    mCommon.transactionEntity.setSubcategoryId(parseInt(transCategory[1]));
                                }

                                mCommon.transactionEntity.setTransactionType(TransactionTypes.Transfer);

                            } else {
                                transType = "Deposit";
                                String[] incomeCategory = getCategoryOrSubCategoryByName("Income");

                                if (!incomeCategory[0].isEmpty()) {
                                    mCommon.transactionEntity.setCategoryId(parseInt(incomeCategory[0]));
                                }

                                if (!incomeCategory[1].isEmpty()) {
                                    mCommon.transactionEntity.setSubcategoryId(parseInt(incomeCategory[1]));
                                }

                                mCommon.transactionEntity.setTransactionType(TransactionTypes.Deposit);
                            }

                        } else if (isWithdrawal == true) {
                            transType = "Withdrawal";
                            mCommon.transactionEntity.setTransactionType(TransactionTypes.Withdrawal);
                        }

                        mCommon.transactionEntity.setStatus("");
                        mCommon.payeeName = "";

                        if (transType != "" && msgBody.toLowerCase().contains("otp") == false) { // if not from blank, then nothing to do with sms

                            //Create the intent that’ll fire when the user taps the notification//
                            Intent t_intent = new Intent(mContext, CheckingTransactionEditActivity.class);

                            // Db setup
                            MmxHelper = new MmxOpenHelper(mContext, app_settings.getDatabaseSettings().getDatabasePath());
                            db = MmxHelper.getReadableDatabase();

                            baseCurencyID = gen_settings.getBaseCurrencytId();
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
                                fromAccountID = parseInt(fromAccountDetails[0]);
                                fromAccountName = fromAccountDetails[1];
                                fromCurrencyID = parseInt(fromAccountDetails[2]);
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

                                //set the ref no. if exists
                                if(!transRefNo.isEmpty()){
                                    mCommon.transactionEntity.setTransactionNumber(transRefNo);
                                }

                                int txnId = getTxnId(transRefNo.trim(), mCommon.transactionEntity.getDateString());

                                switch (txnId)
                                {
                                    case 0: //add new trnsaction

                                        if (transType == "Transfer") //if it is transfer
                                        {
                                            if (!toAccountDetails[0].isEmpty()) // if id exists then considering as account transfer
                                            {
                                                toAccountID = parseInt(toAccountDetails[0]);
                                                toAccountName = toAccountDetails[1];
                                                toCurrencyID = parseInt(toAccountDetails[2]);
                                                toAccCurrencySymbl = toAccountDetails[3];

                                                mCommon.transactionEntity.setAccountToId(toAccountID);

                                                //convert the to amount from the both currency details
                                                CurrencyService currencyService = new CurrencyService(mContext);
                                                mCommon.transactionEntity.setAmountTo(currencyService.doCurrencyExchange(fromCurrencyID,
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
                                            mCommon.transactionEntity.setAccountToId(Constants.NOT_SET);
                                            mCommon.transactionEntity.setAmountTo(MoneyFactory.fromString(transAmount));

                                            mCommon.transactionEntity.setPayeeId(parseInt(transPayee[0]));
                                            mCommon.payeeName = transPayee[1];
                                            mCommon.transactionEntity.setCategoryId(parseInt(transPayee[2]));
                                            mCommon.transactionEntity.setSubcategoryId(parseInt(transPayee[3]));
                                        }

                                        t_intent.setAction(Intent.ACTION_INSERT); //Set the action

                                        break;

                                    default: //Update existing transaction

                                        transType = "Transfer";

                                        AccountTransactionRepository repo = new AccountTransactionRepository(mContext);
                                        AccountTransaction txn = repo.load(txnId);

                                        if (txn != null) {

                                            if (txn.getTransactionType() != TransactionTypes.Transfer) {

                                                AccountRepository accountRepository = new AccountRepository(mContext);

                                                if(txn.getTransactionType() == TransactionTypes.Deposit)
                                                {
                                                    toAccountID = txn.getAccountId();
                                                    toCurrencyID = accountRepository.loadCurrencyIdFor(txn.getAccountId());
                                                }
                                                else
                                                {
                                                    toAccountID = fromAccountID;
                                                    toCurrencyID = fromCurrencyID;
                                                    fromCurrencyID = accountRepository.loadCurrencyIdFor(txn.getAccountId());
                                                }

                                                mCommon.transactionEntity = txn;
                                                mCommon.transactionEntity.setTransactionType(TransactionTypes.Transfer);
                                                mCommon.transactionEntity.setAccountId(fromAccountID);
                                                mCommon.transactionEntity.setAccountToId(toAccountID);

                                                //convert the to amount from the both currency details
                                                CurrencyService currencyService = new CurrencyService(mContext);
                                                mCommon.transactionEntity.setAmountTo(currencyService.doCurrencyExchange(fromCurrencyID,
                                                        mCommon.transactionEntity.getAmount(),
                                                        toCurrencyID));

                                                mCommon.transactionEntity.setPayeeId(Constants.NOT_SET);

                                                String[] transCategory = getCategoryOrSubCategoryByName("Transfer");
                                                if (!transCategory[0].isEmpty()) {
                                                    mCommon.transactionEntity.setCategoryId(parseInt(transCategory[0]));
                                                    mCommon.transactionEntity.setSubcategoryId(parseInt(transCategory[1]));
                                                }

                                                mCommon.transactionEntity.setNotes(mCommon.transactionEntity.getNotes() + "\n\n" + msgBody);

                                                t_intent.setAction(Intent.ACTION_EDIT); //Set the action
                                            }
                                            else //if transfer already exists, then do nothing
                                            {
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
                                        + "Sub Category ID = " + transPayee[3] + "\n"
                                        + "Trans Ref No. = " + transRefNo + "\n"
                                        + "Trans Type = " + transType + "\n";

                                //Must be commented for released version
                                //mCommon.transactionEntity.setNotes(strExtracted);

                                // Set the content for a transaction);
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_SOURCE, "SmsReceiverTransactions.java");
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_ID, mCommon.transactionEntity.getId());
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_ACCOUNT_ID, String.valueOf(mCommon.transactionEntity.getAccountId()));
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TO_ACCOUNT_ID, String.valueOf(mCommon.transactionEntity.getAccountToId()));
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_CODE, mCommon.getTransactionType());
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_PAYEE_ID, String.valueOf(mCommon.transactionEntity.getPayeeId()));
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_PAYEE_NAME, mCommon.payeeName);
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_CATEGORY_ID, String.valueOf(mCommon.transactionEntity.getCategoryId()));
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_SUBCATEGORY_ID, String.valueOf(mCommon.transactionEntity.getSubcategoryId()));
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_AMOUNT, String.valueOf(mCommon.transactionEntity.getAmount()));
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_NOTES, mCommon.transactionEntity.getNotes());
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_DATE, new MmxDate().toDate());
                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_NUMBER, mCommon.transactionEntity.getTransactionNumber());

                                // validate and save the transaction
                                if(skipSaveTrans == false) {
                                    if (validateData()) {
                                        if (saveTransaction()) {

                                            autoTransactionStatus = true;

                                            if (behav_settings.getSmsTransStatusNotification()==true)
                                            {
                                                t_intent.setAction(Intent.ACTION_EDIT);
                                                t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_ID, mCommon.transactionEntity.getId());

                                                showNotification(t_intent, msgBody, msgSender, "Successful");
                                            }
                                            else
                                            {
                                                Toast.makeText(context, "MMEX: Bank Transaction Processed for: \n\n" + strExtracted, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        else
                                        {
                                            if (behav_settings.getSmsTransStatusNotification()==true)
                                            { showNotification(t_intent, msgBody, msgSender, "Save Failed"); }
                                            else
                                            { startActivity(mContext, t_intent, null); }
                                        }
                                    }

                                    //if transaction is not created automatically, then invoke notification or activity screen
                                    if (autoTransactionStatus == false) {

                                        if (behav_settings.getSmsTransStatusNotification()==true)
                                        { showNotification(t_intent, msgBody, msgSender, "Auto Failed"); }
                                        else
                                        { startActivity(mContext, t_intent, null); }
                                    }
                                }
                                else
                                {
                                    if (behav_settings.getSmsTransStatusNotification()==true)
                                    {
                                        showNotification(t_intent, "MMEX: Skiping Bank Transaction updates SMS, because transaction exists with ref. no. " + transRefNo, msgSender, "Already Exists");
                                    }
                                    else
                                    {
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

    private static String getCurrencySymbl(int currencyID)
    {
        //Get the currency sysmbl
        String currencySymbl = "";
        String reqCurrFields[] = {"CURRENCYID", "DECIMAL_POINT", "GROUP_SEPARATOR",  "CURRENCY_SYMBOL"};

        try
        {
            Cursor currencyCursor = db.query("CURRENCYFORMATS_V1", reqCurrFields, "CURRENCYID = ?",
                    new String[] { String.valueOf(currencyID)}, null, null, null );

            if(currencyCursor.moveToFirst()) {
                currencySymbl = currencyCursor.getString(currencyCursor.getColumnIndex("CURRENCY_SYMBOL"));
            }

            currencyCursor.close();
        }
        catch(Exception e)
        {
            Timber.e(e, "getCurrencySymbl");
        }

        return  currencySymbl;

    }

    private static boolean isTransactionSms(String smsSender)
    {
        boolean reqMatch = false;

        try
        {
            Pattern p = Pattern.compile("(-?[a-zA-Z]+)");
            Matcher m = p.matcher(smsSender);

            if (m != null)
            {
                while(m.find())
                {
                    reqMatch = true;
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
            for(int i=0; i<=keySearch.length-1; i++)
            {
                Pattern p = Pattern.compile(keySearch[i]);
                Matcher m = p.matcher(smsMsg);

                if (m != null && reqMatch == false)
                {
                    while(m.find())
                    {
                        reqMatch = true;
                        break;
                    }
                }

                // Come out from the loop if reqMatch is already set
                if (reqMatch == true)
                {
                    break;
                }
            }
        }
        catch(Exception e)
        {
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

            switch (String.valueOf(isUPI))
            {
                case "false": //find the match for non UPI transfer or credit or debit

                    for(int j=0; j<=mIndx.length-1; j++)
                    {
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
            }

            getAccountDetails(reqMatch);

        }
        catch(Exception e)
        {
            Timber.e(e, "extractAccountDetails");
        }
    }

    private static String searchForAccountNum(String smsMsg, int mIndx)
    {
        String reqMatch =  "";

        // - ((\s)using\scard\s(.*?)\s.emaining) added for LBP currency. Request from HussienH
        String[] searchFor =
                {
                        "((\\s)?((\\d+)?[X]+(\\d+))(\\s)?)", "((\\s)?((\\d+)?[x]+(\\d+))(\\s)?)", "((\\s)?((\\d+)?[\\*]+(\\d+))(\\s)?)",
                        "((\\s)?Account\\s?No(.*?)\\s?(\\d+)(\\s)?)", "((\\s)?A/.\\s?No(.*?)\\s?(\\d+)(\\s)?)",
                        "[N-n][O-o](.)?(:)?(\\s)?'(.*?)'", "((\\s)using\\scard\\s(.*?)\\s.emaining)",
                        "([\\(]((.*?)[@](.*?))[\\)])", "(from\\s((.*?)@(.*?))[.]?[\\s])",
                        "(to\\s((.*?)@(.*?))[.]?[\\s])", "(linked((.*?)@(.*?))[.])",
                        "((\\s)[V//v]irtual(\\s)([A//a]ddress)?(I[D//d])?((.*?)@(.*?))(\\s))", "(your\\s(.*?)\\s+using)",
                        "([\\[](\\d+)[\\]])", "(using(.*?)(\\.))", "(.ay.m\\s.allet)"
                };

        int[] getGroup =
                {
                        5, 5, 5,
                        4, 4,
                        4, 3,
                        2, 2,
                        2, 2,
                        6, 2,
                        2, 2, 1
                };

        int mFound;

        try
        {
            for(int i=0; i<=searchFor.length-1; i++)
            {
                mFound = 1;

                Pattern p = Pattern.compile(searchFor[i]);
                Matcher m = p.matcher(smsMsg);

                if (m != null && reqMatch.isEmpty())
                {
                    while(m.find())
                    {
                        if(mFound == mIndx)
                        {
                            // Append X with acc no, bcz start with X for non UPI trans
                            if (m.group(getGroup[i]).trim().matches("\\d+") == true &&
                                    m.group(getGroup[i]).trim().matches("[a-zA-Z@]+") == false )
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

    private static String extractTransAmount(int indexOfAmt, String smsMsg, String fromAccCurrencySymbl)
    {
        String reqMatch = "";
        smsMsg = smsMsg.replace(",", "");
        String searchFor = "((\\s)?##SEARCH4CURRENCY##(\\s)?((\\d+)(\\.\\d+)?))";
        int[] getGroup = {4};
        int indx = 0;

        //Handle multiple symbol for currency
        String[] searchCurrency;

        if (fromAccCurrencySymbl.contentEquals("INR")) {
            searchCurrency = new String[]{"INR", "Rs.", "Rs"};
        } else {
            searchCurrency = new String[]{fromAccCurrencySymbl};
        }

        try
        {
            for(int i=0; i<=searchCurrency.length-1; i++)
            {
                Pattern p = Pattern.compile(searchFor.replace("##SEARCH4CURRENCY##", searchCurrency[i]));
                Matcher m = p.matcher(smsMsg);

                if (m != null && reqMatch.isEmpty())
                {
                    while(m.find())
                    {
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
                "((\\s)at\\s(.*?)\\s+on)", "((\\s)favoring\\s(.*?)\\s+is)",
                "((\\s)to\\s(.*?)\\s+at)", "((\\s)to\\s(.*?)[.])",
                "((\\s)at\\s(.*?)[.])", "([\\*](.*?)[.])",
                "((\\s)FROM\\s(.*?)\\s+\\d)", "(from\\s(.*?)\\s(\\())", "(([a-zA-Z]+)(\\s)has(\\s)added)",
                "((\\s)for\\s(.*?)\\s+for)", "((\\s)at\\s(.*?)\\s+using)", "((\\s)paid\\s(.*?)\\s)",
                "(Info:\\sMOB//?(\\w+))", "((\\s)at\\s(.*?)\\s)"};

        int[] getGroup = {3, 3, 3, 3, 3, 2, 3, 2, 2, 3, 3, 3, 2, 3};
        String[] reqMatch = new String[]{"", "", "", ""};

        try
        {
            for(int i=0; i<=searchFor.length-1; i++)
            {
                Pattern p = Pattern.compile(searchFor[i]);
                Matcher m = p.matcher(smsMsg);

                if (m != null && reqMatch[0].isEmpty())
                {
                    while(m.find())
                    {
                        reqMatch = getPayeeDetails(String.valueOf(m.group(getGroup[i]).trim()));

                        if(!reqMatch[0].isEmpty()){
                            break;
                        }
                    }
                }

                // Come out from the loop if reqMatch is already set
                if (!reqMatch[0].isEmpty())
                {
                    break;
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
        String[] searchFor =
                {
                        "([C//c]heque\\s[N//n]o[.*?](\\d+))", "([R//r]ef(\\s)?[N//n]o(:?.?=?)(\\s)?(\\d+))", "(\\s(\\d+(.*?)\\d+)TXN\\s)",
                        "(([R//r]eference:)(\\s)?(\\d+))",  "([\\*](\\d+)[\\*])", "([I//i]nfo(\\s)?(:?)(.*?)(\\d+)+[:]?[-]?)",
                        "(([R//r]eference [N//n]umber)(.*?)(\\d+))", "(\\s)?#(\\s?)(\\d+)(\\s?)", "(\\/+(\\d+)+\\/)",
                        "(I[D//d](.?:?-?)(\\s)?((.*?)\\w+))", "(I[D//d](.?:?))(\\s)?(\\d+)", "(I[D//d](\\s)is(\\s)?(:?-?)(\\d+))"
                };
        int[] getGroup =
                {
                        2, 5, 2,
                        4, 2, 5,
                        4, 3, 2,
                        4, 4, 4
                };

        try
        {
            for(int i=0; i<=searchFor.length-1; i++)
            {
                Pattern p = Pattern.compile(searchFor[i]);
                Matcher m = p.matcher(smsMsg);

                if (m != null && reqMatch.isEmpty())
                {
                    while(m.find())
                    {
                        reqMatch = m.group(getGroup[i]).trim();
                        break;
                    }
                }

                // Come out from the loop if reqMatch is already set
                if (!reqMatch.isEmpty())
                {
                    break;
                }

            }
        }
        catch(Exception e)
        {
            Timber.e(e, "extractTransRefNo");
        }

        return reqMatch;
    }

    private static String[] getPayeeDetails(String payeeName)
    {
        String[] payeeDetails = new String[]{"", payeeName.trim(), "", ""};

        try
        {
            if(!payeeName.trim().isEmpty()) {

                String sql = "SELECT PAYEEID, PAYEENAME, CATEGID, SUBCATEGID " +
                        "FROM PAYEE_V1 " +
                        "WHERE PAYEENAME LIKE '%" + payeeName + "%' " +
                        "ORDER BY PAYEENAME LIMIT 1";

                Cursor payeeCursor = db.rawQuery(sql, null);

                if(payeeCursor.moveToFirst())
                {
                    payeeDetails = new String[] {
                            payeeCursor.getString(payeeCursor.getColumnIndex("PAYEEID")),
                            payeeCursor.getString(payeeCursor.getColumnIndex("PAYEENAME")),
                            payeeCursor.getString(payeeCursor.getColumnIndex("CATEGID")),
                            payeeCursor.getString(payeeCursor.getColumnIndex("SUBCATEGID"))
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

    private static Integer getTxnId(String refNumber, String transDate)
    {
        int txnId = 0;

        try
        {
            if(!refNumber.trim().isEmpty()) {

                String sql =
                        "SELECT TRANSID " +
                                "FROM CHECKINGACCOUNT_V1 " +
                                "WHERE TRANSACTIONNUMBER  LIKE '%" + refNumber + "%' " +
                                "AND TRANSDATE ='" + transDate + "' " +
                                "ORDER BY TRANSID LIMIT 1";

                Cursor txnCursor = db.rawQuery(sql, null);

                if(txnCursor.moveToFirst())
                {
                    txnId = parseInt(txnCursor.getString(txnCursor.getColumnIndex("TRANSID")));
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

    private static String[] getCategoryOrSubCategoryByName(String searchName)
    {
        String[] cTran = new String[]{"", ""};

        try
        {
            if(!searchName.trim().isEmpty()) {

                String sql =
                        "SELECT c.CATEGID, c.CATEGNAME, s.SUBCATEGID, s.SUBCATEGNAME " +
                                "FROM CATEGORY_V1 c  " +
                                "INNER JOIN SUBCATEGORY_V1 s ON s.CATEGID=c.CATEGID " +
                                "WHERE s.SUBCATEGNAME = '" + searchName + "' " +
                                "ORDER BY s.SUBCATEGID  LIMIT 1";

                Cursor cCursor = db.rawQuery(sql, null);

                if(cCursor.moveToFirst())
                {
                    cTran = new String[]{
                            cCursor.getString(cCursor.getColumnIndex("CATEGID")),
                            cCursor.getString(cCursor.getColumnIndex("SUBCATEGID"))
                    };
                } else{ //search in only catogery

                    sql =
                            "SELECT c.CATEGID, c.CATEGNAME " +
                                    "FROM CATEGORY_V1 c  " +
                                    "WHERE c.CATEGNAME = '" + searchName + "' " +
                                    "ORDER BY c.CATEGID  LIMIT 1";

                    cCursor = db.rawQuery(sql, null);

                    if(cCursor.moveToFirst())
                    {
                        cTran = new String[]{
                                cCursor.getString(cCursor.getColumnIndex("CATEGID")),
                                "-1"
                        };
                    }
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

    private static void getAccountDetails(String[] reqMatch)
    {
        String[] accountDetails = new String[]{"", "", "", "", "", "", ""};

        try
        {
            for(int j=0; j<=reqMatch.length-1; j++)
            {
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

                    Cursor accountCursor = db.rawQuery(sql, null);

                    if(accountCursor.moveToFirst())
                    {
                        accountDetails = new String[] {
                                accountCursor.getString(accountCursor.getColumnIndex("ACCOUNTID")),
                                accountCursor.getString(accountCursor.getColumnIndex("ACCOUNTNAME")),
                                accountCursor.getString(accountCursor.getColumnIndex("CURRENCYID")),
                                accountCursor.getString(accountCursor.getColumnIndex("CURRENCY_SYMBOL")),
                                accountCursor.getString(accountCursor.getColumnIndex("DECIMAL_POINT")),
                                accountCursor.getString(accountCursor.getColumnIndex("GROUP_SEPARATOR")),
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

    public boolean validateData() {

        boolean isTransfer = mCommon.transactionEntity.getTransactionType().equals(TransactionTypes.Transfer);
        Core core = new Core(mContext);

        if (mCommon.transactionEntity.getAccountId() == Constants.NOT_SET) {
            //Toast.makeText(mContext, "MMEX : " + (R.string.error_toaccount_not_selected), Toast.LENGTH_LONG).show();
            return false;
        }

        if (isTransfer) {
            if (mCommon.transactionEntity.getAccountToId() == Constants.NOT_SET) {
                //Toast.makeText(mContext, "MMEX : " + (R.string.error_toaccount_not_selected), Toast.LENGTH_LONG).show();
                return false;
            }
            if (mCommon.transactionEntity.getAccountToId().equals(mCommon.transactionEntity.getAccountId())) {
                //Toast.makeText(mContext, "MMEX : " + (R.string.error_transfer_to_same_account), Toast.LENGTH_LONG).show();
                return false;
            }

            // Amount To is required and has to be positive.
            if (this.mCommon.transactionEntity.getAmountTo().toDouble() <= 0) {
                //Toast.makeText(mContext, "MMEX : " + (R.string.error_amount_must_be_positive), Toast.LENGTH_LONG).show();
                return false;
            }
        } else{

            // payee required for automatic transactions.
            if (!mCommon.transactionEntity.hasPayee()) {
                //Toast.makeText(mContext, "MMEX : " + (R.string.error_amount_must_be_positive), Toast.LENGTH_LONG).show();
                return false;
            }

        }

        // Amount is required and must be positive. Sign is determined by transaction type.
        if (mCommon.transactionEntity.getAmount().toDouble() <= 0) {
            //Toast.makeText(mContext, "MMEX : " + (R.string.error_amount_must_be_positive), Toast.LENGTH_LONG).show();
            return false;
        }

        // Category is required if tx is not a split or transfer.
        boolean hasCategory = mCommon.transactionEntity.hasCategory();
        if (!hasCategory && !isTransfer) {
            //Toast.makeText(mContext, "MMEX : " + (R.string.error_category_not_selected), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    public boolean saveTransaction() {

        AccountTransactionRepository repo = new AccountTransactionRepository(mContext);

        if (!mCommon.transactionEntity.hasId()) {
            // insert
            mCommon.transactionEntity = repo.insert((AccountTransaction) mCommon.transactionEntity);

            if (!mCommon.transactionEntity.hasId()) {
                Toast.makeText(mContext, R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
                Timber.w("Insert new transaction failed!");
                return false;
            }
        } else {
            // update
            boolean updated = repo.update((AccountTransaction) mCommon.transactionEntity);
            if (!updated) {
                Toast.makeText(mContext, R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                Timber.w("Update transaction failed!");
                return false;
            }
        }
        return true;
    }

    /**
     * Note: Check the new NotificationUtils for creation of notification channel and the code that
     * utilizes it.
     * @param intent
     * @param notificationText
     */
    private void showNotification(Intent intent, String notificationText, String msgSender, String txnStatus) {

        try {

            String GROUP_KEY_AMMEX = "com.android.example.MoneyManagerEx";
            int ID_NOTIFICATION =  (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, ID_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManager notificationManager = (NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the NotificationChannel
                NotificationChannel nChannel = new NotificationChannel(CHANNEL_ID, "AMMEXSMS", NotificationManager.IMPORTANCE_DEFAULT);
                nChannel.setDescription(mContext.getString(R.string.notification_process_sms_channel_description));

                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                notificationManager.createNotificationChannel(nChannel);
            }

            Notification notification = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(mContext.getString(R.string.notification_process_sms_transaction_status) + ": " + txnStatus)
                    .setSubText(mContext.getString(R.string.notification_click_to_edit_transaction))
                    .setSmallIcon(R.drawable.ic_stat_notification)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(msgSender + " : " + notificationText))
                    .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                    .setGroup(GROUP_KEY_AMMEX)
                    .build();

            // Change the notification color based on the status
            switch(txnStatus){
                case "Auto Failed":
                    notification.color = ContextCompat.getColor(mContext, R.color.md_red);
                    break;  //optional
                case "Already Exists":
                    notification.color = ContextCompat.getColor(mContext, R.color.md_indigo);
                    break;  //optional
                default:
                    notification.color = ContextCompat.getColor(mContext, R.color.md_primary);
            }

            // notify
            notificationManager.notify(ID_NOTIFICATION, notification);

        } catch (Exception e) {
            Timber.e(e, "showing notification for sms transaction");
        }
    }
}
