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
package com.money.manager.ex.businessobjects;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.money.manager.ex.R;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Handles export of transactions from AllDataFragment into QIF format.
 */
public class QifExport {
    public  QifExport(Context context) {
        this.context = context;
    }

    private Context context;

    /**
     * Export the transactions into qif format and offer file for sharing.
     */
    public void export() {
        // just handle errors here
        try {
            this.export_internal();
        } catch (Exception e) {
            String errorMessage = e.getMessage() == null
                    ? "Error during qif export"
                    : e.getMessage();
            Log.e(this.getClass().getName(), errorMessage);
        }
    }

    private void export_internal() {
        String fileName = generateFileName();

        // get data into qif structure

        // save into file?

        // share file
        Uri contentUri = generateContentUri(fileName);
        offerFile(contentUri);
    }

    private Uri generateContentUri(String fileName) {
        //File imagePath = new File(this.context.getFilesDir(), "export");
        //File file = new File(this.context.getExternalFilesDir(), "export");
        File filePath = new File(this.context.getCacheDir(), "export");
        File newFile = new File(filePath, fileName);

        Uri contentUri = FileProvider.getUriForFile(this.context,
                "com.money.manager.ex.fileprovider", newFile);

        return contentUri;
    }

    private String generateFileName() {
        // use just the date for now?
        Date today = new Date();
        String format = "yyyy-MM-dd_HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
//        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String result = sdf.format(today);

        // append file extension.
        result += ".qif";

        return result;
    }

    private void offerFile(Uri fileUri) {
        String title = this.context.getString(R.string.qif_export);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
//        ClipData clip = new ClipData("file uri", fileUri.toString());
//        intent.setClipData();
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);

        Intent chooser = Intent.createChooser(intent, title);
        this.context.startActivity(chooser);
    }
}
