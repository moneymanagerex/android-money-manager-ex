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

import androidx.preference.PreferenceManager;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.servicelayer.InfoService;

/**
 * Settings in the General category.
 */
public class GeneralSettings
    extends SettingsBase {

    public GeneralSettings(Context context) {
        super(context);

    }

    @Override
    protected SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public String getApplicationLanguage() {
        return get(R.string.pref_locale, "");
    }

    /**
     * Set application locale.
     * @param value Language ISO code (i.e. bs, en)
     */
    public void setApplicationLanguage(String value) {
        set(R.string.pref_locale, value);
    }

    /**
     * Fetches the default account id. The default account is set per database.
     * @return Default account id.
     */
    public Long getDefaultAccountId() {
//        String value = get(R.string.pref_default_account, "");
        InfoService service = new InfoService(getContext());
        String value = service.getInfoValue(InfoKeys.DEFAULT_ACCOUNT_ID);

        return NumericHelper.toLong(value);
    }

    public void setDefaultAccountId(Long accountId) {
        String value = "";
        if (accountId != null) {
            value = accountId.toString();
        }
//        set(R.string.pref_default_account, value);

        InfoService service = new InfoService(getContext());
        service.setInfoValue(InfoKeys.DEFAULT_ACCOUNT_ID, value);
    }

    public String getTheme() {
        String lightTheme = Constants.THEME_LIGHT;
        return get(R.string.pref_theme, lightTheme);
    }

    public Long getBaseCurrencyId() {
        InfoService service = new InfoService(getContext());
        String value = service.getInfoValue(InfoKeys.BASECURRENCYID);

        return NumericHelper.toLong(value);
    }

    public boolean getSendUsage() {
        return get(R.string.pref_anonymous_usage, false);
    }
}
