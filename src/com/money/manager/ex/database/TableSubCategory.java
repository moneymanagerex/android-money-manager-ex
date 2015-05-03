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

public class TableSubCategory extends Dataset {
	// FIELD
	public static final String SUBCATEGID = "SUBCATEGID";
	public static final String SUBCATEGNAME = "SUBCATEGNAME";
	public static final String CATEGID = "CATEGID";

	private int subCategId;
	private String subCategName;
	private int categId;
	
	// CONSTRUCTOR
	public TableSubCategory() {
		super("subcategory_v1", DatasetType.TABLE, "subcategory");
	}
	
	@Override
	public String[] getAllColumns() {
		return new String[] {"SUBCATEGID AS _id", SUBCATEGID, SUBCATEGNAME, CATEGID };
	}

	/**
	 * @return the categId
	 */
	public int getCategId() {
		return categId;
	}

	/**
	 * @return the subCategId
	 */
	public int getSubCategId() {
		return subCategId;
	}

	/**
	 * @return the subCategName
	 */
	public String getSubCategName() {
		return subCategName;
	}

	/**
	 * @param categId the categId to set
	 */
	public void setCategId(int categId) {
		this.categId = categId;
	}

	/**
	 * @param subCategId the subCategId to set
	 */
	public void setSubCategId(int subCategId) {
		this.subCategId = subCategId;
	}

	/**
	 * @param subCategName the subCategName to set
	 */
	public void setSubCategName(String subCategName) {
		this.subCategName = subCategName;
	}
}
