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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.settings.PreferenceConstants;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides handling of the recent databases file list.
 *
 * Created by Alen Siljak on 14/09/2015.
 */
public class RecentDatabasesProvider {

    private static final String PREF_KEY = "LIST";

    public RecentDatabasesProvider(Context context) {
        this.context = context.getApplicationContext();
        this.list = new ArrayList<>();
    }

    private Context context;
    private ArrayList<RecentDatabaseEntry> list;

    public boolean add(RecentDatabaseEntry entry) {
        boolean result = false;
        if (getList().add(entry)){
            this.save(getList());
            result = true;
        }
        return result;
    }

    public List<RecentDatabaseEntry> getList() {
        return this.list;
    }

    public SharedPreferences getPreferences() {
        SharedPreferences prefs = this.context.getSharedPreferences(PreferenceConstants.RECENT_DB_PREFERENCES, 0);
        return prefs;
    }

//    private ArrayList<RecentDatabaseEntry> getFromJsonArray(JSONArray jsonArray) {
//        ArrayList<RecentDatabaseEntry> list = new ArrayList<>();
//
////        for (int i = 0; i < jsonArray.length(); i++) {
////
////        }
//
//        return list;
//    }

//    private JSONArray getJsonArray(List<RecentDatabaseEntry> list) {
//        JSONArray jsonArray = new JSONArray(list);
//        return jsonArray;
//    }

    public String readPreference() {
        return getPreferences().getString(PREF_KEY, "");
    }

    public List<RecentDatabaseEntry> load() {
        String value = readPreference();

//        JSONArray list = null;
//        try {
//            list = new JSONArray(value);
//        } catch (JSONException e) {
//            ExceptionHandler handler = new ExceptionHandler(this.context, this);
//            handler.handle(e, "parsing recent files json");
//        }

//        if (list == null) {
//            return  null;
//        } else {
//            return getFromJsonArray(list);
//        }

        Type listType = new TypeToken<List<RecentDatabaseEntry>>() {
        }.getType();
        Gson gson = new Gson();
        List<RecentDatabaseEntry> list = gson.fromJson(value, listType);
        return list;
    }

    public void save(List<RecentDatabaseEntry> list) {
        Gson gson = new Gson();
        String value = gson.toJson(list);

        getPreferences().edit()
                .putString(PREF_KEY, value)
                .apply();
    }
}
