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

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Downloads a CSV file from a URL into a string.
 * This is an example of an async task used to return a value.
 * The methods are in order in which they are executed.
 */
public class DownloadCsvToStringTask
        extends AsyncTask<String, String, String> {

    public DownloadCsvToStringTask(IDownloadAsyncTaskFeedback caller) {
        mCaller = caller;
    }

    private IDownloadAsyncTaskFeedback mCaller;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//        showDialog(progress_bar_type);
    }

    @Override
    protected String doInBackground(String... params) {
        String url = params[0];
        String result = null;

        try {
            TextDownloader downloader = new TextDownloader();
            result = downloader.downloadAsText(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage.
//        pDialog.setProgress(Integer.parseInt(progress[0]));
        mCaller.onProgressUpdate(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        // task completed. Return a value.
        mCaller.onCsvDownloaded(result);
    }

}
