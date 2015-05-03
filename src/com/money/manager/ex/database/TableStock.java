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

public class TableStock extends Dataset {
	// FIELD
	public static final String STOCKID = "STOCKID";
	public static final String HELDAT = "HELDAT";
	public static final String PURCHASEDATE = "PURCHASEDATE";
	public static final String STOCKNAME = "STOCKNAME";
	public static final String SYMBOL = "SYMBOL";
	public static final String NUMSHARES = "NUMSHARES";
	public static final String PURCHASEPRICE = "PURCHASEPRICE";
	public static final String NOTES = "NOTES";
	public static final String CURRENTPRICE = "CURRENTPRICE";
	public static final String VALUE = "VALUE";
	public static final String COMMISSION = "COMMISSION";

	// CONSTRUCTOR
	public TableStock() {
		super("stock_v1", DatasetType.TABLE, "stock");
	}
	
	@Override
	public String[] getAllColumns() {
		return new String[] { "STOCKID AS _id",
				STOCKID, HELDAT, PURCHASEDATE, STOCKNAME, SYMBOL, NUMSHARES,
				PURCHASEPRICE, NOTES, CURRENTPRICE, VALUE, COMMISSION };
	}
}
