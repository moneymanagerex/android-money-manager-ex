package com.money.manager.ex.settings;

import android.app.Activity;
import android.preference.PreferenceManager;

/**
 * Settings in the General category.
 */
public class GeneralSettings {
    public GeneralSettings(Activity activity) {
        this.Activity = activity;
    }

    public final Activity Activity;

    public String getApplicationLocale() {
        String result = PreferenceManager.getDefaultSharedPreferences(this.Activity.getApplicationContext())
                .getString(this.Activity.getString(PreferencesConstant.PREF_LOCALE), "");
        return result;
    }

}
