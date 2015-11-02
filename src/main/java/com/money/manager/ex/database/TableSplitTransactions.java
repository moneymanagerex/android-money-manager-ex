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
 */
package com.money.manager.ex.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Parcel;
import android.os.Parcelable;

import com.money.manager.ex.core.DatabaseField;
import com.money.manager.ex.domainmodel.SplitTransaction;

import org.apache.commons.lang3.StringUtils;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

public class TableSplitTransactions
	extends Dataset
	implements Parcelable, ISplitTransactionsDataset {

	@DatabaseField(columnName = SplitTransaction.SPLITTRANSID)
	private int splitTransId = -1;
	@DatabaseField(columnName = SplitTransaction.TRANSID)
	private int transId = -1;
	@DatabaseField(columnName = SplitTransaction.CATEGID)
	private int categId = -1;
	@DatabaseField(columnName = SplitTransaction.SUBCATEGID)
	private int subCategId = -1;
	@DatabaseField(columnName = SplitTransaction.SPLITTRANSAMOUNT)
	private Money splitTransAmount = MoneyFactory.fromString("0");

    ContentValues contentValues = new ContentValues();

	public TableSplitTransactions() {
		super(SplitTransaction.TABLE_NAME, DatasetType.TABLE, "splittransaction");
	}

	@Override
	public String[] getAllColumns() {
		return new String[] {"SPLITTRANSID AS _id",
			SplitTransaction.SPLITTRANSID,
			SplitTransaction.TRANSID,
			SplitTransaction.CATEGID,
			SplitTransaction.SUBCATEGID,
			SplitTransaction.SPLITTRANSAMOUNT };
	}

	/**
	 * @return the categId
	 */
	public int getCategId() {
		return categId;
	}

	/**
	 * @return the splitTransAmount
	 */
	public Money getSplitTransAmount() {
		return splitTransAmount;
	}

	/**
	 * @return the splitTransId
	 */
	public int getSplitTransId() {
		return splitTransId;
	}

	/**
	 * @return the subCategId
	 */
	public int getSubCategId() {
		return subCategId;
	}

	/**
	 * @return the transId
	 */
	public int getTransId() {
		return transId;
	}

	/**
	 * @param categId the categId to set
	 */
	public void setCategId(int categId) {
		this.categId = categId;
	}

    /**
     * @param splitTransAmount the splitTransAmount to set
     */
    public void setSplitTransAmount(Money splitTransAmount) {
        this.splitTransAmount = splitTransAmount;
    }

	/**
	 * @param splitTransId the splitTransId to set
	 */
	public void setSplitTransId(int splitTransId) {
		this.splitTransId = splitTransId;
	}

	/**
	 * @param subCategId the subCategId to set
	 */
	public void setSubCategId(int subCategId) {
		this.subCategId = subCategId;
	}

	/**
	 * @param transId the transId to set
	 */
	public void setTransId(int transId) {
		this.transId = transId;
	}
	
	@Override
	public void setValueFromCursor(Cursor c) {
		if (c == null) return;

        DatabaseUtils.cursorRowToContentValues(c, this.contentValues);
        DatabaseUtils.cursorDoubleToCursorValues(c, SplitTransaction.SPLITTRANSAMOUNT, this.contentValues);

		if (c.getColumnIndex(SplitTransaction.SPLITTRANSID) != -1)
			setSplitTransId(c.getInt(c.getColumnIndex(SplitTransaction.SPLITTRANSID)));
		if (c.getColumnIndex(SplitTransaction.TRANSID) != -1)
			setTransId(c.getInt(c.getColumnIndex(SplitTransaction.TRANSID)));
		if (c.getColumnIndex(SplitTransaction.CATEGID) != -1)
			setCategId(c.getInt(c.getColumnIndex(SplitTransaction.CATEGID)));
		if (c.getColumnIndex(SplitTransaction.SUBCATEGID) != -1)
			setSubCategId(c.getInt(c.getColumnIndex(SplitTransaction.SUBCATEGID)));
		if (c.getColumnIndex(SplitTransaction.SPLITTRANSAMOUNT) != -1) {
//            double amount = c.getDouble(c.getColumnIndex(SPLITTRANSAMOUNT));
            String amount = this.contentValues.getAsString(SplitTransaction.SPLITTRANSAMOUNT);
            setSplitTransAmount(MoneyFactory.fromString(amount));
        }
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(getSplitTransId());
		dest.writeInt(getTransId());
		dest.writeInt(getCategId());
		dest.writeInt(getSubCategId());
		dest.writeString(getSplitTransAmount().toString());
	}
	
	public void readToParcel(Parcel source) {
		setSplitTransId(source.readInt());
		setTransId(source.readInt());
		setCategId(source.readInt());
		setSubCategId(source.readInt());
        String amount = source.readString();
        if (StringUtils.isNotEmpty(amount)) {
            setSplitTransAmount(MoneyFactory.fromString(amount));
        }
	}
	
	public final static Parcelable.Creator<TableSplitTransactions> CREATOR = new Parcelable.Creator<TableSplitTransactions>() {
		public TableSplitTransactions createFromParcel(Parcel source) {
			TableSplitTransactions splitTransactions = new TableSplitTransactions();
			splitTransactions.readToParcel(source);
			return splitTransactions;
		}

		@Override
		public TableSplitTransactions[] newArray(int size) {
			return new TableSplitTransactions[size];
		}
	};
}