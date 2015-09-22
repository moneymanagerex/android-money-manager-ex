/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.investment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.currency.CurrenciesActivity;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.utils.DialogUtils;

import java.io.IOException;

/**
 * Task that updates all the security prices in the list.
 */
public class TextDownloaderTask
    extends AsyncTask<String, Integer, Boolean> {

    public TextDownloaderTask(Context context, IDownloadAsyncTaskFeedback feedback) {
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

        DropboxHelper.setAutoUploadDisabled(true);

        // Check if the context still exists to avoid exceptions.
        showProgressDialog();
    }

    @Override
    protected Boolean doInBackground(String... url) {
        try {
            return runTask(url);
        } catch (IllegalArgumentException ex) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(ex, "No results provided by YQL.");
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
//                        mContext.getString(R.string.update_currency_exchange_rates, mCurrencyFormat.getCurrencyName())));
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
            handler.handle(e, "closing dialog");
        }
        if (result) {
            Toast.makeText(mContext, R.string.all_prices_updated, Toast.LENGTH_LONG).show();
        }

        DropboxHelper.setAutoUploadDisabled(false);
        DropboxHelper.notifyDataChanged();

        super.onPostExecute(result);
    }

    private boolean runTask(String... urls) {
        TextDownloader downloader = new TextDownloader();
//        mDialog.setMax(urls.length);

        // Show the symbol being updated?

        for (String url : urls) {
            String content;
            try {
                content = downloader.downloadAsText(url);
            } catch (IOException ex) {
                ExceptionHandler handler = new ExceptionHandler(mContext, this);
                handler.handle(ex, "downloading price");

                return false;
            }
            // notify parent about the price update
            mFeedback.onContentDownloaded(content);

            mProgressCount += 1;
            publishProgress(mProgressCount);
        }

        return Boolean.TRUE;
    }

    private void showProgressDialog() {
        Context context = mContext;

        mDialog = new ProgressDialog(context);

        mDialog.setMessage(context.getString(R.string.starting_price_update));
//        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

}
