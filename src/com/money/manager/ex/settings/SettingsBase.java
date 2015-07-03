package com.money.manager.ex.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Alen Siljak on 03/07/2015.
 */
public abstract class SettingsBase {

    public SettingsBase(Context context) {
        this.mContext = context;
    }

    protected final Context mContext;
//    protected AppSettings mSettings;
    private SharedPreferences.Editor mEditor;

    // common

    protected SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
    }

    protected String getSettingsKey(Integer settingKeyConstant) {
        return mContext.getString(settingKeyConstant, "");
    }

    public SharedPreferences.Editor getEditor() {
        if (mEditor == null) {
            mEditor = getSharedPreferences().edit();
        }
        return mEditor;
    }

    // String

    public String get(String key, String defaultValue) {
        return getSharedPreferences().getString(key, defaultValue);
    }

    protected String getStringSetting(Integer settingKey) {
        String key = getSettingsKey(settingKey);
        return getStringSetting(key);
    }

    protected String getStringSetting(String settingKey) {
        return getSharedPreferences().getString(settingKey, "");
    }

    /**
     * Save string value to settings.
     * @param key
     * @param value
     */
    public boolean set(String key, String value) {
        getEditor().putString(key, value);
        boolean result = getEditor().commit();
        return result;
    }

    // Boolean

    public boolean get(String key, boolean defaultValue) {
        return getSharedPreferences().getBoolean(key, defaultValue);
    }

    protected boolean get(Integer settingKey, boolean defaultValue) {
        String key = getSettingsKey(settingKey);
        return getBooleanSetting(key, defaultValue);
    }

    protected boolean getBooleanSetting(String settingKey) {
        return getBooleanSetting(settingKey, false);
    }

    protected boolean getBooleanSetting(String settingKey, boolean defaultValue) {
        // This is the main method that actually fetches the value.
        return getSharedPreferences().getBoolean(settingKey, defaultValue);
    }

    public boolean set(String key, boolean value) {
        getEditor().putBoolean(key, value);
        boolean result = getEditor().commit();

        return result;
    }

    // Integer

    public boolean set(String key, int value) {
        getEditor().putInt(key, value);
        boolean result = getEditor().commit();
        return result;
    }

}
