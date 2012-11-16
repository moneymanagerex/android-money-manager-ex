package com.money.manager.ex.database;

import android.content.Context;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
/**
 * 
 * @author lazzari.ale@gmail.com
 *
 */
public class QueryBillDeposits extends Dataset {
	// fields
	public static String BDID = "BDID";
	public static String PAYEEID = "PAYEEID";
	public static String PAYEENAME = "PAYEENAME";
	public static String TOACCOUNTID = "TOACCOUNTID";
	public static String TOACCOUNTNAME = "TOACCOUNTNAME";
	public static String ACCOUNTID = "ACCOUNTID";
	public static String ACCOUNTNAME = "ACCOUNTNAME";
	public static String CURRENCYID = "CURRENCYID";
	public static String CATEGSUBCATEGNAME = "CATEGSUBCATEGNAME";
	public static String CATEGNAME = "CATEGNAME";
	public static String SUBCATEGNAME = "SUBCATEGNAME";
	public static String TRANSCODE = "TRANSCODE";
	public static String TRANSAMOUNT = "TRANSAMOUNT";
	public static String NEXTOCCURRENCEDATE = "NEXTOCCURRENCEDATE";
	public static String REPEATS = "REPEATS";
	public static String DAYSLEFT = "DAYSLEFT";
	public static String NOTES = "NOTES";
	public static String STATUS = "STATUS";
	public static String NUMOCCURRENCES = "NUMOCCURRENCES";
	public static String TOTRANSAMOUNT = "TOTRANSAMOUNT";
	public static String TRANSACTIONNUMBER = "TRANSACTIONNUMBER";
	public static String TRANSDATE = "TRANSDATE";
	public static String AMOUNT = "AMOUNT";
	public static String USERNEXTOCCURRENCEDATE = "USERNEXTOCCURRENCEDATE";

	// constructor
	public QueryBillDeposits(Context context) {
		super(MoneyManagerApplication.getRawAsString(context, R.raw.billdeposits), DatasetType.QUERY, QueryBillDeposits.class.getSimpleName());
	}

	// get all columns
	@Override
	public String[] getAllColumns() {
		return new String[] { BDID + " AS _id", BDID, PAYEEID, PAYEENAME, TOACCOUNTID, TOACCOUNTNAME, ACCOUNTID, ACCOUNTNAME, CURRENCYID, CATEGSUBCATEGNAME,
				CATEGNAME, SUBCATEGNAME, TRANSCODE, TRANSAMOUNT, NEXTOCCURRENCEDATE, REPEATS, DAYSLEFT, NOTES, STATUS, NUMOCCURRENCES, TOTRANSAMOUNT,
				TRANSACTIONNUMBER, TRANSDATE, AMOUNT, USERNEXTOCCURRENCEDATE };
	}
}
