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
package com.money.manager.ex.core.file;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.money.manager.ex.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Base for exporting any type of text files.
 */
public class TextFileExport {
    public TextFileExport(Context context) {
        mContext = context;
    }

    protected static final String ProviderAuthority = "com.money.manager.ex.fileprovider";
    protected static final String ExportDirectory = "export";
    protected final String LOGCAT = this.getClass().getSimpleName();
    protected Context mContext;

    public boolean export(String fileName, String contents)
            throws IOException {
        // clear previously exported files.
        this.clearCache();
        // save into temp file.
        File file = createExportFile(fileName);
        if (file == null) {
            Log.e(this.LOGCAT, "Error creating qif file in cache.");
            return false;
        }
        boolean saved = dumpContentIntoFile(contents, file);
        if (!saved) {
            Log.e(this.LOGCAT, "Error saving data into qif file.");
            return false;
        }

        // share file
        offerFile(file);

        return true;
    }

    /**
     * Delete all existing files in export directory.
     * @throws Exception
     */
    private void clearCache()
            throws IOException {
        // delete all cached files.
        File path = getExportDirectory();
        File[] files = path.listFiles();

        for(File file : files) {
            file.delete();
        }
    }

    private File createExportFile(String fileName)
            throws IOException {
        File path = getExportDirectory();

//        tempFile = File.createTempFile(fileName, ".qif", path);

        File file = new File(path, fileName);
        boolean fileCreated = file.createNewFile();
        if (!fileCreated) {
            throw new IOException("Could not create export file!");
        }

        file.deleteOnExit();

        return file;
    }

    private boolean dumpContentIntoFile(String content, File file) {
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

    /**
     * Generates the name of the export directory. Creates the directory if it does not exist.
     * @return A directory into which to temporarily export .qif file.
     * @throws IOException
     */
    private File getExportDirectory()
            throws IOException {
        File path = new File(mContext.getFilesDir(), ExportDirectory);
//        File path = new File(this.context.getExternalFilesDir(null), ExportDirectory);
//        File path = this.context.getExternalFilesDir(null);
//        File path = new File(this.context.getCacheDir(), ExportDirectory);

        // Create output directory if it does not exist.
        if (!path.exists()) {
            boolean directoryCreated = path.mkdir();
            if(!directoryCreated) {
                throw new IOException("Could not create export directory!");
            }
        }

        return path;
    }

    private void offerFile(File file) {
        Uri contentUri = FileProvider.getUriForFile(mContext, ProviderAuthority, file);
        offerFile(contentUri);
    }

    private void offerFile(Uri fileUri) {
        String title = mContext.getString(R.string.qif_export);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);

        Intent chooser = Intent.createChooser(intent, title);
        mContext.startActivity(chooser);
    }

}
