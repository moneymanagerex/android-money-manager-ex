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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.money.manager.ex.PasscodeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.core.Passcode;

/**
 */
public class SecuritySettingsFragment
    extends PreferenceFragment {

    private static final int REQUEST_INSERT_PASSCODE = 1;
    private static final int REQUEST_EDIT_PASSCODE = 2;
    private static final int REQUEST_DELETE_PASSCODE = 3;
    private static final int REQUEST_REINSERT_PASSCODE = 10;

    private String passcode = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.security_settings);
        PreferenceManager.getDefaultSharedPreferences(getActivity());

        // active passcode
        final PreferenceScreen psActivePasscode = (PreferenceScreen) findPreference(getString(PreferenceConstants.PREF_ACTIVE_PASSCODE));
        psActivePasscode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                passcode = null;
                startActivityPasscode(getString(R.string.enter_your_passcode), REQUEST_INSERT_PASSCODE);
                return false;
            }
        });

        final PreferenceScreen psEditPasscode = (PreferenceScreen) findPreference(getString(PreferenceConstants.PREF_EDIT_PASSCODE));
        psEditPasscode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                passcode = null;
                startActivityPasscode(getString(R.string.enter_your_previous_passcode), REQUEST_EDIT_PASSCODE);
                return false;
            }
        });

        final PreferenceScreen psDisablePasscode = (PreferenceScreen) findPreference(getString(PreferenceConstants.PREF_DISABLE_PASSCODE));
        psDisablePasscode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                passcode = null;
                startActivityPasscode(getString(R.string.enter_your_passcode), REQUEST_DELETE_PASSCODE);
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) return;
        if (data == null) return;

        Passcode pass = new Passcode(getActivity());

        switch (requestCode) {
            case REQUEST_INSERT_PASSCODE:
                // check if reinsert
                passcode = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
                startActivityPasscode(getString(R.string.reinsert_your_passcode), REQUEST_REINSERT_PASSCODE);
                break;

            case REQUEST_REINSERT_PASSCODE:
                String sentPasscode = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
                if (sentPasscode == null) {
                    UIHelper.showToast(getActivity(), "passcode not retrieved");
                    return;
                }
                if (passcode != null && passcode.equals(sentPasscode)) {
                    if (!pass.setPasscode(passcode)) {
//                                Toast.makeText(getActivity(), R.string.passcode_not_update, Toast.LENGTH_LONG).show();
                        UIHelper.showToast(getActivity(), R.string.passcode_not_update);
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
                }
                break;

            case REQUEST_EDIT_PASSCODE:
                // check if reinsert
                passcode = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
                String passcodedb = pass.getPasscode();
                if (passcodedb != null && passcode != null) {
                    if (passcodedb.equals(passcode)) {
                        startActivityPasscode(getString(R.string.enter_your_passcode), REQUEST_INSERT_PASSCODE);
                    } else
                        Toast.makeText(getActivity(), R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
                }
                break;

            case REQUEST_DELETE_PASSCODE:
                // check if reinsert
                passcode = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
                String passcodeDelete = pass.getPasscode();
                if (passcodeDelete != null && passcode != null) {
                    if (passcodeDelete.equals(passcode)) {
                        if (!pass.cleanPasscode()) {
                            Toast.makeText(getActivity(), R.string.passcode_not_update, Toast.LENGTH_LONG).show();
                        }
                    } else
                        Toast.makeText(getActivity(), R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // check if has passcode
        Passcode passcode = new Passcode(getActivity());

        if (findPreference(getString(PreferenceConstants.PREF_ACTIVE_PASSCODE)) != null)
            findPreference(getString(PreferenceConstants.PREF_ACTIVE_PASSCODE)).setEnabled(!passcode.hasPasscode());
        if (findPreference(getString(PreferenceConstants.PREF_EDIT_PASSCODE)) != null)
            findPreference(getString(PreferenceConstants.PREF_EDIT_PASSCODE)).setEnabled(passcode.hasPasscode());
        if (findPreference(getString(PreferenceConstants.PREF_DISABLE_PASSCODE)) != null)
            findPreference(getString(PreferenceConstants.PREF_DISABLE_PASSCODE)).setEnabled(passcode.hasPasscode());
    }

    private void startActivityPasscode(CharSequence message, int request) {
        Intent intent = new Intent(getActivity(), PasscodeActivity.class);

        intent.setAction(PasscodeActivity.INTENT_REQUEST_PASSWORD);
        intent.putExtra(PasscodeActivity.INTENT_MESSAGE_TEXT, message);

        startActivityForResult(intent, request);
    }
}
