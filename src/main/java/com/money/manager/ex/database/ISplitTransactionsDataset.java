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

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;

import info.javaperformance.money.Money;

/**
 * Interface for Split Category entity. Used by split categories on account transactions
 * and on recurring transactions.
 */
public interface ISplitTransactionsDataset
    extends Parcelable {

    String ACCOUNTID = "ACCOUNTID";
    String CATEGID = "CATEGID";
    String NOTES = "NOTES";
    String PAYEEID = "PAYEEID";
    String STATUS = "STATUS";
    String SUBCATEGID = "SUBCATEGID";
    String TOACCOUNTID = "TOACCOUNTID";
    String TOTRANSAMOUNT = "TOTRANSAMOUNT";
    String TRANSAMOUNT = "TRANSAMOUNT";
    String TRANSCODE = "TRANSCODE";
    String TRANSACTIONNUMBER = "TRANSACTIONNUMBER";
    String TRANSDATE = "TRANSDATE";
    String FOLLOWUPID = "FOLLOWUPID";

    Integer getId();

    Integer getCategId();

    Integer getSubCategId();

    /**
     * @return the splitTransAmount
     */
    Money getSplitTransAmount();

    // Setters

    void setId(int splitTransId);

    void setCategId(int categId);

    void setSubCategId(int subCategId);

    /**
     * @param splitTransAmount the splitTransAmount to set
     */
    void setSplitTransAmount(Money splitTransAmount);
}
