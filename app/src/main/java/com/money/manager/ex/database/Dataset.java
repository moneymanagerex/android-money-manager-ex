/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.money.manager.ex.MmxContentProvider;

/**
 * Dataset
 */
public abstract class Dataset
	implements BaseColumns {

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
	}

	private String source = "";
	private DatasetType type;
	private String basepath = "";

	/**
	 * All columns of the dataset.
	 * @return the all columns of the dataset
	 */
	public abstract String[] getAllColumns();

	/**
	 * @return the basepath
	 */
	public String getBasepath() {
		return basepath;
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
	 * 
	 * @return the Uri for the content provider
	 */
	public Uri getUri() {
		String baseUri = "content://" + MmxContentProvider.getAuthority() + "/";

		if (!TextUtils.isEmpty(this.basepath)) {
//			switch (this.type) {
//                case TABLE:
//                    // todo: inspect what was the intention here. The result of the operation is ignored.
////                    baseUri.concat("tables/");
//                    break;
//                case QUERY:
//                    // todo: inspect what was the intention here. The result of the operation is ignored.
////                    baseUri.concat("queries/");
//                    break;
//                default:
//                    break;
//			}
			String fullUri = baseUri.concat(this.basepath);
			return Uri.parse(fullUri);
		} else {
			throw new AssertionError("Internal Error. BasePath is not defined for the dataset");
		}
	}

//	/**
//	 *
//	 * @param basepath to use into contentprovider
//	 */
//	public void setBasePath(String basepath) {
//		this.basepath = basepath;
//	}

	/**
	 *
	 * @param source table/view/query
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Populates the instance of the class to current record the cursor
	 * @param c cursor
	 */
	protected void setValueFromCursor(Cursor c) {	}
}
