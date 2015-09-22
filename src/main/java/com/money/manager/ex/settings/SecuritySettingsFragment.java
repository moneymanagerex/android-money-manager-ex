package com.money.manager.ex.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.money.manager.ex.PasscodeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.Passcode;

/**
 *
 */
public class SecuritySettingsFragment extends PreferenceFragment {
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

        switch (requestCode) {
            case REQUEST_INSERT_PASSCODE:
            case REQUEST_REINSERT_PASSCODE:
            case REQUEST_EDIT_PASSCODE:
            case REQUEST_DELETE_PASSCODE:
                if (resultCode == Activity.RESULT_OK) {
                    Passcode pass = new Passcode(getActivity());
                    // insert passcode
                    if (requestCode == REQUEST_INSERT_PASSCODE && data != null) {
                        // check if reinsert
                        passcode = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
                        startActivityPasscode(getString(R.string.reinsert_your_passcode), REQUEST_REINSERT_PASSCODE);
                    }

                    // re-insert passcode
                    if (requestCode == REQUEST_REINSERT_PASSCODE && data != null) {
                        String sentPasscode = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
                        if (sentPasscode == null) {
                            ExceptionHandler handler = new ExceptionHandler(getActivity(), this);
                            handler.showMessage("passcode not retrieved");
                            return;
                        }
                        if (passcode.equals(sentPasscode)) {
                            if (!pass.setPasscode(passcode)) {
                                Toast.makeText(getActivity(), R.string.passcode_not_update, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
                        }
                    }

                    // edit passcode
                    if (requestCode == REQUEST_EDIT_PASSCODE && data != null) {
                        // check if reinsert
                        passcode = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
                        String passcodedb = pass.getPasscode();
                        if (passcodedb != null && passcode != null) {
                            if (passcodedb.equals(passcode)) {
                                startActivityPasscode(getString(R.string.enter_your_passcode), REQUEST_INSERT_PASSCODE);
                            } else
                                Toast.makeText(getActivity(), R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
                        }
                    }
                    // delete passcode
                    if (requestCode == REQUEST_DELETE_PASSCODE && data != null) {
                        // check if reinsert
                        passcode = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
                        String passcodedb = pass.getPasscode();
                        if (passcodedb != null && passcode != null) {
                            if (passcodedb.equals(passcode)) {
                                if (!pass.cleanPasscode()) {
                                    Toast.makeText(getActivity(), R.string.passcode_not_update, Toast.LENGTH_LONG).show();
                                }
                            } else
                                Toast.makeText(getActivity(), R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
                        }
                    }
                }
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
        // set action and data
        intent.setAction(PasscodeActivity.INTENT_REQUEST_PASSWORD);
        intent.putExtra(PasscodeActivity.INTENT_MESSAGE_TEXT, message);
        // start activity
        startActivityForResult(intent, request);
    }
}
