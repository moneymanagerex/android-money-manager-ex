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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.money.manager.ex.R;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Handles export of transactions from AllDataFragment into QIF format.
 */
public class QifExport {
    private static final String ProviderAuthority = "com.money.manager.ex.fileprovider";
    private static final String ExportDirectory = "export";

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
//            String errorMessage = e.getMessage() == null
//                    ? "Error during qif export"
//                    : e.getMessage();
//            Log.e(this.getClass().getName(), errorMessage);
            e.printStackTrace();
        }
    }

    private void clearCache() {
        // todo: delete all files in cache directory.
        // fileList()
        // deleteFile()
    }

    private void export_internal() {
        // todo: get data into qif structure
        String content = "test";

        // todo: save into file?
        File file = createExportFile();
        dumpTransactionsIntoFile(content, file);

        // share file
        Uri contentUri = generateContentUri(file);
        offerFile(contentUri);

        // delete local file
        file.delete();
    }

    private void dumpTransactionsIntoFile(String content, File file) {
        // todo: files created this way are located in private files, not cache!
        try {
            FileOutputStream stream = this.context.openFileOutput(
                    file.getName(), Context.MODE_PRIVATE);
            // use Context.MODE_PRIVATE for private-only files. Context.MODE_APPEND

            stream.write(content.getBytes());
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File createExportFile() {
        String fileName = generateFileName();

        //File imagePath = new File(this.context.getFilesDir(), ExportDirectory);
        //File file = new File(this.context.getExternalFilesDir(), ExportDirectory);
        File filePath = new File(this.context.getCacheDir(), ExportDirectory);

        File newFile = new File(filePath, fileName);
//        File.createTempFile();

        return newFile;
    }

    private Uri generateContentUri(File file) {
        Uri contentUri = FileProvider.getUriForFile(this.context, ProviderAuthority, file);

        return contentUri;
    }

    private String generateFileName() {
        // use just the date for now?
        Date today = new Date();
        String format = "yyyy-MM-dd_HHmm";
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
