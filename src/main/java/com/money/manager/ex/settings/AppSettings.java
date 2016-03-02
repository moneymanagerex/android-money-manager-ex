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
package com.money.manager.ex.settings;

import android.content.Context;
import android.preference.PreferenceManager;

import com.money.manager.ex.R;

/**
 * This class is used to interact with application settings/preferences.
 * Expand with additional methods as needed.
 */
public class AppSettings
    extends SettingsBase {

    public AppSettings(Context context) {
        super(context);

    }

    // setting groups

    private GeneralSettings mGeneral;
    private LookAndFeelSettings mLookAndFeel;
    private BehaviourSettings mBehaviour;
    private InvestmentSettings mInvestment;
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
            mGeneral = new GeneralSettings(getContext());
        }
        return mGeneral;
    }

    public LookAndFeelSettings getLookAndFeelSettings() {
        if (mLookAndFeel == null) mLookAndFeel = new LookAndFeelSettings(getContext());

        return mLookAndFeel;
    }

    public BehaviourSettings getBehaviourSettings() {
        if (mBehaviour == null) {
            mBehaviour = new BehaviourSettings(getContext());
        }
        return mBehaviour;
    }

    public InvestmentSettings getInvestmentSettings() {
        if (mInvestment == null) {
            mInvestment = new InvestmentSettings(getContext());
        }
        return mInvestment;
    }

    public DropboxSettings getDropboxSettings() {
        if (mDropbox == null) {
            mDropbox = new DropboxSettings(getContext());
        }
        return mDropbox;
    }

    // Individual settings.

    public int getPayeeSort() {
        int sort = get(R.string.pref_sort_payee, 0);
        return sort;
    }
}
