package com.money.manager.ex.core.database;

import static com.money.manager.ex.Constants.DEFAULT_DB_FILENAME;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.DatabaseSettings;

import java.io.File;

import timber.log.Timber;

/**
 * The intention is to encapsulate as much of the database file management procedures here.
 */
public class DatabaseManager {
    private final Context mContext;

    public DatabaseManager(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Reads the current database path from the preferences and checks for the existence of the
     * database file.
     * Creates a default database file if the one from preferences is not found. Sets this file as
     * the default database.
     *
     * @return Full path to the current database file.
     */
    public String getDatabasePath() {
        Context context = mContext;

        DatabaseSettings dbSettings = new AppSettings(context).getDatabaseSettings();
        String databasePath = dbSettings.getDatabasePath();

        if (!TextUtils.isEmpty(databasePath)) {
            // Use the db path stored in the preferences.
            File databaseFile = new File(databasePath);
            if (databaseFile.getAbsoluteFile().exists()) {
                return databaseFile.getPath();
            }
        }

        // otherwise try other paths or create the default database.

        String defaultPath = getDefaultDatabasePath();

        // Save db path to preferences.
        dbSettings.setDatabasePath(defaultPath);

        // Show notification
        if (databasePath.equals(defaultPath)) {
            new UIHelper(context).showToast("Default database file will be created at " + defaultPath);
        } else {
            new UIHelper(context).showToast("Database " + databasePath + " not found. Using default:" + defaultPath);
        }

        return defaultPath;
    }

    /**
     * Generates the default database path, including the filename. This is used for database
     * creation and display of the default value during creation.
     *
     * @return The default database path.
     */
    public String getDefaultDatabasePath() {
        return getDefaultDatabaseDirectory()
                .concat(File.separator).concat(DEFAULT_DB_FILENAME);
    }

    /**
     * Gets the directory where the database is (to be) stored. New databases
     * are created here by default.
     * The directory is created if it does not exist.
     * Ref: https://gist.github.com/granoeste/5574148
     *
     * @return the default database directory
     */
    public String getDefaultDatabaseDirectory() {
        File defaultFolder;

        // try with the external storage first.
        defaultFolder = getDbExternalStorageDirectory();
        if (defaultFolder != null) return defaultFolder.getAbsolutePath();

        defaultFolder = getExternalFilesDirectory();
        if (defaultFolder != null) return defaultFolder.getAbsolutePath();

        // Then use files dir.
        defaultFolder = getPackageDirectory();
        if (defaultFolder != null) return defaultFolder.getAbsolutePath();

        return null;
    }

    /**
     * /sdcard/MoneyManagerEx
     *
     * @return the location for the database in the publicly accessible storage
     */
    private File getDbExternalStorageDirectory() {
        // sdcard
        File externalStorageDirectory = Environment.getExternalStorageDirectory();

        if (externalStorageDirectory == null) return null;
        if (!externalStorageDirectory.exists() || !externalStorageDirectory.isDirectory() || !externalStorageDirectory.canWrite()) {
            return null;
        }

        // now create the app's directory in the root.

        String defaultPath = externalStorageDirectory.getAbsolutePath()
                .concat(File.separator).concat("MoneyManagerEx");
        File defaultFolder = new File(defaultPath);
        if (defaultFolder.exists() && defaultFolder.canRead() && defaultFolder.canWrite()) return defaultFolder;

        if (!defaultFolder.exists()) {
            // create the directory.
            if (!defaultFolder.mkdirs()) {
                Timber.w("could not create the storage directory %s", defaultPath);
                return null;
            }
        }

        return defaultFolder;
    }

    /**
     * External files directory
     * /storage/sdcard0/Android/data/package/files
     *
     * @return directory to store the database in external files dir.
     */
    private File getExternalFilesDirectory() {
        File externalFilesDir = getContext().getExternalFilesDir(null);
        if (externalFilesDir == null) return null;

        String dbString = externalFilesDir.getAbsolutePath().concat(File.separator)
                .concat("databases");
        File dbPath = new File(dbString);

        if (dbPath.exists() && dbPath.canRead() && dbPath.canWrite()) return dbPath;

        if (!dbPath.mkdir()) {
            Timber.w("could not create databases directory in external files");
            return null;
        }

        return dbPath;
    }

    /**
     * @return app's files directory
     */
    private File getPackageDirectory() {
        // getFilesDir() = /data/data/package/files
        File packageLocation = getContext().getFilesDir().getParentFile();
        // or: getContext().getApplicationInfo().dataDir
        // getContext().getFilesDir()
        //                internalFolder = "/data/data/" + getContext().getApplicationContext().getPackageName();

        String dbDirectoryPath = packageLocation.getAbsolutePath()
                .concat(File.separator)
                .concat("databases");

        File dbDirectory = new File(dbDirectoryPath);
        if (dbDirectory.exists() && dbDirectory.canRead() && dbDirectory.canWrite()) return dbDirectory;

        if (!dbDirectory.exists()) {
            if (!dbDirectory.mkdir()) {
                Timber.w("could not create databases directory");
                return null;
            }
        }

        return dbDirectory;
    }

}
