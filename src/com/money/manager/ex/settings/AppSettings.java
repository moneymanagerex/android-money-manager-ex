/*
 * Copyright (C) 2012-2015 Money Manager Ex project
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
package com.money.manager.ex.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.money.manager.ex.R;

/**
 * This class is used to interact with application settings/preferences.
 * Expand with additional methods as needed.
 */
public class AppSettings {

    public GeneralSettings General;

    public AppSettings(Activity activity) {
        mLinkedActivity = activity;
        init();
    }

    private Activity mLinkedActivity;
    private SharedPreferences mSettings;
    private SharedPreferences.Editor mEditor;

    public boolean get(String key, boolean defaultValue) {
        return mSettings.getBoolean(key, defaultValue);
    }

    public boolean getHideReconciledAmounts() {
        String key = mLinkedActivity.getString(R.string.pref_transaction_hide_reconciled_amounts);
        return this.get(key, false);
    }

    /**
     * Save string value to settings.
     * @param key
     * @param value
     */
    public void set(String key, String value) {
        mEditor.putString(key, value);
        mEditor.commit();
    }

    public void set(String key, boolean value) {
        mEditor.putBoolean(key, value);
        mEditor.commit();
    }

    private void init() {
        Context context = mLinkedActivity.getApplicationContext();
        mSettings = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mSettings.edit();

        this.General = new GeneralSettings(mLinkedActivity);
    }

}
