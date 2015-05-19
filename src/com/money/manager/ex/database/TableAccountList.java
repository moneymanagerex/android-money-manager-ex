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

import android.database.Cursor;

public class TableAccountList extends Dataset {
	// definizione dei nomi dei campi
	public static final String ACCOUNTID = "ACCOUNTID";
	public static final String ACCOUNTNAME = "ACCOUNTNAME";
	public static final String ACCOUNTTYPE = "ACCOUNTTYPE";
	public static final String ACCOUNTNUM = "ACCOUNTNUM";
	public static final String STATUS = "STATUS";
	public static final String NOTES = "NOTES";
	public static final String HELDAT = "HELDAT";
	public static final String WEBSITE = "WEBSITE";
	public static final String CONTACTINFO = "CONTACTINFO";
	public static final String ACCESSINFO = "ACCESSINFO"; 
	public static final String INITIALBAL = "INITIALBAL";
	public static final String FAVORITEACCT = "FAVORITEACCT";
	public static final String CURRENCYID = "CURRENCYID";

	// definizione dei campi
	private int accountId;
	private String accountName;
	private String accountType;
	private String accountNum;
	private String status;
	private String notes;
	private String heldat;
	private String website;
	private String contactinfo;
	private String accessinfo; 
	private double initialBal;
	private String favoriteAcct;
	private int currencyId;
	// costruttore
	public TableAccountList() {
		super("accountlist_v1", DatasetType.TABLE, "accountlist");
	}

    @Override
	public String[] getAllColumns() {
		return new String[] { "ACCOUNTID AS _id", ACCOUNTID, ACCOUNTNAME,
				ACCOUNTTYPE, ACCOUNTNUM, STATUS, NOTES, HELDAT, WEBSITE,
				CONTACTINFO, ACCESSINFO, INITIALBAL, FAVORITEACCT, CURRENCYID };
	}

	@Override
	public void setValueFromCursor(Cursor c) {
		// controllo che non sia null il cursore
		if (c == null) { return; }
		// controllo che il numero di colonne siano le stesse
		// if (!(c.getColumnCount() == this.getAllColumns().length)) { return; }
		// set dei valori
		this.setAccountId(c.getInt(c.getColumnIndex(ACCOUNTID)));
		this.setAccountName(c.getString(c.getColumnIndex(ACCOUNTNAME)));
		this.setAccountType(c.getString(c.getColumnIndex(ACCOUNTTYPE)));
		this.setAccountNum(c.getString(c.getColumnIndex(ACCOUNTNUM)));
		this.setStatus(c.getString(c.getColumnIndex(STATUS)));
		this.setNotes(c.getString(c.getColumnIndex(NOTES)));
		this.setHeldat(c.getString(c.getColumnIndex(HELDAT)));
		this.setWebsite(c.getString(c.getColumnIndex(WEBSITE)));
		this.setContactinfo(c.getString(c.getColumnIndex(CONTACTINFO)));
		this.setAccessinfo(c.getString(c.getColumnIndex(ACCESSINFO)));
		this.setInitialBal(c.getDouble(c.getColumnIndex(INITIALBAL)));
		this.setFavoriteAcct(c.getString(c.getColumnIndex(FAVORITEACCT)));
		this.setCurrencyId(c.getInt(c.getColumnIndex(CURRENCYID)));
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
	 * @return the accountType
	 */
	public String getAccountType() {
		return accountType;
	}
	/**
	 * @param accountType the accountType to set
	 */
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	/**
	 * @return the accountNum
	 */
	public String getAccountNum() {
		return accountNum;
	}
	/**
	 * @param accountNum the accountNum to set
	 */
	public void setAccountNum(String accountNum) {
		this.accountNum = accountNum;
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
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}
	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}
	/**
	 * @return the heladt
	 */
	public String getHeladt() {
		return heldat;
	}
	/**
	 * @param heladt the heladt to set
	 */
	public void setHeldat(String heladt) {
		this.heldat = heladt;
	}
	/**
	 * @return the website
	 */
	public String getWebsite() {
		return website;
	}
	/**
	 * @param website the website to set
	 */
	public void setWebsite(String website) {
		this.website = website;
	}
	/**
	 * @return the contactinfo
	 */
	public String getContactinfo() {
		return contactinfo;
	}
	/**
	 * @param contactinfo the contactinfo to set
	 */
	public void setContactinfo(String contactinfo) {
		this.contactinfo = contactinfo;
	}
	/**
	 * @return the accessinfo
	 */
	public String getAccessinfo() {
		return accessinfo;
	}
	/**
	 * @param accessinfo the accessinfo to set
	 */
	public void setAccessinfo(String accessinfo) {
		this.accessinfo = accessinfo;
	}
	/**
	 * @return the initialBal
	 */
	public double getInitialBal() {
		return initialBal;
	}
	/**
	 * @param initialBal the initialBal to set
	 */
	public void setInitialBal(double initialBal) {
		this.initialBal = initialBal;
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
	 * 
	 * @return se il conto � favorito oppure no
	 */
	public boolean isFavoriteAcct() {
		return String.valueOf(Boolean.TRUE).equalsIgnoreCase(getFavoriteAcct());
	}
	/**
	 * 
	 * @param value il valore boolean nel table
	 */
	public void setFavoriteAcct(boolean value) {
		this.setFavoriteAcct(Boolean.toString(value).toUpperCase());
	}
}
