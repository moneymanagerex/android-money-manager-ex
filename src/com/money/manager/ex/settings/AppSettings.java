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
package com.money.manager.ex.settings;

import android.content.Context;
import android.preference.PreferenceManager;

import com.money.manager.ex.R;

/**
 * This class is used to interact with application settings/preferences.
 * Expand with additional methods as needed.
 */
public class AppSettings extends SettingsBase {

    public AppSettings(Context context) {
        super(context);

    }

    // setting groups

    private GeneralSettings mGeneral;
    private LookAndFeelSettings mLookAndFeel;
    private BehaviourSettings mBehaviour;
    private DatabaseSettings mDatabase;
    private DropboxSettings mDropbox;

    public DatabaseSettings getDatabaseSettings() {
        if (mDatabase == null) {
            mDatabase = new DatabaseSettings(this);
        }
        return mDatabase;
    }

    public GeneralSettings getGeneralSettings() {
        if (mGeneral == null) {
            mGeneral = new GeneralSettings(mContext);
        }
        return mGeneral;
    }

    public LookAndFeelSettings getLookAndFeelSettings() {
        if (mLookAndFeel == null) mLookAndFeel = new LookAndFeelSettings(mContext);

        return mLookAndFeel;
    }

    public BehaviourSettings getBehaviourSettings() {
        if (mBehaviour == null) {
            mBehaviour = new BehaviourSettings(mContext);
        }
        return mBehaviour;
    }

    public DropboxSettings getDropboxSettings() {
        if (mDropbox == null) {
            mDropbox = new DropboxSettings(mContext);
        }
        return mDropbox;
    }

    public Context getContext() {
        return mContext;
    }

    // Individual settings.

    public int getPayeeSort() {
        int sort = get(R.string.pref_sort_payee, 0);
        return sort;
    }

    /**
     * @return the show transaction
     */
    public String getShowTransaction() {
        return get(mContext.getString(R.string.pref_show_transaction),
                mContext.getResources().getString(R.string.last7days));
    }

}
