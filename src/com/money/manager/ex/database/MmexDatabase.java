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
package com.money.manager.ex.database;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.settings.AppSettings;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Contains functions for manipulating database.
 */
public class MmexDatabase {
    public MmexDatabase(Context context){
        mContext = context;
    }

    private final String LOGCAT = this.getClass().getSimpleName();
    private Context mContext;

    /**
     * Creates a new database file at the default location.
     * @param filename File name for the new database. Extension .mmb will be appended if not
     *                 included in the filename.
     */
    public boolean createDatabase(String filename) {
        boolean result = false;

        try {
            result = createDatabase_Internal(filename);
        } catch (Exception ex) {
            String error = "Error creating database";
            showToast(error, Toast.LENGTH_SHORT);
            Log.e(LOGCAT, ": " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return result;
    }

    private boolean createDatabase_Internal(String filename)
            throws IOException {
        filename = cleanupFilename(filename);

        // it might be enough simply to generate thenew filename and set this as the
        // default database.
        String location = MoneyManagerApplication.getDatabaseDirectory(mContext);
        String newFilePath = location + File.separator + filename;

        // Create db file.
        File dbFile = new File(newFilePath);
        if (dbFile.exists()) {
            showToast(R.string.create_db_exists, Toast.LENGTH_SHORT);
            return false;
        } else {
            dbFile.createNewFile();
        }

//        Core core = new Core(mContext);
//        core.changeDatabase(newFilePath);
        // close connection
        MoneyManagerOpenHelper.getInstance(mContext).close();
        // change database
//        MoneyManagerApplication.setDatabasePath(mContext, newFilePath);

        // store as the default database in settings
        AppSettings settings = new AppSettings(mContext);
        boolean pathSet = settings.getDatabaseSettings().setDatabasePath(newFilePath);
        if (!pathSet) {
            Log.e(LOGCAT, "Error setting the database path.");
            showToast(R.string.create_db_error, Toast.LENGTH_SHORT);
        }

        return pathSet;
    }

    private String cleanupFilename(String filename) {
        // trim any trailing or leading spaces
        filename = filename.trim();

        // check if filename already contains the extension
        boolean containsExtension = Pattern.compile(Pattern.quote(".mmb"), Pattern.CASE_INSENSITIVE)
                .matcher(filename)
                .find();
        if (!containsExtension) {
            filename += ".mmb";
        }

        return filename;
    }

    private void showToast(int resourceId, int duration) {
        Toast.makeText(mContext, resourceId, duration).show();
    }

    private void showToast(String text, int duration) {
        Toast.makeText(mContext, text, duration).show();
    }

}
