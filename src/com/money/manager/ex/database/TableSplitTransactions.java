/*******************************************************************************
 * Copyright (C) 2012 The Android Money Manager Ex Project
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
 ******************************************************************************/
package com.money.manager.ex.database;

public class TableSplitTransactions extends Dataset {
	// FIELDS
	public static final String SPLITTRANSID = "SPLITTRANSID";
	public static final String TRANSID = "TRANSID";
	public static final String CATEGID = "CATEGID";
	public static final String SUBCATEGID = "SUBCATEGID";
	public static final String SPLITTRANSAMOUNT = "SPLITTRANSAMOUNT";

	// CONSTRUCTOR
	public TableSplitTransactions() {
		super("splittransaction_v1", DatasetType.TABLE, "splittransaction");
	}

	@Override
	public String[] getAllColumns() {
		return new String[] {"SPLITTRANSID AS _id", SPLITTRANSID, TRANSID, CATEGID,
				SUBCATEGID, SPLITTRANSAMOUNT };
	}
}
