/*
 * Copyright (C) 2012-2017 The Android Money Manager Ex Project Team
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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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

import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

import static android.support.v4.content.ContextCompat.startActivity;
import static java.lang.Integer.*;

public class SmsReceiverTransactions extends BroadcastReceiver {

    private Context mContext;

    @Inject
    BriteDatabase database;

    private EditTransactionCommonFunctions mCommon;

    /// Db setup
    public static MmxOpenHelper MmxHelper;
    public static SQLiteDatabase db;

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

        try {
            //------- if settings enabled the parse the sms and create trans ---------------
            if (behav_settings.getBankSmsTrans() == true) {

                //---get the SMS message passed in---
                Bundle bundle = intent.getExtras();
                SmsMessage[] msgs = null;
                String msgBody = "";
                String msgSender = "";

                if (bundle != null) {
                    //---retrieve the SMS message received---
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];

                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        msgSender = msgs[i].getOriginatingAddress();
                        msgBody += msgs[i].getMessageBody().toString();
                    }

                    ITransactionEntity model = AccountTransaction.create();
                    mCommon = new EditTransactionCommonFunctions(null, model, database);

                    // find out the trans type using reg ex
                    String[] key_credit_search = {"(credited)", "(received)", "(added)", "(reloaded)", "(deposited)"};
                    String[] key_debit_search = {"(made)", "(debited)", "(using)", "(paid)", "(purchase)", "(withdrawn)"};
                    String transType = "";

                    Boolean isDeposit = validateTransType(key_credit_search, msgBody.toLowerCase());
                    Boolean isWithdrawal = validateTransType(key_debit_search, msgBody.toLowerCase());

                    if (isDeposit == true) {
                        if (isWithdrawal == true) {
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

                        mCommon.payeeName = "";

                    } else if (isWithdrawal == true) {
                        transType = "Withdrawal";
                        mCommon.transactionEntity.setTransactionType(TransactionTypes.Withdrawal);
                    }

                    mCommon.transactionEntity.setStatus("");

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
                        String[] fromAccountDetails = extractAccountDetails(msgBody, 1);

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
                        String transAmount = extractTransAmount(msgBody, fromAccCurrencySymbl);

                        //if no ac no (like XXXX1234) and no amt, then this is not valid sms to do transaction
                        if (!fromAccountDetails[6].isEmpty() && !transAmount.isEmpty()) {
                            mCommon.transactionEntity.setAmount(MoneyFactory.fromString(transAmount));

                            String[] transPayee = extractTransPayee(msgBody);
                            String transRefNo = extractTransRefNo(msgBody);

                            int txnId = getTxnId(transRefNo.trim(), transType, mCommon.transactionEntity.getDateString());

                            switch (txnId) {
                                case 0: //add new trnsaction
                                    //if it is transfer
                                    if (transType == "Transfer")
                                    {
                                        //get the to account details
                                        String[] toAccountDetails = extractAccountDetails(msgBody, 2);

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

                                        } else { // if not, then IMPS transfer tp 3rd party

                                            transType = "Withdrawal";
                                            mCommon.transactionEntity.setTransactionType(TransactionTypes.Withdrawal);
                                            mCommon.transactionEntity.setAccountToId(Constants.NOT_SET);
                                            mCommon.transactionEntity.setAmountTo(MoneyFactory.fromString(transAmount));

                                            //if there is no to account found from mmex db, then check for payee
                                            //This will helps me to handle 3rd party transfer thru IMPS
                                            if (!toAccountDetails[6].isEmpty() && transPayee[0].isEmpty()) {
                                                transPayee = getPayeeDetails(toAccountDetails[6].trim());
                                            }
                                        }
                                    }
                                    else
                                    {
                                        mCommon.transactionEntity.setAccountToId(Constants.NOT_SET);
                                        mCommon.transactionEntity.setAmountTo(MoneyFactory.fromString(transAmount));
                                    }

                                    if (!transPayee[0].isEmpty()) { //if payee not found then use the last payee

                                        mCommon.transactionEntity.setPayeeId(parseInt(transPayee[0]));
                                        mCommon.payeeName = transPayee[1];
                                        mCommon.transactionEntity.setCategoryId(parseInt(transPayee[2]));
                                        mCommon.transactionEntity.setSubcategoryId(parseInt(transPayee[3]));
                                    } else {
                                        mCommon.payeeName = "";
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

                                            toAccountID = fromAccountID;
                                            toCurrencyID = fromCurrencyID;
                                            fromCurrencyID = accountRepository.loadCurrencyIdFor(txn.getAccountId());

                                            mCommon.transactionEntity = txn;
                                            mCommon.transactionEntity.setTransactionType(TransactionTypes.Transfer);
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
                                        }

                                    }

                                    t_intent.setAction(Intent.ACTION_EDIT); //Set the action
                            }

                            // Capture the details the for Toast
                            String strExtracted = "Account = " + fromAccountName + "-" + fromAccountDetails[6] + "\n"
                                    + "Trans Amt = " + fromAccCurrencySymbl + " " + transAmount + ",\n"
                                    + "Payyee Name= " + transPayee[1] + "\n"
                                    + "Category ID = " + transPayee[2] + "\n"
                                    + "Sub Category ID = " + transPayee[3] + "\n"
                                    + "Trans Ref No. = " + transRefNo + "\n"
                                    + "Trans Type = " + transType + "\n";

                            // Set the content for a transaction);
                            t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_ID, mCommon.transactionEntity.getId());
                            t_intent.putExtra(EditTransactionActivityConstants.KEY_ACCOUNT_ID, String.valueOf(mCommon.transactionEntity.getAccountId()));
                            t_intent.putExtra(EditTransactionActivityConstants.KEY_TO_ACCOUNT_ID, String.valueOf(mCommon.transactionEntity.getAccountToId()));
                            t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_CODE, mCommon.getTransactionType());
                            t_intent.putExtra(EditTransactionActivityConstants.KEY_PAYEE_ID, String.valueOf(mCommon.transactionEntity.getPayeeId()));
                            t_intent.putExtra(EditTransactionActivityConstants.KEY_PAYEE_NAME, mCommon.payeeName);
                            t_intent.putExtra(EditTransactionActivityConstants.KEY_CATEGORY_ID, String.valueOf(mCommon.transactionEntity.getCategoryId()));
                            t_intent.putExtra(EditTransactionActivityConstants.KEY_SUBCATEGORY_ID, String.valueOf(mCommon.transactionEntity.getSubcategoryId()));
                            t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_AMOUNT, String.valueOf(mCommon.transactionEntity.getAmount()));
                            t_intent.putExtra(EditTransactionActivityConstants.KEY_NOTES, strExtracted + "\n\n" + mCommon.transactionEntity.getNotes());
                            t_intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_DATE, new MmxDate().toDate());

                            if (validateData()) {
                                if (saveTransaction()) {
                                    Toast.makeText(context, "MMEX: Bank Transaction Processed for: \n\n" + strExtracted, Toast.LENGTH_LONG).show();
                                } else {
                                    startActivity(mContext, t_intent, null);
                                    //showNotification(t_intent, strExtracted);
                                }
                            } else {
                                startActivity(mContext, t_intent, null);
                                //showNotification(t_intent, strExtracted);
                            }

                            //reset the value
                            bundle = null;
                            msgs = null;
                            msgBody = "";
                            msgSender = "";
                            mCommon = null;
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            Toast.makeText(context, "MMEX: Bank Transaction Process EXCEPTION --> " +  e, Toast.LENGTH_LONG).show();
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

            if(currencyCursor.getCount() > 0) {
                currencyCursor.moveToFirst();
                currencySymbl = currencyCursor.getString(currencyCursor.getColumnIndex("CURRENCY_SYMBOL"));
            }
        }
        catch(Exception e)
        {
            //System.err.println("EXCEPTION:" + e);
        }

        return  currencySymbl;

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
            }
        }
        catch(Exception e)
        {
            //System.err.println("EXCEPTION:" + e);
        }

        return reqMatch;
    }

    private static String[] extractAccountDetails(String smsMsg, int mIndx)
    {
        String reqMatch = "";
        String[] searchFor = {"((\\s)?(\\d+)?[X]+(\\d+)\\s)", "((\\s)?(\\d+)?[x]+(\\d+)\\s)",
                "(.ay.m\\s.allet)", "(.ay.m)"};
        int[] getGroup = {4, 4, 1, 1};
        int mFound = 1;
        String[] accountDetails = new String[]{"", "", "", "", "", "", ""};

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
                            reqMatch = m.group(getGroup[i]).trim();
                            break;
                        }
                        else { mFound = mFound + 1; }
                    }
                }
            }

            if (reqMatch != "") {

                accountDetails = new String[] {"", "", "", "", "", "", reqMatch };

                String sql =
                        "SELECT A.ACCOUNTID, A.ACCOUNTNAME, A.ACCOUNTNUM, A.CURRENCYID, " +
                                "C.CURRENCY_SYMBOL, C.DECIMAL_POINT, C.GROUP_SEPARATOR " +
                                "FROM ACCOUNTLIST_V1 A " +
                                "INNER JOIN CURRENCYFORMATS_V1 C ON C.CURRENCYID = A.CURRENCYID " +
                                "WHERE A.STATU='Open' AND A.ACCOUNTNUM LIKE '%" + reqMatch + "%' " +
                                "ORDER BY A.ACCOUNTID " +
                                "LIMIT 1";

                Cursor accountCursor = db.rawQuery(sql, null);

                if(accountCursor.getCount() > 0)
                {
                    accountCursor.moveToFirst();
                    accountDetails = new String[] {
                            accountCursor.getString(accountCursor.getColumnIndex("ACCOUNTID")),
                            accountCursor.getString(accountCursor.getColumnIndex("ACCOUNTNAME")),
                            accountCursor.getString(accountCursor.getColumnIndex("CURRENCYID")),
                            accountCursor.getString(accountCursor.getColumnIndex("CURRENCY_SYMBOL")),
                            accountCursor.getString(accountCursor.getColumnIndex("DECIMAL_POINT")),
                            accountCursor.getString(accountCursor.getColumnIndex("GROUP_SEPARATOR")),
                            reqMatch
                    };
                }
            }

        }
        catch(Exception e)
        {
            //System.err.println("EXCEPTION:" + e);
        }

        return accountDetails;
    }

    private static String extractTransAmount(String smsMsg, String fromAccCurrencySymbl)
    {
        String reqMatch = "";
        smsMsg = smsMsg.replace(",", "");
        String searchFor = "((\\s)?##SEARCH4CURRENCY##(.)?(\\s)?((\\d+)(\\.\\d+)?))";
        int[] getGroup = {5};

        //Handle multiple symbol for currency
        String[] searchCurrency;

        if (fromAccCurrencySymbl.contentEquals("INR")) {
            searchCurrency = new String[]{"INR", "Rs"};
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
                        reqMatch = m.group(getGroup[0]).trim();
                        break;
                    }
                }
            }

        }
        catch(Exception e)
        {
            //System.err.println("EXCEPTION:"+e);
        }

        return reqMatch;
    }

    private static String[] extractTransPayee(String smsMsg)
    {
        String[] searchFor = {
                "((\\s)at+(.*?)\\s+on)", "((\\s)favoring+(.*?)\\s+is)",
                "((\\s)to+(.*?)\\s+at)", "((\\s)to+(.*?)[.])", "((\\s)at+(.*?)[.])", "([\\*](.*?)[.])",
                "((\\s)FROM+(.*?)\\s+\\d)", "(from\\s(.*?)\\s(\\())", "(.ay.m)"};

        int[] getGroup = {3, 3, 3, 3, 3, 2, 3, 2, 1};
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
            }
        }
        catch(Exception e)
        {
            //System.err.println("EXCEPTION:"+e);
        }

        return reqMatch;
    }

    private static String extractTransRefNo(String smsMsg)
    {
        String reqMatch = "";
        String[] searchFor = {"(IMPS+\\sRef\\s+no+\\s(\\d+))", "(\\/+(\\d+)+\\/)",
                "((ID(.)?(:)?)+(\\d+))", "((id is+\\s(:)?)+(\\d+))",
                "(Info(:)+(\\s)?[A-Z]+([-][A-Z]+)?[\\*]?(\\d+)[\\*]?[-]?[a-zA-Z]+(\\s)?[A-Za-z]+[.])",
                "(Info(:)+(\\s)?(\\d+)[:])"};
        int[] getGroup = {2, 2, 5, 4, 5, 4};

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
            }
        }
        catch(Exception e)
        {
            //System.err.println("EXCEPTION:"+e);
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

                if(payeeCursor.getCount() > 0)
                {
                    payeeCursor.moveToFirst();
                    payeeDetails = new String[] {
                            payeeCursor.getString(payeeCursor.getColumnIndex("PAYEEID")),
                            payeeCursor.getString(payeeCursor.getColumnIndex("PAYEENAME")),
                            payeeCursor.getString(payeeCursor.getColumnIndex("CATEGID")),
                            payeeCursor.getString(payeeCursor.getColumnIndex("SUBCATEGID"))
                    };
                }
            }
        }
        catch(Exception e)
        {
            //System.err.println("EXCEPTION:"+e);
        }

        return payeeDetails;
    }

    private static Integer getTxnId(String refNumber, String transType, String transDate)
    {
        int txnId = 0;

        try
        {
            if(!refNumber.trim().isEmpty()) {

                String sql =
                        "SELECT TRANSID, NOTES, TRANSDATE " +
                                "FROM CHECKINGACCOUNT_V1 " +
                                "WHERE NOTES LIKE '%" + refNumber + "%' AND TRANSCODE <> '" + transType + "' " +
                                "AND TRANSDATE ='" + transDate + "' " +
                                "ORDER BY TRANSID LIMIT 1";

                Cursor txnCursor = db.rawQuery(sql, null);

                if(txnCursor.getCount() > 0)
                {
                    txnCursor.moveToFirst();
                    txnId = parseInt(txnCursor.getString(txnCursor.getColumnIndex("TRANSID")));
                }
            }
        }
        catch(Exception e)
        {
            //System.err.println("EXCEPTION:"+e);
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

                if(cCursor.getCount() > 0)
                {
                    cCursor.moveToFirst();
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

                    if(cCursor.getCount() > 0)
                    {
                        cCursor.moveToFirst();
                        cTran = new String[]{
                                cCursor.getString(cCursor.getColumnIndex("CATEGID")),
                                "-1"
                        };
                    }
                }
            }
        }
        catch(Exception e)
        {
            System.err.println("EXCEPTION:"+e);
        }

        return cTran;
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

    private void showNotification(Intent intent, String notificationText) {

        String NOTIFICATION_CHANNEL_ID = "android_mmex_1910"; // The id of the channel.

        int NOTIFICATION_ID = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        intent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1910, intent, 0);

        //Get an instance of NotificationManager//
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle(mContext.getString(R.string.application_name) + " - SMS Transaction Failed")
                .setContentText(notificationText)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setAutoCancel(true);

        // Gets an instance of the NotificationManager service//
        NotificationManager mNotificationManager =  (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
