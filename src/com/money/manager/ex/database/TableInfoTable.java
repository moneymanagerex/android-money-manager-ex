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

public class TableInfoTable extends Dataset {
	// FIELD
	public static final String INFOID = "INFOID";
	public static final String INFONAME = "INFONAME";
	public static final String INFOVALUE = "INFOVALUE";
	// CONSTRUCTOR
	public TableInfoTable() {
		super("infotable_v1", DatasetType.TABLE, "infotable");
	}
	//GET ALL COLUMN
	@Override
	public String[] getAllColumns() {
		return new String[] {"INFOID AS _id", INFOID, INFONAME, INFOVALUE};
	}
}
