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

import android.databinding.tool.util.StringUtils;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.money.manager.ex.R;
import com.money.manager.ex.common.AmountInputDialog;
import com.money.manager.ex.common.events.AmountEnteredEvent;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.domainmodel.Info;
import com.money.manager.ex.servicelayer.InfoService;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Look & feel settings.
 */
public class BehaviourFragment
    extends PreferenceFragment {

    private static final String KEY_THRESHOLD = "AssetAllocationThreshold";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_behaviour);

        PreferenceManager.getDefaultSharedPreferences(getActivity());

        // checkbox on open and favorite account
//        final CheckBoxPreference chkFilter = (CheckBoxPreference) findPreference(getString(
//                R.string.pref_behaviour_focus_filter));

//        Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                MainActivity.setRestartActivity(true);
//                return true;
//            }
//        };
        // Set the main activity to restart on change of any of the following settings.
//        chkFilter.setOnPreferenceChangeListener(listener);

//        initializeAssetAllocationThreshold();
    }

    // Events

    public void onEvent(AmountEnteredEvent event) {
        if (event.requestId.equals(KEY_THRESHOLD)) {
//            BehaviourSettings settings = new BehaviourSettings(getActivity());
//            settings.setAssetAllocationDifferenceThreshold(event.amount);
            InfoService service = new InfoService(getActivity());
            service.setInfoValue(InfoKeys.ASSET_ALLOCATION_DIFF_THRESHOLD, event.amount.toString());
        }
    }

    // Private

//    private void initializeAssetAllocationThreshold() {
//        Preference threshold = findPreference(getString(R.string.pref_behaviour_asset_allocation_threshold));
//        if (threshold == null) return;
//
////        final BehaviourSettings settings = new BehaviourSettings(getActivity());
//
//        Preference.OnPreferenceClickListener listener = new Preference.OnPreferenceClickListener() {
//            @Override
//            public boolean onPreferenceClick(Preference preference) {
//                InfoService service = new InfoService(getActivity());
//                String setting = service.getInfoValue(InfoKeys.ASSET_ALLOCATION_DIFF_THRESHOLD);
//                // settings.getAssetAllocationDifferenceThreshold()
//                if(org.apache.commons.lang3.StringUtils.isEmpty(setting)) {
//                    setting = "0";
//                }
//                Money value = MoneyFactory.fromString(setting);
//
//                // show number entry form
//                AmountInputDialog.getInstance(KEY_THRESHOLD, value);
//                return true;
//            }
//        };
//        threshold.setOnPreferenceClickListener(listener);
//    }
}
