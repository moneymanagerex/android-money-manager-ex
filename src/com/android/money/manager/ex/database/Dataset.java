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
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.android.money.manager.ex.MoneyManagerProvider;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
 */
public abstract class Dataset implements BaseColumns {
	private String source = "";
	private DatasetType type;
	private String basepath = "";
	private String _ID = "ROWID AS _id";
	/**
	 * 
	 * @param source table/view/query
	 * @param type of dataset
	 */
	public Dataset(String source, DatasetType type) {
		this(source, type, "");
	}	
	/**
	 * 
	 * @param source table/view/query
	 * @param type of dataset
	 * @param basepath for match uri
	 */
	public Dataset(String source, DatasetType type, String basepath) {
		this.source = source;
		this.type = type;
		this.basepath = basepath;
	};
	/**
	 * 
	 * @param source table/view/query
	 */
	public void setSource(String source) {
		this.source = source;
	}	
	/**
	 * 
	 * @param basepath to use into contentprovider
	 */
	public void setBasePath(String basepath) {
		this.basepath = basepath;
	};

	/**
	 * 
	 * @param id colonna chiave del dataset
	 */
	public void setID(String id) {
		this._ID = id;
	}
	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
	/**
	 * @return the type
	 */
	public DatasetType getType() {
		return type;
	}
	/**
	 * @return the basepath
	 */
	public String getBasepath() {
		return basepath;
	}
	/**
	 * 
	 * @return the Uri for the content provider
	 */
	public Uri getUri() {
		String parse = "content://" + MoneyManagerProvider.AUTHORITY + "/";
		// check if set basepath
		if (!TextUtils.isEmpty(this.basepath)) {
			//che tye of dataset
			switch (this.type) {
			case TABLE:
				parse.concat("tables/");
				break;
			case QUERY:
				parse.concat("queries/");
				break;
			}
			return Uri.parse(parse.concat(this.basepath));
		} else {
			throw new AssertionError("Internal Error. BasePath is not defined for the dataset");
		}
	}
	/**
	 * 
	 * @return the all columns of the dataset
	 */
	public String[] getAllColumns() {
		return new String[] {""};
	}
	/**
	 * 
	 * @return SQL statment
	 */
	public String getSQL() {
		switch (type) {
		case TABLE: case VIEW:
			return "SELECT " + getAllColumns() + " FROM " + source;
		case QUERY:
			return source;
		default:
			return null;
		}
	}
	/**
	 * Populates the instance of the class to current record the cursor
	 * @param c
	 */
	protected void setValueFromCursor(Cursor c) {
		return;
	}
	/**
	 * check if record of dataset can be delete
	 * @return true if record can delete
	 */
	public boolean canDelete() {
		return true;
	}
}
