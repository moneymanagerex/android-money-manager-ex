/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.core.file;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import timber.log.Timber;

/**
 * Base for exporting any type of text files.
 */
public class TextFileExport {
    public TextFileExport(Context context) {
        mContext = context;
    }

    protected static final String ExportDirectory = "export";
    protected final String LOGCAT = this.getClass().getSimpleName();
    protected Context mContext;

    /**
     * Export text contents as a file.
     * @param fileName The name of the file only (i.e. name + extension).
     * @param contents Text contents to update into the file.
     * @param dialogTitle The title to use for the export binaryDialog.
     * @return Indicator whether the operation was successful.
     * @throws IOException
     */
    public boolean export(String fileName, String contents, String dialogTitle) throws IOException {
        // clear previously exported files.
        this.clearCache();

        // update into temp file.
        File file = createExportFile(fileName);
        if (file == null) {
            Timber.e("Error creating qif file in cache.");
            return false;
        }
        boolean saved = dumpContentIntoFile(contents, file);
        if (!saved) {
            Timber.e("Error saving data into qif file.");
            return false;
        }

        return export(file, dialogTitle);
    }

    /**
     * Export an existing file.
     */
    public boolean export(File file, String dialogTitle) {
        // share file
        offerFile(file, dialogTitle);

        return true;
    }

    /**
     * Delete all existing files in export directory.
     */
    public void clearCache() throws IOException {
        // delete all cached files.
        File path = getExportDirectory();
        File[] files = path.listFiles();

        for(File file : files) {
            file.delete();
        }
    }

    public File createExportFile(String fileName) throws IOException {
        File path = getExportDirectory();

        File file = new File(path, fileName);
        boolean fileCreated = file.createNewFile();
        if (!fileCreated) {
            throw new IOException("Could not create export file!");
        }

        // this is never called in Android: file.deleteOnExit();

        return file;
    }

    private boolean dumpContentIntoFile(String content, File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            Timber.e(e);
            return false;
        }

        return true;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Generates the name of the export directory. Creates the directory if it does not exist.
     * @return A directory into which to temporarily export .qif file.
     * @throws IOException
     */
    private File getExportDirectory() throws IOException {

        File path = new File(getContext().getFilesDir(), ExportDirectory);
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

    private void offerFile(File file, String title) {
        //protected static final String ProviderAuthority = mContext.getApplicationContext().getPackageName() + "com.money.manager.ex.fileprovider";
        String authority = getContext().getApplicationContext().getPackageName() + ".fileprovider";

        Uri contentUri = FileProvider.getUriForFile(getContext(), authority, file);
        offerFile(contentUri, title);
    }

    private void offerFile(Uri fileUri, String title) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);

        Intent chooser = Intent.createChooser(intent, title);
        getContext().startActivity(chooser);
    }

}
