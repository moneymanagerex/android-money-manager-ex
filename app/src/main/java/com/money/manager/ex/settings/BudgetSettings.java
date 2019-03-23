/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.money.manager.ex.R;
import com.money.manager.ex.core.DefinedDateRange;
import com.money.manager.ex.core.DefinedDateRangeName;
import com.money.manager.ex.core.DefinedDateRanges;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.servicelayer.InfoService;

import timber.log.Timber;

/**
 * Budget preferences
 */
public class BudgetSettings
    extends SettingsBase {

    public BudgetSettings(Context context) {
        super(context);
    }

    @Override
    protected SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public Boolean getShowSimpleView() {
        return get(R.string.pref_budget_show_simple_view, false);
    }

    public void setShowSimpleView(boolean value) {
        set(R.string.pref_budget_show_simple_view, value);
    }

}
