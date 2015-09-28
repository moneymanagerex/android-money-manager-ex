/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
 *
 */
package com.money.manager.ex.database;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.money.manager.ex.Constants;
import com.money.manager.ex.core.NumericHelper;

import java.math.BigDecimal;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

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
    public Money amount;
    public Money totalAmount;
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
		amount = MoneyFactory.fromDouble(c.getDouble(c.getColumnIndex(ISplitTransactionsDataset.TRANSAMOUNT)));
		this.totalAmount = MoneyFactory.fromDouble(c.getDouble(c.getColumnIndex(ISplitTransactionsDataset.TOTRANSAMOUNT)));
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
        dest.writeString(amount.toString());
        dest.writeString(totalAmount.toString());
        dest.writeInt(payeeId);
        dest.writeInt(categoryId);
        dest.writeInt(subCategoryId);
        dest.writeString(transactionNumber);
        dest.writeString(notes);
        dest.writeString(nextOccurrence);
        dest.writeInt(repeats);
        dest.writeInt(numOccurrence);
    }

    public TableBillsDeposits initialize() {
        this.id = Constants.NOT_SET;
        this.accountId = Constants.NOT_SET;
        this.toAccountId = Constants.NOT_SET;
        this.transactionCode = "";
        this.status = "";
        this.amount = MoneyFactory.fromBigDecimal(BigDecimal.ZERO);
        this.totalAmount = MoneyFactory.fromBigDecimal(BigDecimal.ZERO);
        this.payeeId = Constants.NOT_SET;
        this.categoryId = Constants.NOT_SET;
        this.subCategoryId = Constants.NOT_SET;
        this.transactionNumber = "";
        this.notes = "";
        this.nextOccurrence = "";
        this.repeats = Constants.NOT_SET;
        this.numOccurrence = Constants.NOT_SET;

        return this;
    }

    public void readFromParcel(Parcel source) {
        id = source.readInt();
        accountId = source.readInt();
        toAccountId = source.readInt();
        transactionCode = source.readString();
        status = source.readString();
        amount = MoneyFactory.fromString(source.readString());
        totalAmount = MoneyFactory.fromString(source.readString());
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
            record.readFromParcel(source);
            return record;
        }

        @Override
        public TableBillsDeposits[] newArray(int size) {
            return new TableBillsDeposits[size];
        }
    };
}

