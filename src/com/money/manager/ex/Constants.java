package com.money.manager.ex;

public class Constants {
	// Transaction Type
	public static final String TRANSACTION_TYPE_WITHDRAWAL = "Withdrawal";
	public static final String TRANSACTION_TYPE_DEPOSIT = "Deposit"; 
	public static final String TRANSACTION_TYPE_TRANSFER = "Transfer";
	// Transaction Status
	public static final String TRANSACTION_STATUS_UNRECONCILED = "";
	public static final String TRANSACTION_STATUS_RECONCILED = "R";
	public static final String TRANSACTION_STATUS_VOID = "V";
	public static final String TRANSACTION_STATUS_FOLLOWUP = "F";
	public static final String TRANSACTION_STATUS_DUPLICATE = "D";
	// Info Table Settings
	public static final String INFOTABLE_USERNAME = "USERNAME";
	public static final String INFOTABLE_BASECURRENCYID = "BASECURRENCYID";
	public static final String INFOTABLE_DATEFORMAT = "DATEFORMAT";
	public static final String INFOTABLE_FINANCIAL_YEAR_START_DAY = "FINANCIAL_YEAR_START_DAY";
	public static final String INFOTABLE_FINANCIAL_YEAR_START_MONTH = "FINANCIAL_YEAR_START_MONTH";
	public static final String INFOTABLE_SKU_ORDER_ID = "SKU_ORDER_ID";
	// Intent: Action
	public static final String INTENT_ACTION_EDIT = "android.intent.action.EDIT";
	public static final String INTENT_ACTION_INSERT = "android.intent.action.INSERT";
}
