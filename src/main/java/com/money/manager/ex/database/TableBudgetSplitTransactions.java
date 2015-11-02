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
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.money.manager.ex.core.DatabaseField;

import org.apache.commons.lang3.StringUtils;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

public class TableBudgetSplitTransactions
        extends Dataset
        implements Parcelable, ISplitTransactionsDataset {

	// FIELDS
	public static final String SPLITTRANSID = "SPLITTRANSID";
	public static final String TRANSID = "TRANSID";
	public static final String CATEGID = "CATEGID";
	public static final String SUBCATEGID = "SUBCATEGID";
	public static final String SPLITTRANSAMOUNT = "SPLITTRANSAMOUNT";

    @DatabaseField(columnName = SPLITTRANSID)
    private int splitTransId = -1;
    @DatabaseField(columnName = TRANSID)
    private int transId = -1;
    @DatabaseField(columnName = CATEGID)
    private int categId = -1;
    @DatabaseField(columnName = SUBCATEGID)
    private int subCategId = -1;
    @DatabaseField(columnName = SPLITTRANSAMOUNT)
    private Money splitTransAmount = MoneyFactory.fromString("0");

    ContentValues contentValues;

    // CONSTRUCTOR
	public TableBudgetSplitTransactions() {
		super("budgetsplittransactions_v1", DatasetType.TABLE, "budgetsplittransactions");

        this.contentValues = new ContentValues();
	}

    @Override
    public Uri getUri(Context context) {
        return getUri();
    }

	@Override
	public String[] getAllColumns() {
		return new String[] { "SPLITTRANSID AS _id", SPLITTRANSID, TRANSID, CATEGID, SUBCATEGID, SPLITTRANSAMOUNT};
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

        // todo: DatabaseUtils.cursorRowToContentValues(c, this.contentValues);

		// set values
		if (c.getColumnIndex(SPLITTRANSID) != -1) 
			setSplitTransId(c.getInt(c.getColumnIndex(SPLITTRANSID)));
		if (c.getColumnIndex(TRANSID) != -1) 
			setTransId(c.getInt(c.getColumnIndex(TRANSID)));
		if (c.getColumnIndex(CATEGID) != -1) 
			setCategId(c.getInt(c.getColumnIndex(CATEGID)));
		if (c.getColumnIndex(SUBCATEGID) != -1) 
			setSubCategId(c.getInt(c.getColumnIndex(SUBCATEGID)));
		if (c.getColumnIndex(SPLITTRANSAMOUNT) != -1) {
            double amount = c.getDouble(c.getColumnIndex(SPLITTRANSAMOUNT));
            setSplitTransAmount(MoneyFactory.fromDouble(amount));
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

    public final static Parcelable.Creator<TableBudgetSplitTransactions> CREATOR = new Parcelable.Creator<TableBudgetSplitTransactions>() {
        public TableBudgetSplitTransactions createFromParcel(Parcel source) {
            TableBudgetSplitTransactions splitTransactions = new TableBudgetSplitTransactions();
            splitTransactions.readToParcel(source);
            return splitTransactions;
        }

        @Override
        public TableBudgetSplitTransactions[] newArray(int size) {
            return new TableBudgetSplitTransactions[size];
        };
    };
}
