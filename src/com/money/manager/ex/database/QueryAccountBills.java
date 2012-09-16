/*******************************************************************************
 * Copyright (C) 2012 The Android Money Manager Ex Project
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
 ******************************************************************************/
package com.money.manager.ex.database;

import android.database.Cursor;

import com.money.manager.ex.MoneyManagerApplication;


public class QueryAccountBills extends Dataset {
	private static final String SQL = 
			"SELECT ACCOUNTLIST_V1.ACCOUNTID AS _id, ACCOUNTLIST_V1.ACCOUNTID, ACCOUNTNAME, STATUS, FAVORITEACCT, CURRENCYID, " +
			"(INITIALBAL + T1.TOTAL) AS TOTAL, " +
			"(INITIALBAL + T1.reconciled) AS RECONCILED " +
			"FROM ACCOUNTLIST_V1, ( " +
			"select accountid, ROUND(SUM(total), 2) as total, ROUND(SUM(reconciled), 2) as reconciled " +
			"from ( " +
			"select accountid, transcode, sum(case when status in ('R', 'F', '') then -transamount else 0 end) as total, sum(case when status = 'R' then -transamount else 0 end) as reconciled " +
			"from checkingaccount_v1 " +
			"where transcode in ('Withdrawal') " +
			"group by accountid, transcode " +
			"  " +
			"union " +
			"  " +
			"select accountid, transcode, sum(case when status in ('R', 'F', '')  then transamount else 0 end) as total, sum(case when status = 'R' then transamount else 0 end) as reconciled " +
			"from checkingaccount_v1 " +
			"where transcode in ('Deposit') " +
			"group by accountid, transcode " +
			" " +
			"union " +
			" " +
			"select accountid, transcode, sum(case when status in ('R', 'F', '')  then -transamount else 0 end) as total, sum(case when status = 'R' then -transamount else 0 end) as reconciled " +
			"from checkingaccount_v1 " +
			"where transcode in ('Transfer') " +
			"group by accountid, transcode " +
			"  " +
			"union " +
			"  " +
			"select toaccountid AS accountid, transcode, sum(case when status in ('R', 'F', '')  then totransamount else 0 end) as total, sum(case when status = 'R' then totransamount else 0 end) as reconciled " +
			"from checkingaccount_v1 " +
			"where transcode in ('Transfer') and toaccountid <> -1  " +
			"group by toaccountid, transcode " +
			")  t " +
			"group by accountid " +
			") T1 " +
			"WHERE ACCOUNTLIST_V1.ACCOUNTID=T1.ACCOUNTID AND ACCOUNTLIST_V1.ACCOUNTTYPE='Checking'"; 
	//definizione dei nomi dei campi
	public static final String ACCOUNTID = "ACCOUNTID";
	public static final String ACCOUNTNAME = "ACCOUNTNAME";
	public static final String STATUS = "STATUS";
	public static final String FAVORITEACCT = "FAVORITEACCT"; 
	public static final String CURRENCYID = "CURRENCYID"; 
	public static final String TOTAL = "TOTAL";
	public static final String RECONCILED = "RECONCILED";
	//definizione dei campi
	private int accountId;
	private String accountName;
	private String status;
	private String favoriteAcct;
	private int currencyId;
	private double total;
	// definizione del costruttore
	public QueryAccountBills() {
		super(SQL, DatasetType.QUERY, "accountbills");
	}
	@Override
	public String[] getAllColumns() {
		return new String[] {"ACCOUNTID AS _id", ACCOUNTID, ACCOUNTNAME, STATUS, FAVORITEACCT, CURRENCYID, TOTAL, RECONCILED};
	}
	/**
	 * @return the accountId
	 */
	public int getAccountId() {
		return accountId;
	}
	/**
	 * @param accountId the accountId to set
	 */
	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}
	/**
	 * @return the accountName
	 */
	public String getAccountName() {
		return accountName;
	}
	/**
	 * @param accountName the accountName to set
	 */
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the favoriteAcct
	 */
	public String getFavoriteAcct() {
		return favoriteAcct;
	}
	/**
	 * @param favoriteAcct the favoriteAcct to set
	 */
	public void setFavoriteAcct(String favoriteAcct) {
		this.favoriteAcct = favoriteAcct;
	}
	/**
	 * @return the currencyId
	 */
	public int getCurrencyId() {
		return currencyId;
	}
	/**
	 * @param currencyId the currencyId to set
	 */
	public void setCurrencyId(int currencyId) {
		this.currencyId = currencyId;
	}
	/**
	 * @return the total
	 */
	public double getTotal() {
		return total;
	}
	/**
	 * @param total the total to set
	 */
	public void setTotal(double total) {
		this.total = total;
	}
	@Override
	public void setValueFromCursor(Cursor c) {
		// controllo che non sia null il cursore
		if (c == null) { return; }
		// controllo che il numero di colonne siano le stesse
		if (!(c.getColumnCount() == this.getAllColumns().length)) { return; }
		// set dei valori
		this.setAccountId(c.getInt(c.getColumnIndex(ACCOUNTID)));
		this.setAccountName(c.getString(c.getColumnIndex(ACCOUNTNAME)));
		this.setCurrencyId(c.getInt(c.getColumnIndex(CURRENCYID)));
		this.setFavoriteAcct(c.getString(c.getColumnIndex(FAVORITEACCT)));
		this.setStatus(c.getString(c.getColumnIndex(STATUS)));
		this.setTotal(c.getDouble(c.getColumnIndex(TOTAL)));
	}
}
