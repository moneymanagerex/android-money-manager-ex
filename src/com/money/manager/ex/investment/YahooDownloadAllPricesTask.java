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
package com.money.manager.ex.investment;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyFormatsListActivity;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.utils.ActivityUtils;
import com.money.manager.ex.utils.CurrencyUtils;

import java.io.IOException;
import java.util.List;

/**
 * Task that updates all the security prices in the list.
 */
public class YahooDownloadAllPricesTask
    extends AsyncTask<String, Integer, Boolean> {

    public YahooDownloadAllPricesTask(Context context, IDownloadAsyncTaskFeedback feedback) {
        mFeedback = feedback;
        mContext = context;
    }

    private final String LOGCAT = this.getClass().getSimpleName();

    private Context mContext;
    private ProgressDialog mDialog = null;
    private IDownloadAsyncTaskFeedback mFeedback;
    private int mProgressCount = 0;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        DropboxHelper.setDisableAutoUpload(true);

        showProgressDialog();
    }

    @Override
    protected Boolean doInBackground(String... symbols) {
        TextDownloader downloader = new TextDownloader();
        mDialog.setMax(symbols.length);

        for (String symbol:symbols) {
            String url = mFeedback.getUrlForSymbol(symbol);
            String csv = null;
            try {
                csv = downloader.downloadAsText(url);
            } catch (IOException iox) {
                Log.e(LOGCAT, iox.getMessage());
                iox.printStackTrace();
                return false;
            }
            // notify parent about the price update
            mFeedback.onCsvDownloaded(csv);

            mProgressCount += 1;
            publishProgress(mProgressCount);
        }

        return Boolean.TRUE;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        if (mDialog != null) {
            //mDialog.setMax(mCountCurrencies);

            mDialog.setProgress(values[0]);

            // todo: check what this does.
//            if (mCurrencyFormat != null) {
//                mDialog.setMessage(mCore.highlight(mCurrencyFormat.getCurrencyName(),
//                        mContext.getString(R.string.update_currency_exchange_rates, mCurrencyFormat.getCurrencyName())));
//            }
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        try {
            if (mDialog != null) mDialog.hide();
        } catch (Exception e) {
            Log.e(CurrencyFormatsListActivity.LOGCAT, e.getMessage());
        }
        if (result) {
            // todo: update text
            Toast.makeText(mContext, R.string.success_currency_exchange_rates, Toast.LENGTH_LONG).show();
        }

        DropboxHelper.setDisableAutoUpload(false);
        DropboxHelper.notifyDataChanged();

//        ActivityUtils.restoreOrientation(getActivity(), mPrevOrientation);

        super.onPostExecute(result);
    }

    private void showProgressDialog() {
        Context context = mContext;

        mDialog = new ProgressDialog(context);

        // todo: update text
        mDialog.setMessage(context.getString(R.string.start_currency_exchange_rates));

        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

}
