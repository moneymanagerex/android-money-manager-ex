/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.utils;

import android.content.Context;
import android.database.Cursor;
//import net.sqlcipher.database.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.database.MmexOpenHelper;
import com.money.manager.ex.datalayer.InfoRepository;
import com.money.manager.ex.settings.AppSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Various database-related utility functions
 */
public class MyDatabaseUtils {

    public static String[] getArgsForId(int id) {
        String[] result = new String[] { Integer.toString(id) };
        return result;
    }

    public static boolean isEncryptedDatabase(String dbPath) {
        return dbPath.contains(".emb");
    }

    public MyDatabaseUtils(Context context){
        mContext = context.getApplicationContext();
    }

    private Context mContext;

    /**
     * Runs SQLite pragma check on the database file.
     * @return A boolean indicating whether the check was successfully completed.
     */
    public boolean checkIntegrity() {
        SQLiteDatabase db = MmexOpenHelper.getInstance(getContext())
            .getReadableDatabase();

        boolean result = db.isDatabaseIntegrityOk();
        return result;
    }

    public Context getContext() {
        return mContext;
    }

    public String makePlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    /**
     * Checks if all the required tables are present.
     * Should be expanded and improved to check for the whole schema.
     * @return A boolean indicating whether the schema is correct.
     */
    public boolean checkSchema() {
        boolean result = false;

        // Get the names of all the tables from the generation script.
        ArrayList<String> scriptTables;
        try {
            scriptTables = getAllTableNamesFromGenerationScript();
        } catch (IOException | SQLiteDiskIOException ex) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(ex, "reading table names from generation script");

            return false;
        }

        // get the list of all the tables from the database.
        ArrayList<String> existingTables = getTableNamesFromDb();

        // compare. retainAll, removeAll, addAll
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
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(ex, "creating database");
        }
        return result;
    }

    public boolean fixDuplicates() {
        boolean result = false;

        // todo: check if there are duplicate records in Info Table
        InfoRepository repo = new InfoRepository(getContext());
        //repo.

        // todo: delete them?

        return result;
    }

    // Private

    private boolean createDatabase_Internal(String filename)
            throws IOException {
        filename = cleanupFilename(filename);

        // it might be enough simply to generate the new filename and set it as the default database.
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

        // close connection
        MmexOpenHelper.getInstance(mContext).close();

        // change database
        // store as the default database in settings
        AppSettings settings = new AppSettings(mContext);
        boolean pathSet = settings.getDatabaseSettings().setDatabasePath(newFilePath);
        if (!pathSet) {
            Log.e(this.getClass().getSimpleName(), "Error setting the database path.");
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
        SQLiteDatabase db = MmexOpenHelper.getInstance(mContext)
                .getReadableDatabase();

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        ArrayList<String> result = new ArrayList<>();
        int i = 0;
        while (c.moveToNext()) {
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
