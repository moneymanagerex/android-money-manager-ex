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

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.money.manager.ex.R;
import com.money.manager.ex.common.AmountInputDialog;
import com.money.manager.ex.common.events.AmountEnteredEvent;

import de.greenrobot.event.EventBus;
import info.javaperformance.money.Money;

/**
 * Look & feel settings.
 */
public class InvestmentSettingsFragment
    extends PreferenceFragmentCompat {

    private static final String KEY_THRESHOLD = "AssetAllocationThreshold";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.getDefaultSharedPreferences(getActivity());

        initializeAssetAllocationThreshold();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // use either setPreferenceScreen(PreferenceScreen) or addPreferencesFromResource(int).

        addPreferencesFromResource(R.xml.settings_behaviour);
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    // Events

    public void onEvent(AmountEnteredEvent event) {
        if (event.requestId.equals(KEY_THRESHOLD)) {
            BehaviourSettings settings = new BehaviourSettings(getActivity());
            settings.setAssetAllocationDifferenceThreshold(event.amount);
        }
    }

    // Private

    private void initializeAssetAllocationThreshold() {
        Preference threshold = findPreference(getString(R.string.pref_asset_allocation_threshold));
        if (threshold == null) return;

        final BehaviourSettings settings = new BehaviourSettings(getActivity());

        Preference.OnPreferenceClickListener listener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Money value = settings.getAssetAllocationDifferenceThreshold();

                // show number entry form
                AmountInputDialog
                    .getInstance(KEY_THRESHOLD, value)
                    .show(getFragmentManager(), KEY_THRESHOLD);
                return true;
            }
        };
        threshold.setOnPreferenceClickListener(listener);
    }
}
