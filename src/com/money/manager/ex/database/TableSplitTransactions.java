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

import com.money.manager.ex.core.DatabaseField;
import com.money.manager.ex.interfaces.ISplitTransactionsDataset;

public class TableSplitTransactions extends Dataset
        implements Parcelable, ISplitTransactionsDataset {

	// FIELD NAMES
	public static final String SPLITTRANSID = "SPLITTRANSID";
	public static final String TRANSID = "TRANSID";
	public static final String CATEGID = "CATEGID";
	public static final String SUBCATEGID = "SUBCATEGID";
	public static final String SPLITTRANSAMOUNT = "SPLITTRANSAMOUNT";
	// definizione dei campi
	@DatabaseField(columnName = SPLITTRANSID)
	private int splitTransId = -1;
	@DatabaseField(columnName = TRANSID)
	private int transId = -1;
	@DatabaseField(columnName = CATEGID)
	private int categId = -1;
	@DatabaseField(columnName = SUBCATEGID)
	private int subCategId = -1;
	@DatabaseField(columnName = SPLITTRANSAMOUNT)
	private double splitTransAmount = 0;
	
	// CONSTRUCTOR
	public TableSplitTransactions() {
		super(TABLE_NAME, DatasetType.TABLE, "splittransaction");
	}

	public static String TABLE_NAME = "SPLITTRANSACTIONS_V1";

	@Override
	public String[] getAllColumns() {
		return new String[] {"SPLITTRANSID AS _id", SPLITTRANSID, TRANSID, CATEGID,
				SUBCATEGID, SPLITTRANSAMOUNT };
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
	public double getSplitTransAmount() {
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
    public void setSplitTransAmount(double splitTransAmount) {
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
		if (c == null) 
			return;
		// set values
		if (c.getColumnIndex(SPLITTRANSID) != -1) 
			setSplitTransId(c.getInt(c.getColumnIndex(SPLITTRANSID)));
		if (c.getColumnIndex(TRANSID) != -1) 
			setTransId(c.getInt(c.getColumnIndex(TRANSID)));
		if (c.getColumnIndex(CATEGID) != -1) 
			setCategId(c.getInt(c.getColumnIndex(CATEGID)));
		if (c.getColumnIndex(SUBCATEGID) != -1) 
			setSubCategId(c.getInt(c.getColumnIndex(SUBCATEGID)));
		if (c.getColumnIndex(SPLITTRANSAMOUNT) != -1) 
			setSplitTransAmount(c.getDouble(c.getColumnIndex(SPLITTRANSAMOUNT)));
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
		dest.writeDouble(getSplitTransAmount());
	}
	
	public void readToParcel(Parcel source) {
		setSplitTransId(source.readInt());
		setTransId(source.readInt());
		setCategId(source.readInt());
		setSubCategId(source.readInt());
		setSplitTransAmount(source.readDouble());
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
		};
	};
}