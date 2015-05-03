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

package com.money.manager.ex.interfaces;

import android.os.Parcelable;

/**
 * Created by Alen on 28/03/2015.
 */
public interface ISplitTransactionsDataset extends Parcelable {

    public int getCategId();

    /**
     * @return the splitTransAmount
     */
    public double getSplitTransAmount();

    public int getSplitTransId();

    public int getSubCategId();

    // Setters

    public void setCategId(int categId);

    /**
     * @param splitTransAmount the splitTransAmount to set
     */
    public void setSplitTransAmount(double splitTransAmount);

    public void setSubCategId(int subCategId);
}
