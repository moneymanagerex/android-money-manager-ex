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

import android.net.Uri;
import android.os.Parcelable;

/**
 * Interface for Split Category entity. Used by split categories on account transactions
 * and on recurring transactions.
 */
public interface ISplitTransactionsDataset
        extends Parcelable {
    // Fields
    public static final String ACCOUNTID = "ACCOUNTID";
    public static final String CATEGID = "CATEGID";
    public static final String NOTES = "NOTES";
    public static final String PAYEEID = "PAYEEID";
    public static final String STATUS = "STATUS";
    public static final String SUBCATEGID = "SUBCATEGID";
    public static final String TOACCOUNTID = "TOACCOUNTID";
    public static final String TOTRANSAMOUNT = "TOTRANSAMOUNT";
    public static final String TRANSAMOUNT = "TRANSAMOUNT";
    public static final String TRANSCODE = "TRANSCODE";
    public static final String TRANSACTIONNUMBER = "TRANSACTIONNUMBER";
    public static final String TRANSDATE = "TRANSDATE";
    public static final String FOLLOWUPID = "FOLLOWUPID";

    Uri getUri();

    int getCategId();

    /**
     * @return the splitTransAmount
     */
    double getSplitTransAmount();

    int getSplitTransId();

    int getSubCategId();

    // Setters

    void setCategId(int categId);

    /**
     * @param splitTransAmount the splitTransAmount to set
     */
    void setSplitTransAmount(double splitTransAmount);

    void setSplitTransId(int splitTransId);

    void setSubCategId(int subCategId);
}
