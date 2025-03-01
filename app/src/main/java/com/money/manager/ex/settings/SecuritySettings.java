package com.money.manager.ex.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class SecuritySettings extends SettingsBase {
    public SecuritySettings(Context context) {
        super(context);

    }

    @Override
    protected SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public boolean getFingerprintAuthentication() {
        return get(PreferenceConstants.PREF_FINGERPRINT, true);
    }

    public void setFingerprintAuthentication(boolean status) {
        set(PreferenceConstants.PREF_FINGERPRINT, status);
    }
}
