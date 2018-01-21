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
package com.money.manager.ex.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * This class is used to interact with application preferences/preferences.
 * Expand with additional methods as needed.
 */
public class AppSettings
    extends SettingsBase {

    @Inject
    public AppSettings(Context context) {
        super(context);

        // DI
        MmexApplication.getApp().iocComponent.inject(this);
    }

    @Inject Lazy<SharedPreferences> sharedPreferences;

    // setting groups

    private GeneralSettings mGeneral;
    private LookAndFeelSettings mLookAndFeel;
    private BehaviourSettings mBehaviour;
    private InvestmentSettings mInvestment;
    private BudgetSettings mBudget;
    private DatabaseSettings mDatabase;

    @Override
    protected SharedPreferences getPreferences() {
        return sharedPreferences.get();
    }

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

    public BudgetSettings getBudgetSettings() {
        if (mBudget == null) {
            mBudget = new BudgetSettings(getContext());
        }
        return mBudget;
    }

    // Individual preferences.

    public int getPayeeSort() {
        int sort = get(R.string.pref_sort_payee, 0);
        return sort;
    }
}
