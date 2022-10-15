/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.os.Build;
import android.os.Environment;

import com.money.manager.ex.MmxContentProvider;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.core.database.DatabaseManager;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.datalayer.InfoRepositorySql;
import com.money.manager.ex.domainmodel.Info;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.home.RecentDatabasesProvider;
import com.money.manager.ex.settings.AppSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import dagger.Lazy;
import timber.log.Timber;

import static com.money.manager.ex.Constants.DEFAULT_DB_FILENAME;

/**
 * Various database-related utility functions
 */
public class MmxDatabaseUtils {

    public static void closeCursor(Cursor c) {
        if (c == null || c.isClosed()) return;

        c.close();
    }

    public static String[] getArgsForId(int id) {
        String[] result = new String[] { Integer.toString(id) };
        return result;
    }

    public static boolean isEncryptedDatabase(String dbPath) {
        return dbPath.contains(".emb");
    }

    public static boolean isValidDbFile(String dbFilePath) {
        File dbFile = new File(dbFilePath);

        if (!dbFile.exists()) return false;
        // extension
        if (!dbFile.getName().endsWith(".mmb")) return false;
        // also add .emb in the future.

        if (!dbFile.canRead()) return false;
        if (!dbFile.canWrite()) return false;

        return true;
    }

    // Dynamic

    @Inject
    public MmxDatabaseUtils(Context context){
        mContext = context;

        // dependency injection
        MmexApplication.getApp().iocComponent.inject(this);
    }

    @Inject Lazy<RecentDatabasesProvider> mDatabasesLazy;
    @Inject Lazy<MmxOpenHelper> openHelper;
    @Inject Lazy<InfoRepositorySql> infoRepositorySqlLazy;
    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    /**
     * Runs SQLite pragma check on the database file.
     * @return A boolean indicating whether the check was successfully completed.
     */
    public boolean checkIntegrity() {
        boolean result = openHelper.get().getReadableDatabase()
                .isDatabaseIntegrityOk();
        return result;
    }

    /**
     * Checks if all the required tables are present.
     * Should be expanded and improved to check for the whole schema.
     * @return A boolean indicating whether the schema is correct.
     */
    public boolean checkSchema() {
        try {
            return checkSchemaInternal();
        } catch (Exception e) {
            Timber.e(e, "checking schema");
            return false;
        }
    }

    public String createDatabase() {
        return createDatabase(DEFAULT_DB_FILENAME);
    }

    /**
     * Creates a new database file at the default location, with the given db file name.
     * @param fileName File name for the new database. Extension .mmb will be appended if not
     *                 included in the fileName. Excludes path!
     *                 If null, a default file name will be used.
     */
    public String createDatabase(@NonNull String fileName) {
        String result = null;

        try {
            result = createDatabase_Internal(fileName);
        } catch (Exception e) {
            Timber.e(e, "creating database");
        }
        return result;
    }

    public boolean fixDuplicates() {
        boolean result = false;

        // check if there are duplicate records in Info Table
        InfoRepositorySql repo = infoRepositorySqlLazy.get(); //new InfoRepositorySql(getContext());
        List<Info> results = repo.loadAll(InfoKeys.DATEFORMAT);
        if (results == null) return false;

        if (results.size() > 1) {
            // delete them, leaving only the first one
            int keepId = results.get(0).getId();

            for(Info toBeDeleted : results) {
                int idToDelete = toBeDeleted.getId();
                if (idToDelete != keepId) {
                    repo.delete(idToDelete);
                }
            }
        } else {
            // no duplicates found
            result = true;
        }

        return result;
    }

    public String makePlaceholders(int len) {
        if (len < 1) {
            // It would lead to an invalid query anyway ..
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
     * Change the database used by the app.
     * Sets the given database path (full path to the file) as the current database. Adds it to the
     * recent files. Resets the data layer.
     * All that is needed after this method is to (re-)start the Main Activity, which will read all
     * the stored preferences.
     * @return Indicator whether the database is valid for use.
     */
    public boolean useDatabase(@NonNull DatabaseMetadata database) {
        // check if the file is a valid database.
        if (!isValidDbFile(database.localPath)) {
            throw new IllegalArgumentException("Not a valid database file!");
        }

        // Set path in preferences.
        new AppSettings(getContext()).getDatabaseSettings()
                .setDatabasePath(database.localPath);

        // Store the Recent Database entry.
        boolean added = mDatabasesLazy.get().add(database);
        if (!added) {
            throw new RuntimeException("could not add to recent files");
        }

        // Switch database in the active data layer.
        MmexApplication.getApp().initDb(database.localPath);

        resetContentProvider();

        return true;
    }

    // Private

    private boolean checkSchemaInternal() {
        boolean result = false;

        // Get the names of all the tables from the generation script.
        ArrayList<String> scriptTables;
        try {
            scriptTables = getAllTableNamesFromGenerationScript();
        } catch (IOException | SQLiteDiskIOException ex) {
            Timber.e(ex, "reading table names from generation script");
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
            new UIHelper(getContext()).showToast(message.toString());
        } else {
            // everything matches
            result = true;
        }

        return result;
    }

    private String createDatabase_Internal(String filename)
        throws IOException {

        filename = cleanupFilename(filename);

        // it might be enough simply to generate the new filename and set it as the default database.
        String location = new DatabaseManager(mContext).getDefaultDatabaseDirectory();

        String newFilePath = location.concat(File.separator).concat(filename);

        // Create db file.
        File dbFile = new File(newFilePath);
        if (dbFile.exists()) {
            throw new RuntimeException(getContext().getString(R.string.create_db_exists));
        }

        if (!dbFile.createNewFile()) {
            throw new RuntimeException(getContext().getString(R.string.create_db_error));
        }

        // close connection
        openHelper.get().close();

        // store as the current database in preferences
        new AppSettings(getContext()).getDatabaseSettings().setDatabasePath(newFilePath);

        return newFilePath;
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
        InputStream inputStream = getContext().getResources().openRawResource(R.raw.tables_v1);
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
     * @return An ArrayList of table details.
     */
    private ArrayList<String> getTableNamesFromDb() {
        SQLiteDatabase db = openHelper.get().getReadableDatabase();

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        ArrayList<String> result = new ArrayList<>();
        int i = 0;
        while (c.moveToNext()) {
            String temp = c.getString(i);
            result.add(temp);
        }
        c.close();

        return result;
    }

    private void resetContentProvider() {
        ContentResolver resolver = getContext().getContentResolver();
        String authority = getContext().getApplicationContext().getPackageName() + ".provider";
        ContentProviderClient client = resolver.acquireContentProviderClient(authority);

        assert client != null;
        MmxContentProvider provider = (MmxContentProvider) client.getLocalContentProvider();

        assert provider != null;
        provider.resetDatabase();

        if (Build.VERSION.SDK_INT >= 24) {
            client.close();
        } else {
            client.release();
        }
    }
}
