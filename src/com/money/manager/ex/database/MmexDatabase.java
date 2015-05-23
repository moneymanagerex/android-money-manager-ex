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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.settings.AppSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

    /**
     * Runs SQLite pragma check on the database file.
     * @return A boolean indicating whether the check was successfully completed.
     */
    public boolean checkIntegrity() {
        SQLiteDatabase db = MoneyManagerOpenHelper.getInstance(mContext)
                .getReadableDatabase();

        boolean result = db.isDatabaseIntegrityOk();
        return result;
    }

    /**
     * Checks if all the required tables are present.
     * Should be expanded and improved to check for the whole schema.
     * @return A boolean indicating whether the schema is correct.
     */
    public boolean checkSchema() {
        boolean result = false;

        // Get the names of all the tables from the generation script.
        ArrayList<String> scriptTables = null;
        try {
            scriptTables = getAllTableNamesFromGenerationScript();
        } catch (IOException ioex) {
            String error = "Error reading table names from generation script";
            Log.e(LOGCAT, error + ": " + ioex.getLocalizedMessage());
            ioex.printStackTrace();
            showToast(error, Toast.LENGTH_SHORT);

            return false;
        }

        // get the list of all the tables from the database.
        ArrayList<String> existingTables = getTableNamesFromDb();

        // compare. retainAll, remaveAll, addAll
        scriptTables.removeAll(existingTables);
        // If there is anything left, the script schema has more tables than the db.
        if (!scriptTables.isEmpty()) {
            StringBuilder message = new StringBuilder("Tables missing: ");
            for(String table:scriptTables) {
                message.append(table);
                message.append(" ");
            }
            showToast(message.toString(), Toast.LENGTH_LONG);
        } else {
            // everything matches
            result = true;
        }

        return result;
    }

    private ArrayList<String> getAllTableNamesFromGenerationScript()
            throws IOException {
        InputStream inputStream = mContext.getResources().openRawResource(R.raw.database_create);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = reader.readLine();
        String textToMatch = "create table";
        ArrayList<String> tableNames = new ArrayList<>();

        while (line != null) {
            boolean found = false;
            if (line.contains(textToMatch)) found = true;
            if (!found && line.contains(textToMatch.toUpperCase())) found = true;

            if (found) {
                // extract the table name from the instruction
                line = line.replace(textToMatch, "");
                line = line.replace(textToMatch.toUpperCase(), "");
                line = line.replace("(", "");
                // remove any empty spaces.
                line = line.trim();

                tableNames.add(line);
            }

            line = reader.readLine();
        }

        return tableNames;
    }

    /**
     * Get all table Details from teh sqlite_master table in Db.
     *
     * @return An ArrayList of table details.
     */
    private ArrayList<String> getTableNamesFromDb() {
        SQLiteDatabase db = MoneyManagerOpenHelper.getInstance(mContext)
                .getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table'", null);
        ArrayList<String> result = new ArrayList<>();
        int i = 0;
//        result.add(c.getColumnNames());
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
//            String[] temp = new String[c.getColumnCount()];
//            for (i = 0; i < temp.length; i++) {
//                temp[i] = c.getString(i);
//            }
            String temp = c.getString(i);
            result.add(temp);
        }

        return result;
    }

    private void showToast(int resourceId, int duration) {
        Toast.makeText(mContext, resourceId, duration).show();
    }

    private void showToast(String text, int duration) {
        Toast.makeText(mContext, text, duration).show();
    }

}
