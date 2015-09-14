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
package com.money.manager.ex.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.money.manager.ex.settings.PreferenceConstants;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

/**
 * Provides handling of the recent databases file queue.
 *
 * Created by Alen Siljak on 14/09/2015.
 */
public class RecentDatabasesProvider {

    private static final String PREF_KEY = "LIST";

    public RecentDatabasesProvider(Context context) {
        this.context = context.getApplicationContext();
//        this.queue = new ArrayDeque<>(5);

        this.load();
    }

    public Queue<RecentDatabaseEntry> queue;

    private Context context;

    public boolean add(RecentDatabaseEntry entry) {
        boolean result = false;

        // check if this item already exist.
        if (contains(entry)) {
            // Put as the last if it does.
            remove(entry);
            this.queue.offer(entry);
            return true;
        }

        if (this.queue.add(entry)){
            // If we have more than 5 elements, remove one.
            if (this.queue.size() > 5) {
                this.queue.remove();
                this.queue.offer(entry);
            }

            this.save();

            result = true;
        }
        return result;
    }

    /**
     * Finds the object by comparing the properties, not equating the Java objects.
     * @param entry Entry with properties we are searching for.
     * @return The entry that is inside the queue. Can be used for removal.
     */
    public RecentDatabaseEntry find(RecentDatabaseEntry entry) {
        Queue<RecentDatabaseEntry> queue = this.queue;
        Iterator<RecentDatabaseEntry> iterator = queue.iterator();
        RecentDatabaseEntryComparator comparator = new RecentDatabaseEntryComparator();

        while (iterator.hasNext()) {
            RecentDatabaseEntry existing = iterator.next();
            if (comparator.compare(existing, entry) == 0) {
                return existing;
            }
        }

        return null;
    }

    public boolean remove(RecentDatabaseEntry entry) {
        RecentDatabaseEntry existing = find(entry);
        if (existing != null) {
            return this.queue.remove(existing);
        }
        return false;
    }

    public boolean contains(RecentDatabaseEntry entry) {
        boolean found = false;

        RecentDatabaseEntry existing = find(entry);
        if (existing != null) {
            found = true;
        }

        return found;
    }

//    public Queue<RecentDatabaseEntry> this.queue {
//        return this.queue;
//    }

    public SharedPreferences getPreferences() {
        SharedPreferences prefs = this.context.getSharedPreferences(PreferenceConstants.RECENT_DB_PREFERENCES, 0);
        return prefs;
    }

    public String readPreference() {
        return getPreferences().getString(PREF_KEY, "");
    }

    public void load() {
        String value = readPreference();

        Type listType = new TypeToken<Queue<RecentDatabaseEntry>>() {}.getType();
        Gson gson = new Gson();

        Queue<RecentDatabaseEntry> queue = gson.fromJson(value, listType);

        if (queue == null) {
            this.queue = new ArrayDeque<>(5);
        } else {
            this.queue = queue;
        }
    }

    public void save() {
        String value = toJson();

        getPreferences().edit()
                .putString(PREF_KEY, value)
                .apply();
    }

    public String toJson() {
        Gson gson = new Gson();
        Queue<RecentDatabaseEntry> queue = this.queue;

        String value = gson.toJson(queue);

        return value;
    }
}
