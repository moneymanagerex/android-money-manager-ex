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

import com.money.manager.ex.core.TransactionTypes;

/**
 * Recurring transaction.
 */
public class TableBillsDeposits
        extends Dataset {

	// FIELD
	public static final String BDID = "BDID";
	public static final String ACCOUNTID = "ACCOUNTID";
	public static final String TOACCOUNTID = "TOACCOUNTID";
	public static final String PAYEEID = "PAYEEID";
	public static final String TRANSCODE = "TRANSCODE";
	public static final String TRANSAMOUNT = "TRANSAMOUNT";
	public static final String STATUS = "STATUS";
	public static final String TRANSACTIONNUMBER = "TRANSACTIONNUMBER";
	public static final String NOTES = "NOTES";
	public static final String CATEGID = "CATEGID";
	public static final String SUBCATEGID = "SUBCATEGID";
	public static final String TRANSDATE = "TRANSDATE";
	public static final String FOLLOWUPID = "FOLLOWUPID";
	public static final String TOTRANSAMOUNT = "TOTRANSAMOUNT";
	public static final String REPEATS = "REPEATS";
	public static final String NEXTOCCURRENCEDATE = "NEXTOCCURRENCEDATE";
	public static final String NUMOCCURRENCES = "NUMOCCURRENCES";
	// constructor
	public TableBillsDeposits() {
		super("billsdeposits_v1", DatasetType.TABLE, "billsdeposits");
	}
	
	public Integer id;
	public Integer accountId;
    public Integer toAccountId;
    public String transactionCode;
    public TransactionTypes transactionType;
    public String status;
    public Double amount;
    public Double totalAmount;
    public Integer payeeId;
    public Integer categoryId;
    public Integer subCategoryId;
    public String transactionNumber;
    public String notes;
    public String nextOccurrence;
    public Integer repeats;
    public Integer numOccurrence;
	
	@Override
	public String[] getAllColumns() {
		return new String [] {BDID + " AS _id", BDID, ACCOUNTID, TOACCOUNTID, PAYEEID, TRANSCODE, TRANSAMOUNT, STATUS,
							  TRANSACTIONNUMBER, NOTES, CATEGID, SUBCATEGID, TRANSDATE, FOLLOWUPID, TOTRANSAMOUNT, REPEATS,
							  NEXTOCCURRENCEDATE, NUMOCCURRENCES};
	}
	
	@Override
	public void setValueFromCursor(Cursor c) {
		this.id = c.getInt(c.getColumnIndex(TableBillsDeposits.BDID));
		this.accountId = c.getInt(c.getColumnIndex(TableBillsDeposits.ACCOUNTID));
		this.toAccountId = c.getInt(c.getColumnIndex(TableBillsDeposits.TOACCOUNTID));
		this.transactionCode = c.getString(c.getColumnIndex(TableBillsDeposits.TRANSCODE));
		this.transactionType = TransactionTypes.valueOf(this.transactionCode);
		status = c.getString(c.getColumnIndex(TableBillsDeposits.STATUS));
		amount = c.getDouble(c.getColumnIndex(TableBillsDeposits.TRANSAMOUNT));
		this.totalAmount = c.getDouble(c.getColumnIndex(TableBillsDeposits.TOTRANSAMOUNT));
		payeeId = c.getInt(c.getColumnIndex(TableBillsDeposits.PAYEEID));
		categoryId = c.getInt(c.getColumnIndex(TableBillsDeposits.CATEGID));
		subCategoryId = c.getInt(c.getColumnIndex(TableBillsDeposits.SUBCATEGID));
		transactionNumber = c.getString(c.getColumnIndex(TableBillsDeposits.TRANSACTIONNUMBER));
		notes = c.getString(c.getColumnIndex(TableBillsDeposits.NOTES));
		nextOccurrence = c.getString(c.getColumnIndex(TableBillsDeposits.NEXTOCCURRENCEDATE));
		repeats = c.getInt(c.getColumnIndex(TableBillsDeposits.REPEATS));
		numOccurrence = c.getInt(c.getColumnIndex(TableBillsDeposits.NUMOCCURRENCES));
	}
}
