/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.database;

/**
 * This has been migrated to QueryAllData.
 */
public class ViewMobileData extends Dataset {
	public static final String mobiledata =  
		"( " + 
		"SELECT 	CANS.TransID AS ID, " + 
		"	CANS.TransCode AS TransactionType, " + 
		"	date( CANS.TransDate ) AS Date, " + 
		"	d.userdate AS UserDate, " + 
		"	coalesce( CAT.CategName, SCAT.CategName ) AS Category, " + 
		"	coalesce( SUBCAT.SUBCategName, SSCAT.SUBCategName, '' ) AS Subcategory, " + 
		"	ROUND( ( CASE CANS.TRANSCODE WHEN 'Withdrawal' THEN -1 ELSE 1 END ) *  ( CASE CANS.CATEGID WHEN -1 THEN st.splittransamount ELSE CANS.TRANSAMOUNT END) , 2 ) AS Amount, " + 
		"	cf.currency_symbol AS currency, " + 
		"	CANS.Status AS Status, " + 
		"	CANS.NOTES AS Notes, " + 
		"	cf.BaseConvRate AS BaseConvRate, " + 
		"	FROMACC.CurrencyID AS CurrencyID, " + 
		"	FROMACC.AccountName AS AccountName, " + 
		"	FROMACC.AccountID AS AccountID, " + 
		"	ifnull( TOACC.AccountName, '' ) AS ToAccountName, " + 
		"	ifnull( TOACC.ACCOUNTId, -1 ) AS ToAccountID, " + 
		"	CANS.ToTransAmount ToTransAmount, " + 
		"	ifnull( TOACC.CURRENCYID, -1 ) AS ToCurrencyID, " + 
		"	( CASE ifnull( CANS.CATEGID, -1 ) WHEN -1 THEN 1 ELSE 0 END ) AS Splitted, " + 
		"	ifnull( CAT.CategId, st.CategId ) AS CategID, " + 
		"	ifnull( ifnull( SUBCAT.SubCategID, st.subCategId ) , -1 ) AS SubCategID, " + 
		"	ifnull( PAYEE.PayeeName, '' ) AS Payee, " + 
		"	ifnull( PAYEE.PayeeID, -1 ) AS PayeeID, " + 
		"	CANS.TRANSACTIONNUMBER AS TransactionNumber, " + 
		"	d.year AS Year, " + 
		"	d.month AS Month, " + 
		"	d.day AS Day, " + 
		"	d.finyear AS FinYear, " + 
		"	ROUND( ( CASE CANS.TRANSCODE WHEN 'Withdrawal' THEN -1 ELSE 1 END ) *  ( CASE CANS.CATEGID WHEN -1 THEN st.splittransamount ELSE CANS.TRANSAMOUNT END) , 2 ) * ifnull(cf.BaseConvRate, 1) As AmountBaseConvRate " + 
		"FROM 	CHECKINGACCOUNT_V1 CANS LEFT JOIN CATEGORY_V1 CAT ON CAT.CATEGID = CANS.CATEGID " + 
		"	LEFT JOIN SUBCATEGORY_V1 SUBCAT ON SUBCAT.SUBCATEGID = CANS.SUBCATEGID AND SUBCAT.CATEGID = CANS.CATEGID " + 
		"	LEFT JOIN PAYEE_V1 PAYEE ON PAYEE.PAYEEID = CANS.PAYEEID  " + 
		"	LEFT JOIN ACCOUNTLIST_V1 FROMACC ON FROMACC.ACCOUNTID = CANS.ACCOUNTID " + 
		"	LEFT JOIN ACCOUNTLIST_V1 TOACC ON TOACC.ACCOUNTID = CANS.TOACCOUNTID " + 
		"	LEFT JOIN splittransactions_v1 st ON CANS.transid = st.transid " + 
		"	LEFT JOIN CATEGORY_V1 SCAT ON SCAT.CATEGID = st.CATEGID AND CANS.TransId = st.transid " + 
		"	LEFT JOIN SUBCATEGORY_V1 SSCAT ON SSCAT.SUBCATEGID = st.SUBCATEGID AND SSCAT.CATEGID = st.CATEGID AND CANS.TransId = st.transid " + 
		"	LEFT JOIN currencyformats_v1 cf ON cf.currencyid = FROMACC.currencyid " + 
		"	LEFT JOIN  (  " + 
		"           SELECT	transid AS id, " + 
		"	    	  			date( transdate ) AS transdate, " + 
		"	    				round( strftime( '%d', transdate )  ) AS day, " + 
		"	    				round( strftime( '%m', transdate )  ) AS month, " + 
		"	    				round( strftime( '%Y', transdate )  ) AS year, " + 
		"	    				round( strftime( '%Y', transdate, 'start of month',  (  ( CASE WHEN fd.infovalue <= round( strftime( '%d', transdate )  ) THEN 1 ELSE 0 END ) - fm.infovalue ) || ' month' )  ) AS finyear, " + 
		"	    				ifnull( ifnull( strftime( df.infovalue, TransDate ) ,  ( strftime( REPLACE( df.infovalue, '%y', SubStr( strftime( '%Y', TransDate ) , 3, 2 )  ) , TransDate )  )  ) , date( TransDate )  ) AS UserDate " + 
		"           FROM CHECKINGACCOUNT_V1 LEFT JOIN infotable_v1 df ON df.infoname = 'DATEFORMAT' " + 
		"	    				LEFT JOIN infotable_v1 fm ON fm.infoname = 'FINANCIAL_YEAR_START_MONTH' " + 
		"	    				LEFT JOIN infotable_v1 fd ON fd.infoname = 'FINANCIAL_YEAR_START_DAY'  " + 
		"       ) d ON d.id = CANS.TRANSID " + 
		") mobiledata ";
	// FIELDS
	public static final String ID = "ID";
	public static final String TransactionType = "TransactionType";
	public static final String Date = "Date";
	public static final String UserDate = "UserDate";
	public static final String Year = "Year";
	public static final String Month = "Month";
	public static final String Day = "Day";
	public static final String Category = "Category";
	public static final String Subcategory = "Subcategory";
	public static final String Amount = "Amount";
	public static final String BaseConvRate = "BaseConvRate";
	public static final String CURRENCYID = "CurrencyID";
	public static final String AccountName = "AccountName";
	public static final String ACCOUNTID = "AccountID";
	public static final String ToAccountName = "ToAccountName";
	public static final String ToAccountID = "ToAccountID";
	public static final String TOTRANSAMOUNT = "ToTransAmount";
	public static final String ToCurrencyID = "ToCurrencyID";
	public static final String Splitted  = "Splitted";
	public static final String CategID = "CategID";
	public static final String SubcategID = "SubcategID";
	public static final String Payee = "Payee";
	public static final String PayeeID = "PayeeID";
	public static final String TransactionNumber = "TransactionNumber";
	public static final String Status = "Status";
	public static final String Notes = "Notes";
	public static final String currency = "currency";
	public static final String finyear = "finyear";
	public static final String AmountBaseConvRate = "AmountBaseConvRate";
	
	// CONSTRUCTOR
	public ViewMobileData() {
		super(mobiledata, DatasetType.VIEW, "mobiledata");
	}
	
	@Override
	public String[] getAllColumns() {
		return new String[] {"ID AS _id", ID, TransactionType, Date, UserDate, Year, Month, Day,
				Category, Subcategory, Amount, BaseConvRate, CURRENCYID, AccountName, ACCOUNTID,
                ToAccountName, ToAccountID, TOTRANSAMOUNT, ToCurrencyID, Splitted , CategID,
                SubcategID, Payee, PayeeID, TransactionNumber, Status, Notes, currency, finyear,
                AmountBaseConvRate};
	}
}
