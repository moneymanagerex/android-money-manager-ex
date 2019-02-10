package com.money.manager.ex.core.database;

import android.content.Context;
import android.text.TextUtils;

import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.DatabaseSettings;
import com.money.manager.ex.utils.MmxDatabaseUtils;

import java.io.File;

/**
 * The intention is to encapsulate as much of the database file management procedures here.
 */
public class DatabaseManager {
    public DatabaseManager(Context context) {
        mContext = context;
    }

    private Context mContext;

    /**
     * Reads the current database path from the preferences and checks for the existence of the
     * database file.
     * Creates a default database file if the one from preferences is not found. Sets this file as
     * the default database.
     * @return Full path to the current database file.
     */
    public String getDatabasePath() {
        Context context = mContext;

        DatabaseSettings dbSettings = new AppSettings(context).getDatabaseSettings();
        String databasePath = dbSettings.getDatabasePath();

        if (!TextUtils.isEmpty(databasePath)) {
            // Use the db path stored in the preferences.
            File databaseFile = new File(databasePath);
            if (databaseFile.getAbsoluteFile().exists())  {
                return databaseFile.getPath();
            }
        }

        // otherwise try other paths or create the default database.

        String defaultPath = new MmxDatabaseUtils(context).getDefaultDatabasePath();

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
}
