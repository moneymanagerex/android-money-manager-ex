package com.money.manager.ex.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.preferences.PreferencesConstant;
import com.money.manager.ex.view.RobotoView;

import org.apache.commons.lang3.math.NumberUtils;

public class LookFeelSettingsActivity extends BaseFragmentActivity {
    private static String LOGCAT = LookFeelSettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new LookFeelFragment()).commit();
    }

    public static class LookFeelFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.look_and_feel_settings);
            PreferenceManager.getDefaultSharedPreferences(getActivity());

            final MoneyManagerApplication application = (MoneyManagerApplication) getActivity().getApplication();

            // checkbox on open and favorite account
            final CheckBoxPreference chkAccountOpen = (CheckBoxPreference) findPreference(PreferencesConstant.PREF_ACCOUNT_OPEN_VISIBLE);
            final CheckBoxPreference chkAccountFav = (CheckBoxPreference) findPreference(PreferencesConstant.PREF_ACCOUNT_FAV_VISIBLE);

            Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    MainActivity.setRestartActivity(true);
                    return true;
                }
            };
            // set listener on the checkbox
            chkAccountOpen.setOnPreferenceChangeListener(listener);
            chkAccountFav.setOnPreferenceChangeListener(listener);

            // show transaction
            final ListPreference lstShow = (ListPreference) findPreference(PreferencesConstant.PREF_SHOW_TRANSACTION);
            if (lstShow != null) {
                lstShow.setSummary(application.getShowTransaction());
                lstShow.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        lstShow.setSummary((CharSequence) newValue);
                        return true;
                    }
                });
            }

            // font type
            final ListPreference lstFont = (ListPreference) findPreference(PreferencesConstant.PREF_APPLICATION_FONT);
            if (lstFont != null) {
                lstFont.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (newValue instanceof String && NumberUtils.isNumber(newValue.toString())) {
                            RobotoView.setUserFont(Integer.parseInt(newValue.toString()));
                            return true;
                        }
                        return false;
                    }
                });
            }

            //font size
            final ListPreference lstFontSize = (ListPreference) findPreference(PreferencesConstant.PREF_APPLICATION_FONT_SIZE);
            if (lstFontSize != null) {
                lstFontSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        RobotoView.setUserFontSize(getActivity().getApplicationContext(), newValue.toString());
                        return true;
                    }
                });
            }

            //theme
            final ListPreference lstTheme = (ListPreference) findPreference(PreferencesConstant.PREF_THEME);
            if (lstTheme != null) {
                lstTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        MainActivity.setRestartActivity(true);
                        return true;
                    }
                });
            }
        }
    }
}
