package com.money.manager.ex.checkingaccount;

import com.money.manager.ex.CheckingAccountActivity;

/**
 * Constants used by the Checking Account
 */
public class CheckingAccountConstants {
    public static final String LOGCAT = CheckingAccountActivity.class.getSimpleName();

    public static final int REQUEST_PICK_PAYEE = 1;
    public static final int REQUEST_PICK_ACCOUNT = 2;
    public static final int REQUEST_PICK_CATEGORY = 3;
    public static final int REQUEST_PICK_SPLIT_TRANSACTION = 4;

    public static final String KEY_TRANS_ID = "AllDataActivity:TransId";
    public static final String KEY_BDID_ID = "AllDataActivity:bdId";
    public static final String KEY_ACCOUNT_ID = "AllDataActivity:AccountId";
    public static final String KEY_TO_ACCOUNT_ID = "AllDataActivity:ToAccountId";
    public static final String KEY_TO_ACCOUNT_NAME = "AllDataActivity:ToAccountName";
    public static final String KEY_TRANS_CODE = "AllDataActivity:TransCode";
    public static final String KEY_TRANS_STATUS = "AllDataActivity:TransStatus";
    public static final String KEY_TRANS_DATE = "AllDataActivity:TransDate";
    public static final String KEY_TRANS_AMOUNT = "AllDataActivity:TransAmount";
    public static final String KEY_TRANS_TOTAMOUNT = "AllDataActivity:TransTotAmount";
    public static final String KEY_PAYEE_ID = "AllDataActivity:PayeeId";
    public static final String KEY_PAYEE_NAME = "AllDataActivity:PayeeName";
    public static final String KEY_CATEGORY_ID = "AllDataActivity:CategoryId";
    public static final String KEY_CATEGORY_NAME = "AllDataActivity:CategoryName";
    public static final String KEY_SUBCATEGORY_ID = "AllDataActivity:SubCategoryId";
    public static final String KEY_SUBCATEGORY_NAME = "AllDataActivity:SubCategoryName";
    public static final String KEY_NOTES = "AllDataActivity:Notes";
    public static final String KEY_TRANS_NUMBER = "AllDataActivity:TransNumber";
    public static final String KEY_NEXT_OCCURRENCE = "AllDataActivity:NextOccurrence";
    public static final String KEY_SPLIT_TRANSACTION = "AllDataActivity:SplitTransaction";
    public static final String KEY_SPLIT_TRANSACTION_DELETED = "AllDataActivity:SplitTransactionDeleted";
    public static final String KEY_ACTION = "AllDataActivity:Action";
}
