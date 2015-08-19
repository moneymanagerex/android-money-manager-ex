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

public class TableCheckingAccount
		extends Dataset {

	public static final String TRANSID = "TRANSID";

	private int transId;
	private int accountId;
	private int toAccountId;
	private int payeeId;
	private String transCode;
	private double transAmount;
	private String status;
	private String transactionNumber;
	private String notes;
	private int categId;
	private int subCategId;
	private String transDate;
	private int followupId;
	private double toTransAmount;

	public TableCheckingAccount() {
		super("checkingaccount_v1", DatasetType.TABLE, "checkingaccount");
	}

	@Override
	public String[] getAllColumns() {
		return new String[] {
				"TRANSID AS _id", TRANSID,
                ISplitTransactionsDataset.ACCOUNTID,
                ISplitTransactionsDataset.TOACCOUNTID,
                ISplitTransactionsDataset.PAYEEID,
                ISplitTransactionsDataset.TRANSCODE,
                ISplitTransactionsDataset.TRANSAMOUNT,
                ISplitTransactionsDataset.STATUS,
                ISplitTransactionsDataset.TRANSACTIONNUMBER,
                ISplitTransactionsDataset.NOTES,
				ISplitTransactionsDataset.CATEGID,
                ISplitTransactionsDataset.SUBCATEGID,
                ISplitTransactionsDataset.TRANSDATE,
                ISplitTransactionsDataset.FOLLOWUPID,
                ISplitTransactionsDataset.TOTRANSAMOUNT};
	}
	
	@Override
	protected void setValueFromCursor(Cursor c) {
		if (c == null) { return; }

		this.setTransId(c.getInt(c.getColumnIndex(TRANSID)));
		this.setAccountId(c.getInt(c.getColumnIndex(ISplitTransactionsDataset.ACCOUNTID)));
		this.setToAccountId(c.getInt(c.getColumnIndex(ISplitTransactionsDataset.TOACCOUNTID)));
		this.setPayeeId(c.getInt(c.getColumnIndex(ISplitTransactionsDataset.PAYEEID)));
		this.setTransCode(c.getString(c.getColumnIndex(ISplitTransactionsDataset.TRANSCODE)));
		this.setTransAmount(c.getDouble(c.getColumnIndex(ISplitTransactionsDataset.TOTRANSAMOUNT)));
		this.setStatus(c.getString(c.getColumnIndex(ISplitTransactionsDataset.STATUS)));
		this.setTransactionNumber(c.getString(c.getColumnIndex(ISplitTransactionsDataset.TRANSACTIONNUMBER)));
		this.setNotes(c.getString(c.getColumnIndex(ISplitTransactionsDataset.NOTES)));
		this.setCategId(c.getInt(c.getColumnIndex(ISplitTransactionsDataset.CATEGID)));
		this.setSubCategId(c.getInt(c.getColumnIndex(ISplitTransactionsDataset.SUBCATEGID)));
		this.setTransDate(c.getString(c.getColumnIndex(ISplitTransactionsDataset.TRANSDATE)));
		this.setFollowupId(c.getInt(c.getColumnIndex(ISplitTransactionsDataset.FOLLOWUPID)));
		this.setToTransAmount(c.getInt(c.getColumnIndex(ISplitTransactionsDataset.TOTRANSAMOUNT)));
		
	}
	public int getTransId() {
		return transId;
	}
	public void setTransId(int transId) {
		this.transId = transId;
	}
	public int getAccountId() {
		return accountId;
	}
	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}
	public int getToAccountId() {
		return toAccountId;
	}
	public void setToAccountId(int toAccountId) {
		this.toAccountId = toAccountId;
	}
	public int getPayeeId() {
		return payeeId;
	}
	public void setPayeeId(int payeeId) {
		this.payeeId = payeeId;
	}
	public String getTransCode() {
		return transCode;
	}
	public void setTransCode(String transCode) {
		this.transCode = transCode;
	}
	public double getTransAmount() {
		return transAmount;
	}
	public void setTransAmount(double transAmount) {
		this.transAmount = transAmount;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTransactionNumber() {
		return transactionNumber;
	}
	public void setTransactionNumber(String transactionNumber) {
		this.transactionNumber = transactionNumber;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public int getCategId() {
		return categId;
	}
	public void setCategId(int categId) {
		this.categId = categId;
	}
	public int getSubCategId() {
		return subCategId;
	}
	public void setSubCategId(int subCategId) {
		this.subCategId = subCategId;
	}
	public String getTransDate() {
		return transDate;
	}
	public void setTransDate(String transDate) {
		this.transDate = transDate;
	}
	public int getFollowupId() {
		return followupId;
	}
	public void setFollowupId(int followupId) {
		this.followupId = followupId;
	}
	public double getToTransAmount() {
		return toTransAmount;
	}
	public void setToTransAmount(double toTransAmount) {
		this.toTransAmount = toTransAmount;
	}
}
