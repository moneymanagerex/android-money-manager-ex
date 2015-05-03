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

public class TableAssets extends Dataset {
	//FIELDS
	public static final String ASSETID = "ASSETID";
	public static final String STARTDATE = "STARTDATE";
	public static final String ASSETNAME = "ASSETNAME";
	public static final String VALUE = "VALUE";
	public static final String VALUECHANGE = "VALUECHANGE";
	public static final String NOTES = "NOTES";
	public static final String VALUECHANGERATE = "VALUECHANGERATE";
	public static final String ASSETTYPE = "ASSETTYPE";
	
	public TableAssets() {
		super("assets_v1", DatasetType.TABLE, "assets");
	}
}
