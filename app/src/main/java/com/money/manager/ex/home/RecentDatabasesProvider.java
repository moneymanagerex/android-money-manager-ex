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
package com.money.manager.ex.home;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.money.manager.ex.log.ExceptionHandler;
import com.money.manager.ex.settings.PreferenceConstants;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;

import timber.log.Timber;

/**
 * Provides handling of the recent databases file queue.
 */
public class RecentDatabasesProvider {

    private static final String PREF_KEY = "LIST";

    public RecentDatabasesProvider(Context context) {
        this.context = context.getApplicationContext();

        this.load();
    }

    public LinkedHashMap<String, RecentDatabaseEntry> map;

    private Context context;

    public boolean add(String key, RecentDatabaseEntry value) {
        // check if this item already exist.
        if (contains(value)) {
            // Put as the last if it does.
            remove(value);
        }

        this.map.put(key, value);

        // If we have more than 5 elements, remove one.
        if (this.map.size() > 5) {
            this.removeOldest();
        }

        this.save();

        return true;
    }

    public boolean add(RecentDatabaseEntry entry) {
        return add(entry.filePath, entry);
    }

    /**
     * Clears the recent files list.
     * @return boolean indicator of success.
     */
    public boolean clear() {
        this.map.clear();

        this.save();

        return true;
    }

    public RecentDatabaseEntry find(RecentDatabaseEntry entry) {
        RecentDatabaseEntryComparator comparator = new RecentDatabaseEntryComparator();

        for (RecentDatabaseEntry existing : this.map.values()) {
            if (comparator.compare(existing, entry) == 0) {
                return existing;
            }
        }
        return null;
    }

    public boolean remove(RecentDatabaseEntry entry) {
        RecentDatabaseEntry existing = find(entry);
        if (existing != null) {
            this.map.remove(existing);
            return true;
        }
        return false;
    }

    public boolean contains(String path) {
        return this.map.containsKey(path);
    }

    public boolean contains(RecentDatabaseEntry entry) {
        boolean found = false;

        RecentDatabaseEntry existing = find(entry);
        if (existing != null) {
            found = true;
        }

        return found;
    }

    public SharedPreferences getPreferences() {
        SharedPreferences prefs = this.context.getSharedPreferences(PreferenceConstants.RECENT_DB_PREFERENCES, 0);
        return prefs;
    }

    public String readPreference() {
        return getPreferences().getString(PREF_KEY, "");
    }

    public void load() {
        String value = readPreference();

        LinkedHashMap<String, RecentDatabaseEntry> map = null;
        try {
            map = parseStorageContent(value);
        } catch (Exception e) {
            Timber.e(e, "parsing recents");
        }

        if (map == null) {
//            this.queue = new ArrayDeque<>(5);
            this.map = new LinkedHashMap<>();
        } else {
//            this.queue = queue;
            this.map = map;
        }
    }

    private LinkedHashMap<String, RecentDatabaseEntry> parseStorageContent(String value) {
        Type listType = new TypeToken<LinkedHashMap<String, RecentDatabaseEntry>>() {}.getType();
        Gson gson = new Gson();

        LinkedHashMap<String, RecentDatabaseEntry> map = gson.fromJson(value, listType);
        return map;
    }

    public void save() {
        String value = toJson();

        getPreferences().edit()
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
}
