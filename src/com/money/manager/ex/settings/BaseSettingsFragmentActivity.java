package com.money.manager.ex.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.money.manager.ex.R;
import com.money.manager.ex.fragment.BaseFragmentActivity;

/**
 * Created by Alessandro Lazzari on 15/03/2015.
 */
public class BaseSettingsFragmentActivity extends BaseFragmentActivity {
    private static String LOGCAT = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstance) {
        setContentView(R.layout.settings_activity);
        super.onCreate(savedInstance);
        setDisplayHomeAsUpEnabled(true);
    }

    protected void setSettingFragment(PreferenceFragment fragment) {
        getFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
    }
}
