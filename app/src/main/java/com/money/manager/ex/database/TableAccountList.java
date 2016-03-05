/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.database;

import android.database.Cursor;

import com.money.manager.ex.domainmodel.Account;

public class TableAccountList
		extends Dataset {

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

    public TableAccountList() {
		super("accountlist_v1", DatasetType.TABLE, "accountlist");
	}

    @Override
	public String[] getAllColumns() {
		return new String[] { "ACCOUNTID AS _id", Account.ACCOUNTID, Account.ACCOUNTNAME,
				Account.ACCOUNTTYPE, Account.ACCOUNTNUM, Account.STATUS, Account.NOTES,
				Account.HELDAT, Account.WEBSITE, Account.CONTACTINFO, Account.ACCESSINFO,
                Account.INITIALBAL, Account.FAVORITEACCT, Account.CURRENCYID };
	}

	@Override
	public void setValueFromCursor(Cursor c) {
		// controllo che non sia null il cursore
		if (c == null) { return; }
		// controllo che il numero di colonne siano le stesse
		// if (!(c.getColumnCount() == this.getAllColumns().length)) { return; }
		// set dei valori
		this.setAccountId(c.getInt(c.getColumnIndex(Account.ACCOUNTID)));
		this.setAccountName(c.getString(c.getColumnIndex(Account.ACCOUNTNAME)));
		this.setAccountType(c.getString(c.getColumnIndex(Account.ACCOUNTTYPE)));
		this.setAccountNum(c.getString(c.getColumnIndex(Account.ACCOUNTNUM)));
		this.setStatus(c.getString(c.getColumnIndex(Account.STATUS)));
		this.setNotes(c.getString(c.getColumnIndex(Account.NOTES)));
		this.setHeldat(c.getString(c.getColumnIndex(Account.HELDAT)));
		this.setWebsite(c.getString(c.getColumnIndex(Account.WEBSITE)));
		this.setContactinfo(c.getString(c.getColumnIndex(Account.CONTACTINFO)));
		this.setAccessinfo(c.getString(c.getColumnIndex(Account.ACCESSINFO)));
		this.setInitialBal(c.getDouble(c.getColumnIndex(Account.INITIALBAL)));
		this.setFavoriteAcct(c.getString(c.getColumnIndex(Account.FAVORITEACCT)));
		this.setCurrencyId(c.getInt(c.getColumnIndex(Account.CURRENCYID)));
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
