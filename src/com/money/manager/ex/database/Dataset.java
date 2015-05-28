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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.MoneyManagerProvider;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
 */
@SuppressWarnings("unused")
public abstract class Dataset
		implements BaseColumns {

	private static final String LOGCAT = Dataset.class.getSimpleName();

	// member private of class
	private String source = "";
	private DatasetType type;
	private String basepath = "";
	private String _ID = "ROWID AS _id";
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

	/**
	 * The default check in CheckingAccount. If checked to another table use canDelete(Context context, ContentValues values, String className)
	 * @param context context from call
	 * @param values to compose filter
	 * @return true if can delete
	 */
	public boolean canDelete(Context context, ContentValues values) {
		return canDelete(context, values, TableCheckingAccount.class.getName());
	}	
	/**
	 * 
	 * @param context context from call
	 * @param values to compose filter
	 * @param className name dataset to check
	 * @return true if can delete
	 */
	public boolean canDelete(Context context, ContentValues values, String className) {
		// check if content values is populate
		if (values.size() < 0) {
			return true;
		}
		// compose filter
		String selection = "";
		List<String> selectionArgs = new ArrayList<>();
		
		for(Entry<String, Object> entry : values.valueSet()) {
			if (!(TextUtils.isEmpty(selection))) {
				selection += " AND ";
			}
			selection += entry.getKey() + "=?";
			selectionArgs.add(entry.getValue().toString());
		}
		// create dynamic dataset
		@SuppressWarnings("rawtypes")
		Class[] classParm = null;
		Object[] objectParm = null;
		Dataset dataset;
		try {
			Class<?> cls = Class.forName(className);
			Constructor<?> cnt = cls.getConstructor(classParm);
			dataset = (Dataset) cnt.newInstance(objectParm);
		} catch (Exception e) {
			Log.e(LOGCAT, e.getMessage());
			return false;
		}
		// check if dataset is created
		if (dataset == null) {
			if (BuildConfig.DEBUG) Log.d(LOGCAT, "Dataset is not created dynamic. Force return false");
			return false;
		}
		// check if referenced
		Cursor cursor = context.getContentResolver().query(dataset.getUri(), null,
				selection, selectionArgs.toArray(new String[selectionArgs.size()]), null);
		if (cursor != null && cursor.getCount() <= 0) {
			cursor.close();
			return true;
		} else {
			return false;
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
	 * 
	 * @return SQL statment
	 */
	public String getSQL() {
		switch (type) {
		case TABLE:
		case VIEW:
			return "SELECT " + getAllColumns() + " FROM " + source;
		case QUERY:
			return source;
		default:
			return null;
		}
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
		String parse = "content://" + MoneyManagerProvider.getAuthority() + "/";
		// check if set basepath
		if (!TextUtils.isEmpty(this.basepath)) {
			//che tye of dataset
			switch (this.type) {
			case TABLE:
				// todo: inspect what was the intention here. The result of the operation is ignored.

				parse.concat("tables/");
				break;
			case QUERY:
				// todo: inspect what was the intention here. The result of the operation is ignored.

				parse.concat("queries/");
				break;
			default:
				break;
			}
			return Uri.parse(parse.concat(this.basepath));
		} else {
			throw new AssertionError("Internal Error. BasePath is not defined for the dataset");
		}
	}
	/**
	 * 
	 * @param basepath to use into contentprovider
	 */
	public void setBasePath(String basepath) {
		this.basepath = basepath;
	}
	/**
	 * 
	 * @param id colonna chiave del dataset
	 */
	public void setID(String id) {
		this._ID = id;
	}
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
