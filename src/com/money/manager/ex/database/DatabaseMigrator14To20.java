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

import android.content.ContentUris;
import android.content.Context;
import android.widget.Toast;

import com.money.manager.ex.MainActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.settings.AppSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * The code for migrating the databases used in v1.4 to use with 2.0.
 * Users who do not use Dropbox have their databases in the old location. V2.0 started
 * using the new location and many users report missing data.
 */
public class DatabaseMigrator14To20 {
    public DatabaseMigrator14To20(Context context) {
        mContext = context;
    }

    public static final String DEFAULT_DB_FILENAME = "data.mmb";

    private Context mContext;

    /**
     * Checks the legacy (v1.4) location for the database.
     * @return boolean indicator whether there is a copy of database at the legacy location.
     */
    public boolean legacyDataExists() {
        String dbPath = getLegacyDbPath();

        // Check if there is a file there.
        File dbFile = new File(dbPath);
        return dbFile.exists();
    }

    public String getLegacyDbPath() {
        String dbPath;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            dbPath = mContext.getApplicationInfo().dataDir;
        } else {
            dbPath = "/data/data/" + mContext.getApplicationContext().getPackageName();
        }
        // This was the default database name and it was impossible to create another one
        // at this location.
//        dbPath += "/databases/data.mmb";
        dbPath += "/databases/";
        // There are other internal databases, like webview.db.

        return dbPath;
    }

    public String getV20Path() {
        Core core = new Core(mContext);
        File newPath = core.getExternalStorageDirectoryApplication();
//        String dbPath = newPath.toString() + "/data.mmb";
        String dbPath = newPath.toString();
        return dbPath;
    }

    /**
     * Copy the database from the old location (v1.4) into the new one (v2.0), set it as the
     * default database, and start using.
     * Does not check for existence. Use legacyDataExists() first.
     * @return boolean indicator if the full operation was successful.
     */
    public boolean migrateLegacyDatabase() {
        boolean result = false;

        String legacyPath = getLegacyDbPath();
        File legacyFile = new File(legacyPath, DEFAULT_DB_FILENAME);

        // copy the legacy database
        String newPath = getV20Path();
        File newFile = new File(newPath, "legacyData.mmb");
        try {
            copy(legacyFile, newFile);
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }

        // rename the legacy database to .bak
        File backupFile = new File(legacyPath, "data.bak");
        boolean renameSuccessful = legacyFile.renameTo(backupFile);
        if (!renameSuccessful) return result;

        // set the database path preference
        AppSettings settings = new AppSettings(mContext);
        String newFilename = newFile.toString();
        settings.getDatabaseSettings().setDatabasePath(newFilename);

        // open the newly copied database
        // set to restart main activity to reload the db.
        MainActivity.setRestartActivity(true);
        Toast.makeText(mContext, R.string.database_migrate_14_to_20_complete, Toast.LENGTH_LONG)
                .show();

        result = true;
        return result;
    }

    public void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }
}
