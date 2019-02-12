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

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.widget.Toast;

import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.utils.MmxDate;

/**
 * Look & feel preferences.
 */
public class BehaviourSettingsFragment
    extends PreferenceFragmentCompat {

    private static final String KEY_NOTIFICATION_TIME = "NotificationTime";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // use either setPreferenceScreen(PreferenceScreen) or addPreferencesFromResource(int).

        addPreferencesFromResource(R.xml.preferences_behaviour);

        initializeNotificationTime();
        initializeSmsAutomation();
    }

    @Override
    public void onStart() {
        super.onStart();

//        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

//        EventBus.getDefault().unregister(this);
    }

    // Private

    private void initializeNotificationTime() {
        Preference preference = findPreference(getString(PreferenceConstants.PREF_REPEATING_TRANSACTION_CHECK));
        if (preference == null) return;

        Preference.OnPreferenceClickListener listener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showTimePicker();
                return true;
            }
        };
        preference.setOnPreferenceClickListener(listener);
    }

    private void showTimePicker() {
        final BehaviourSettings settings = new BehaviourSettings(getActivity());

        RadialTimePickerDialogFragment.OnTimeSetListener timeSetListener = new RadialTimePickerDialogFragment.OnTimeSetListener() {
            @Override
            public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {
                String value = String.format("%02d:%02d", hourOfDay, minute);
                settings.setNotificationTime(value);
            }
        };

        // get time to display (current setting)
        String timeString = settings.getNotificationTime();
//        DateTimeFormatter formatter = DateTimeFormat.forPattern(Constants.TIME_FORMAT);
//        DateTime currentValue = formatter.parseDateTime(timeString);
        MmxDate currentValue = new MmxDate(timeString, Constants.TIME_FORMAT);

        int hour = currentValue != null ? currentValue.getHourOfDay() : 8;
        int minute = currentValue != null ? currentValue.getMinuteOfHour() : 0;

        RadialTimePickerDialogFragment timePicker = new RadialTimePickerDialogFragment()
            .setOnTimeSetListener(timeSetListener)
            .setForced24hFormat()
            .setStartTime(hour, minute)
            .setThemeDark();
        timePicker.show(getChildFragmentManager(), KEY_NOTIFICATION_TIME);
    }

    //Author:- velmuruganc - Added for Issue : #1144 - Add automatic bank transaction updates
    private void initializeSmsAutomation()
    {
        final BehaviourSettings settings = new BehaviourSettings(getActivity());

        Preference preference = findPreference(getString(PreferenceConstants.PREF_SMS_AUTOMATIC_TRANSACTIONS));

        if (preference == null) return;

        Preference.OnPreferenceClickListener listener = new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {

                if (Build.VERSION.SDK_INT >= 23)
                {
                    //Check the permission exists, if not request the permission from the user
                    int result = ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.RECEIVE_SMS);

                    if (settings.getBankSmsTrans()==true)
                    {
                        if (result == PackageManager.PERMISSION_GRANTED)
                        {
                            Toast.makeText(getActivity(), R.string.granted_receive_sms_access, Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            // request for the permission
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECEIVE_SMS}, 1);
                        }
                    }
                    else
                    {
                        // remove the permissions
                        Toast.makeText(getActivity(), R.string.revoke_receive_sms_access, Toast.LENGTH_LONG).show();
                        settings.setBankSmsTrans(false);
                        settings.setSmsTransStatusNotification(false);

                    }
                }

                return true;
            }
        };
        preference.setOnPreferenceClickListener(listener);
    }

}
