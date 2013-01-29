package com.money.manager.ex.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;

import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.TableInfoTable;

public class Core {
	private static final String LOGCAT = Core.class.getSimpleName();
	public static final int INVALID_ATTRIBUTE = -1;
	public static final String INFO_NAME_USERNAME = "USERNAME";
	public static final String INFO_NAME_DATEFORMAT = "DATEFORMAT";
	public static final String INFO_NAME_FINANCIAL_YEAR_START_DAY = "FINANCIAL_YEAR_START_DAY";
	public static final String INFO_NAME_FINANCIAL_YEAR_START_MONTH = "FINANCIAL_YEAR_START_MONTH";
	
	
	private Context context;

	public Core(Context context) {
		super();
		this.context = context;
	}

	/**
	 * Resolves the id attribute in color
	 * 
	 * @param attr id attribute
	 * @return color
	 */
	public int resolveColorAttribute(int attr) {
		return context.getResources().getColor(resolveIdAttribute(attr));
	}
	
	/**
	 * Resolve the id attribute into int value
	 * @param attr id attribute
	 * @return
	 */
	public int resolveIdAttribute(int attr) {
		TypedValue tv = new TypedValue();
		if (context.getTheme().resolveAttribute(attr, tv, true))
			return tv.resourceId;
		else 
			return INVALID_ATTRIBUTE;
	}
	/**
	 * Retrieve value of info
	 * @param info to be retrieve
	 * @return value
	 */
	public String getInfoValue(String info) {
		TableInfoTable infoTable = new TableInfoTable();
		MoneyManagerOpenHelper helper = null;
		Cursor data = null;
		String ret = null;

		try {
			helper = new MoneyManagerOpenHelper(context);
			data = helper.getReadableDatabase().query(infoTable.getSource(), null, TableInfoTable.INFONAME + "=?", new String[] {info}, null, null, null);
			if (data != null && data.moveToFirst()) {
				ret = data.getString(data.getColumnIndex(TableInfoTable.INFOVALUE));
			}
		} catch (Exception e) {
			Log.e(LOGCAT, e.getMessage());
		} finally {
			// close data
			if (data != null)
				data.close();
			if (helper != null)
				helper.close();
		}
		
		return ret;
	}
	/**
	 * Update value of info
	 * @param info to be updated
	 * @param value
	 * @return true if update success otherwise false
	 */
	public boolean setInfoValue(String info, String value) {
		boolean ret = true;
		TableInfoTable infoTable = new TableInfoTable();
		MoneyManagerOpenHelper helper = null;
		ContentValues values = new ContentValues();
		values.put(TableInfoTable.INFOVALUE, value);
		
		try {
			helper = new MoneyManagerOpenHelper(context);
			ret = helper.getWritableDatabase().update(infoTable.getSource(), values, TableInfoTable.INFONAME + "=?", new String[] {info}) >= 0;
		} catch (Exception e) {
			Log.e(LOGCAT, e.getMessage());
			ret = false;
		} finally {
			if (helper != null)
				helper.close();
		}
		
		return ret;
	}
	
	/**
	 * Return arrays of month formatted and localizated
	 * @return arrays of months
	 */
	public String[] getListMonths() {
		return new DateFormatSymbols().getMonths();
	}
	
	public File backupDatabase() {
		File database = new File(MoneyManagerApplication.getDatabasePath(context));
		if (database == null || !database.exists())
			return null;
		//get external storage
		File externalStorage = Environment.getExternalStorageDirectory();
		if (!(externalStorage != null && externalStorage.exists() && externalStorage.isDirectory()))
			return null;
		//create folder to copy database
		File folderOutput = new File(externalStorage + "/" + context.getPackageName());
		//make a directory
		if (!folderOutput.exists()) {
			if (!folderOutput.mkdirs())
				return null;
		}
		//take a folder of database
		ArrayList<File> filesFromCopy = new ArrayList<File>();
		//add current database
		filesFromCopy.add(database);
		//get file journal
		File folder = database.getParentFile();
		if (folder != null) {
			for(File file : folder.listFiles()) {
				if (file.getName().startsWith(database.getName()) && !database.getName().equals(file.getName())) {
					filesFromCopy.add(file);
				}
			}
		}
		//copy all files
		for (int i = 0; i < filesFromCopy.size(); i ++) {
			try {
				copy(filesFromCopy.get(i), new File(folderOutput + "/" + filesFromCopy.get(i).getName()));
			} catch (Exception e) {
				Log.e(LOGCAT, e.getMessage());
				return null;
			}
		}
		
		return new File(folderOutput + "/" + filesFromCopy.get(0).getName());
	}
	
	public void copy(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}
}
