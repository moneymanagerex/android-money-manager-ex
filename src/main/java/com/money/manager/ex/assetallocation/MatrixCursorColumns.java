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
package com.money.manager.ex.assetallocation;

import com.money.manager.ex.domainmodel.AssetClass;

/**
 * Column names for the matrix cursor for Asset Allocation
 */
public class MatrixCursorColumns {
    public static final String ID = "_id";
    public static final String NAME =  AssetClass.NAME;
    public static final String ALLOCATION =  AssetClass.ALLOCATION;
    public static final String VALUE =  "Value";
    public static final String CURRENT_ALLOCATION = "CurrentAllocation";
    public static final String CURRENT_VALUE =  "CurrentValue";
    public static final String DIFFERENCE =  "Difference";
}
