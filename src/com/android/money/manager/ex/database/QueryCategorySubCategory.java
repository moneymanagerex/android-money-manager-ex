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
package com.android.money.manager.ex.database;

import android.database.Cursor;

import com.android.money.manager.ex.MoneyManagerApplication;


public class QueryCategorySubCategory extends Dataset {
	private static final String SQL = 
		"SELECT CATEGORY_V1.CATEGID, " +
		"CATEGORY_V1.CATEGNAME, " +
		"-1 AS SUBCATEGID, " +
		"NULL AS SUBCATEGNAME, " +
		"CATEGORY_V1.CATEGNAME AS CATEGSUBNAME " +
		"FROM CATEGORY_V1" +
		" UNION " +
		"SELECT CATEGORY_V1.CATEGID, " +
		"CATEGORY_V1.CATEGNAME, " +
		"SUBCATEGORY_V1.SUBCATEGID, " +
		"SUBCATEGORY_V1.SUBCATEGNAME, " +
		"CATEGORY_V1.CATEGNAME || CASE WHEN SUBCATEGORY_V1.SUBCATEGNAME IS NOT NULL THEN ' : ' || SUBCATEGORY_V1.SUBCATEGNAME ELSE '' END AS CATEGSUBNAME " +
		"FROM CATEGORY_V1 INNER JOIN SUBCATEGORY_V1 ON CATEGORY_V1.CATEGID = SUBCATEGORY_V1.CATEGID"; 
	//definizione dei nomi dei campi
	public static final String CATEGID = "CATEGID";
	public static final String CATEGNAME = "CATEGNAME";
	public static final String SUBCATEGID = "SUBCATEGID";
	public static final String SUBCATEGNAME = "SUBCATEGNAME";
	public static final String CATEGSUBNAME = "CATEGSUBNAME";
	//definizione dei campi
	private int categId;
	private String categName;
	private int subCategId;
	private String subCategName;
	private String categSubName;
	// definizione del costruttore
	public QueryCategorySubCategory() {
		super(SQL, DatasetType.QUERY, "categorysubcategory");
	}
	@Override
	public String[] getAllColumns() {
		return new String[] {"0 AS _id", CATEGID, CATEGNAME, SUBCATEGID, SUBCATEGNAME, CATEGSUBNAME};
	}
	public int getCategId() {
		return categId;
	}
	public void setCategId(int categId) {
		this.categId = categId;
	}
	public String getCategName() {
		return categName;
	}
	public void setCategName(String categName) {
		this.categName = categName;
	}
	public int getSubCategId() {
		return subCategId;
	}
	public void setSubCategId(int subCategId) {
		this.subCategId = subCategId;
	}
	public String getSubCategName() {
		return subCategName;
	}
	public void setSubCategName(String subCategName) {
		this.subCategName = subCategName;
	}
	public String getCategSubName() {
		return categSubName;
	}
	public void setCategSubName(String categSubName) {
		this.categSubName = categSubName;
	}
	@Override
	public void setValueFromCursor(Cursor c) {
		// controllo che non sia null il cursore
		if (c == null) { return; }
		// controllo che il numero di colonne siano le stesse
		if (!(c.getColumnCount() == this.getAllColumns().length)) { return; }
		// set dei valori
		this.setCategId(c.getInt(c.getColumnIndex(CATEGID)));
		this.setCategName(c.getString(c.getColumnIndex(CATEGNAME)));
		this.setSubCategId(c.getInt(c.getColumnIndex(SUBCATEGID)));
		this.setSubCategName(c.getString(c.getColumnIndex(SUBCATEGNAME)));
		this.setCategSubName(c.getString(c.getColumnIndex(CATEGSUBNAME)));
	}
}
