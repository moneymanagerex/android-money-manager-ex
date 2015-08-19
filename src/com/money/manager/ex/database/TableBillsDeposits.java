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
import android.os.Parcel;
import android.os.Parcelable;

import com.money.manager.ex.core.TransactionTypes;

/**
 * Recurring transaction.
 */
public class TableBillsDeposits
        extends Dataset
        implements Parcelable {

	// FIELD
	public static final String BDID = "BDID";
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
//    public TransactionTypes transactionType;
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
		return new String [] {BDID + " AS _id", BDID,
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
                ISplitTransactionsDataset.TOTRANSAMOUNT,
                REPEATS, NEXTOCCURRENCEDATE, NUMOCCURRENCES};
	}
	
	@Override
	public void setValueFromCursor(Cursor c) {
		this.id = c.getInt(c.getColumnIndex(TableBillsDeposits.BDID));
		this.accountId = c.getInt(c.getColumnIndex(ISplitTransactionsDataset.ACCOUNTID));
		this.toAccountId = c.getInt(c.getColumnIndex(ISplitTransactionsDataset.TOACCOUNTID));
		this.transactionCode = c.getString(c.getColumnIndex(ISplitTransactionsDataset.TRANSCODE));
//		this.transactionType = TransactionTypes.valueOf(this.transactionCode);
		status = c.getString(c.getColumnIndex(ISplitTransactionsDataset.STATUS));
		amount = c.getDouble(c.getColumnIndex(ISplitTransactionsDataset.TRANSAMOUNT));
		this.totalAmount = c.getDouble(c.getColumnIndex(ISplitTransactionsDataset.TOTRANSAMOUNT));
		payeeId = c.getInt(c.getColumnIndex(ISplitTransactionsDataset.PAYEEID));
		categoryId = c.getInt(c.getColumnIndex(ISplitTransactionsDataset.CATEGID));
		subCategoryId = c.getInt(c.getColumnIndex(ISplitTransactionsDataset.SUBCATEGID));
		transactionNumber = c.getString(c.getColumnIndex(ISplitTransactionsDataset.TRANSACTIONNUMBER));
		notes = c.getString(c.getColumnIndex(ISplitTransactionsDataset.NOTES));
		nextOccurrence = c.getString(c.getColumnIndex(TableBillsDeposits.NEXTOCCURRENCEDATE));
		repeats = c.getInt(c.getColumnIndex(TableBillsDeposits.REPEATS));
		numOccurrence = c.getInt(c.getColumnIndex(TableBillsDeposits.NUMOCCURRENCES));
	}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(accountId);
        dest.writeInt(toAccountId);
        dest.writeString(transactionCode);
        dest.writeString(status);
        dest.writeDouble(amount);
        dest.writeDouble(totalAmount);
        dest.writeInt(payeeId);
        dest.writeInt(categoryId);
        dest.writeInt(subCategoryId);
        dest.writeString(transactionNumber);
        dest.writeString(notes);
        dest.writeString(nextOccurrence);
        dest.writeInt(repeats);
        dest.writeInt(numOccurrence);
    }

    public void readToParcel(Parcel source) {
        id = source.readInt();
        accountId = source.readInt();
        toAccountId = source.readInt();
        transactionCode = source.readString();
        status = source.readString();
        amount = source.readDouble();
        totalAmount = source.readDouble();
        payeeId = source.readInt();
        categoryId = source.readInt();
        subCategoryId = source.readInt();
        transactionNumber = source.readString();
        notes = source.readString();
        nextOccurrence = source.readString();
        repeats = source.readInt();
        numOccurrence = source.readInt();
    }


    public final static Parcelable.Creator<TableBillsDeposits> CREATOR = new Parcelable.Creator<TableBillsDeposits>() {
        public TableBillsDeposits createFromParcel(Parcel source) {
            TableBillsDeposits record = new TableBillsDeposits();
            record.readToParcel(source);
            return record;
        }

        @Override
        public TableBillsDeposits[] newArray(int size) {
            return new TableBillsDeposits[size];
        }
    };
}

