package com.money.manager.ex.core;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.database.QueryAllData;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import au.com.bytecode.opencsv.CSVWriter;

public class ExportToCsvFile extends AsyncTask<Void, Void, Boolean> {
	private final String LOGCAT = ExportToCsvFile.class.getSimpleName();
	private Context mContext;
	private AllDataAdapter mAdapter;
	private ProgressDialog dialog;

	private String mFileName = null;
	private String mPrefix = "";

	public ExportToCsvFile(Context context, AllDataAdapter adapter) {
		mContext = context;
		mAdapter = adapter;
		// create progress dialog
		dialog = new ProgressDialog(mContext);
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		if (mAdapter == null || mAdapter.getCursor() == null)
			return false;
		// take cursor
		Cursor data = mAdapter.getCursor();
		// create object to write csv file
		try {
			CSVWriter csvWriter = new CSVWriter(new FileWriter(mFileName), CSVWriter.DEFAULT_SEPARATOR,
					CSVWriter.NO_QUOTE_CHARACTER);
			data.moveToFirst();
			while (!data.isAfterLast()) {
				String[] record = new String[7];
				// compose a records
				record[0] = data.getString(data.getColumnIndex(QueryAllData.UserDate));
				if (!TextUtils.isEmpty(data.getString(data.getColumnIndex(QueryAllData.Payee)))) {
					record[1] = data.getString(data.getColumnIndex(QueryAllData.Payee));
				} else {
					record[1] = data.getString(data.getColumnIndex(QueryAllData.ToAccountName));
				}
				record[2] = Double.toString(data.getDouble(data.getColumnIndex(QueryAllData.TOTRANSAMOUNT)));
				record[3] = data.getString(data.getColumnIndex(QueryAllData.Category));
				record[4] = data.getString(data.getColumnIndex(QueryAllData.Subcategory));
				record[5] = Integer.toString(data.getInt(data.getColumnIndex(QueryAllData.TransactionNumber)));
				record[6] = data.getString(data.getColumnIndex(QueryAllData.Notes));
				// writer record
				csvWriter.writeNext(record);
				// move to next row
				data.moveToNext();
			}
			csvWriter.close();
		} catch (Exception e) {
			Log.e(LOGCAT, e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
		// prompt
		Toast.makeText(mContext,
				mContext.getString(result ? R.string.export_file_complete : R.string.export_file_failed, mFileName),
				Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		// file
		File externalStorage = Environment.getExternalStorageDirectory();
		if (!(externalStorage != null && externalStorage.exists() && externalStorage.isDirectory()))
			return;
		// create folder to copy database
		File folderOutput = new File(externalStorage + "/" + mContext.getPackageName());
		// make a directory
		if (!folderOutput.exists()) {
			if (!folderOutput.mkdirs())
				return;
		}
		String prefix = getPrefixName();
		if (!TextUtils.isEmpty(prefix))
			prefix = prefix + "_";
		// compose file name
		mFileName = folderOutput + "/" + prefix
				+ new SimpleDateFormat("yyyyMMddhhmmss").format(Calendar.getInstance().getTime()) + ".csv";
		// dialog
		dialog.setIndeterminate(true);
		dialog.setMessage(mContext.getString(R.string.export_data_in_progress));
		dialog.show();
	}

	/**
	 * @return the mPrefix
	 */
	public String getPrefixName() {
		if (TextUtils.isEmpty(mPrefix)) {
			return "";
		} else {
			return mPrefix;
		}
	}

	/**
	 * @param mPrefix
	 *            the mPrefix to set
	 */
	public void setPrefixName(String mPrefix) {
		this.mPrefix = mPrefix;
	}
}
