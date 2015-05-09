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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Handles export of transactions from AllDataFragment into QIF format.
 */
public class QifExport {
    private static final String ProviderAuthority = "com.money.manager.ex.fileprovider";
    private static final String ExportDirectory = "export";
    private static final String QifExtension = ".qif";

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
            e.printStackTrace();
        }
    }

    private void export_internal() throws IOException {
        // todo: get data into qif structure
        String content = "test";

        // save into temp file.
        File file = createExportFile();
        if (file == null) {
            Log.e(this.getClass().getSimpleName(), "Error creating qif file in cache.");
            return;
        }
        boolean saved = dumpTransactionsIntoFile(content, file);
        if (!saved) {
            Log.e(this.getClass().getSimpleName(), "Error saving data into qif file.");
            return;
        }

        // share file
        Uri contentUri = FileProvider.getUriForFile(this.context, ProviderAuthority, file);
        offerFile(contentUri);

        // delete local file
//        file.delete();
        file.deleteOnExit();
    }

//    private void dumpTransactionsIntoFile(String content, File file) {
//        // files created this way are located in private files, not cache!
//        try {
//            FileOutputStream stream = this.context.openFileOutput(
//                    file.getName(), Context.MODE_PRIVATE);
//            // use Context.MODE_PRIVATE for private-only files. Context.MODE_APPEND
//
//            stream.write(content.getBytes());
//            stream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private boolean dumpTransactionsIntoFile(String content, File file) {
//        boolean result;
//
//        try {
//            result = file.createNewFile();
//            if(!result) {
//                throw new Exception("Error creating file!");
//            }
//
//            FileWriter writer = new FileWriter(file);
//            writer.write(content);
//            writer.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            result = false;
//        }
//
//        return result;
//    }

    private boolean dumpTransactionsIntoFile(String content, File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private File createExportFile() throws IOException {
        String fileName = generateFileName();

        //File imagePath = new File(this.context.getFilesDir(), ExportDirectory);
        //File file = new File(this.context.getExternalFilesDir(), ExportDirectory);
//        File filePath = new File(this.context.getCacheDir(), ExportDirectory);

//        File newFile = new File(filePath, fileName);

        File tempFile = null;
        tempFile = File.createTempFile(fileName, ".qif", this.context.getCacheDir());
//            Log.d(this.getClass().getSimpleName(), tempFile.toString());
        tempFile.deleteOnExit();

        //tempFile.setWritable(true);

        return tempFile;
    }

    private String generateFileName() {
        // use just the date for now?
        Date today = new Date();
        String format = "yyyy-MM-dd_HHmmss";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
//        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String result = sdf.format(today);

        // append file extension.
//        result += ".qif";

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
