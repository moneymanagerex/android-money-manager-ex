/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.investment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.sync.SyncManager;
import com.money.manager.ex.utils.DialogUtils;

import java.io.IOException;

/**
 * Task that updates all the security prices in the list.
 */
public class YahooDownloadAllPricesTask
    extends AsyncTask<String, Integer, Boolean> {

    public YahooDownloadAllPricesTask(Context context, IDownloadAsyncTaskFeedback feedback) {
        mFeedback = feedback;
        mContext = context;
    }

    private Context mContext;
    private ProgressDialog mDialog = null;
    private IDownloadAsyncTaskFeedback mFeedback;
    private int mProgressCount = 0;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        SyncManager.disableAutoUpload();

        // Check if the context still exists to avoid exceptions.
        showProgressDialog();
    }

    @Override
    protected Boolean doInBackground(String... symbols) {
        try {
            return runTask(symbols);
        } catch (IllegalArgumentException ex) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(ex, "No price provided by Yahoo.");
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error in Yahoo download all prices", e);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        if (mDialog != null) {
            mDialog.setProgress(values[0]);

            // todo: check what this does.
//            if (mCurrencyFormat != null) {
//                mDialog.setMessage(mCore.highlight(mCurrencyFormat.getCurrencyName(),
//                        context.getString(R.string.update_currency_exchange_rates, mCurrencyFormat.getCurrencyName())));
//            }
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        try {
            if (mDialog != null) {
                DialogUtils.closeProgressDialog(mDialog);
            }
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "closing progress dialog");
        }
        if (result) {
            Toast.makeText(mContext, R.string.all_prices_updated, Toast.LENGTH_LONG).show();
        }

        SyncManager.enableAutoUpload();
        SyncManager.dataChanged();

        super.onPostExecute(result);
    }

    private boolean runTask(String... symbols) {
        TextDownloader downloader = new TextDownloader();
        mDialog.setMax(symbols.length);

        for (String symbol:symbols) {
            // Show the symbol being updated.

            String url = mFeedback.getUrlForSymbol(symbol);
            String csv;
            try {
                csv = downloader.downloadAsText(url);
            } catch (IOException iox) {
                ExceptionHandler handler = new ExceptionHandler(getContext());
                handler.handle(iox, "downloading quote");
                return false;
            }
            // notify parent about the price update
            mFeedback.onContentDownloaded(csv);

            mProgressCount += 1;
            publishProgress(mProgressCount);
        }

        return Boolean.TRUE;
    }

    private void showProgressDialog() {
        mDialog = new ProgressDialog(getContext());

        mDialog.setMessage(getContext().getString(R.string.starting_price_update));
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    private Context getContext() {
        return mContext;
    }
}
