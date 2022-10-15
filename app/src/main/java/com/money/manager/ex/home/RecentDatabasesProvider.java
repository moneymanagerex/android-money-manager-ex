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
package com.money.manager.ex.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.PreferenceConstants;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Provides handling of the recent databases file queue.
 */
@Singleton
public class RecentDatabasesProvider {

    private static final String PREF_KEY = "LIST";

    @Inject
    public RecentDatabasesProvider(MmexApplication app) {
        this.context = app;
        load();
    }

    public LinkedHashMap<String, DatabaseMetadata> map;

    private Context context;

    /**
     * Persists a recent database entry.
     * @param key Local file path is used as the key.
     * @param value The recent database object.
     * @return Indicator if the value was saved successfully.
     */
    public boolean add(String key, DatabaseMetadata value) {
        // check if this item already exist.
        if (contains(value.localPath)) {
            // Put as the last if it does.
            remove(key);
        }

        this.map.put(key, value);

        // If we have more than 5 elements, remove one.
        if (this.map.size() > 5) {
            this.removeOldest();
        }

        this.save();

        return true;
    }

    public boolean add(DatabaseMetadata entry) {
        return add(entry.localPath, entry);
    }

    /**
     * Clears the recent files list. Leaves only the metadata for the current database.
     * @return boolean indicator of success.
     */
    public boolean clear() {
        // keep the current database.
        DatabaseMetadata current = getCurrent();

        this.map.clear();

        // add back the current db
        add(current);
//        this.save();

        MainActivity.setRestartActivity(true);

        return true;
    }

    public int count() {
        return this.map.size();
    }

    public DatabaseMetadata get(String key) {
        return this.map.get(key);
    }

    public Context getContext() {
        return this.context;
    }

    /**
     * find and return the current database
     * @return The current database metadata, if any, or null.
     */
    public DatabaseMetadata getCurrent() {
        String dbPath = new AppSettings(getContext()).getDatabaseSettings().getDatabasePath();
        if (TextUtils.isEmpty(dbPath)) return null;

        if (contains(dbPath)) {
            return map.get(dbPath);
        }

        // otherwise create the default entry for the existing path.
        DatabaseMetadata defaultDb = new DatabaseMetadataFactory(getContext()).createDefaultEntry();
        // and save it to the list
        add(defaultDb);

        return defaultDb;
    }

    public boolean remove(String localPath) {
        DatabaseMetadata existing = get(localPath);
        if (existing != null) {
            this.map.remove(existing);
            return true;
        }
        return false;
    }

    public boolean contains(String path) {
        return this.map.containsKey(path);
    }

    public String readPreference() {
        return getRecentDbPreferences().getString(PREF_KEY, "");
    }

    public void load() {
        String value = readPreference();

        LinkedHashMap<String, DatabaseMetadata> map = null;
        try {
            map = parseStorageContent(value);
        } catch (Exception e) {
            Timber.e(e, "parsing recent databases content");
        }

        if (map == null) {
            this.map = new LinkedHashMap<>();
            // todo: create the default entry for the current database, if any.
        } else {
            this.map = map;
        }
    }

    public void save() {
        String value = toJson();

        getRecentDbPreferences().edit()
                .putString(PREF_KEY, value)
                .apply();
    }

    public String toJson() {
        Gson gson = new Gson();

        String value = gson.toJson(this.map);

        return value;
    }

    public void removeOldest() {
        // remove the first item?
        String firstKey = null;

        for(String key : this.map.keySet()) {
            firstKey = key;
            break;
        }

        this.map.remove(firstKey);
    }

    /*
        private
     */

    private SharedPreferences getRecentDbPreferences() {
        SharedPreferences prefs = getContext().getSharedPreferences(PreferenceConstants.RECENT_DB_PREFERENCES, 0);
        return prefs;
    }

    private LinkedHashMap<String, DatabaseMetadata> parseStorageContent(String value) {
        Type listType = new TypeToken<LinkedHashMap<String, DatabaseMetadata>>() {}.getType();
        Gson gson = new Gson();

        LinkedHashMap<String, DatabaseMetadata> map = gson.fromJson(value, listType);
        return map;
    }

}
