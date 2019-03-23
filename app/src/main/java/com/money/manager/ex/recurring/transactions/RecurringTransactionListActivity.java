/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.recurring.transactions;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.money.manager.ex.PasscodeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Passcode;
import com.money.manager.ex.common.MmxBaseFragmentActivity;

import androidx.fragment.app.FragmentManager;

/**
 * Not used.
 */
public class RecurringTransactionListActivity
    extends MmxBaseFragmentActivity {

    public static final String INTENT_EXTRA_LAUNCH_NOTIFICATION = "RecurringTransactionListActivity:LaunchNotification";
    public static final int INTENT_REQUEST_PASSCODE = 2;
    private static final String FRAGMENTTAG = RecurringTransactionListActivity.class.getSimpleName() + "_Fragment";

    private RecurringTransactionListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_toolbar_activity);

        // check if launch from notification
        if (getIntent() != null && getIntent().getBooleanExtra(INTENT_EXTRA_LAUNCH_NOTIFICATION, false)) {
            Passcode passcode = new Passcode(getApplicationContext());
            if (passcode.hasPasscode()) {
                Intent intent = new Intent(this, PasscodeActivity.class);
                // set action and data
                intent.setAction(PasscodeActivity.INTENT_REQUEST_PASSWORD);
                intent.putExtra(PasscodeActivity.INTENT_MESSAGE_TEXT, getString(R.string.enter_your_passcode));
                // start activity
                startActivityForResult(intent, INTENT_REQUEST_PASSCODE);
            }
        }
        // set actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set fragment and fragment manager
        FragmentManager fm = getSupportFragmentManager();
        listFragment = new RecurringTransactionListFragment();
        // attach fragment on activity
        if (fm.findFragmentById(R.id.content) == null) {
            fm.beginTransaction().add(R.id.content, listFragment, FRAGMENTTAG).commit();
        }

//        Answers.getInstance().logCustom(new CustomEvent(AnswersEvents.RecurringTransactionList.name()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check request code
        switch (requestCode) {
            case INTENT_REQUEST_PASSCODE:
                boolean isAuthenticated = false;
                if (resultCode == RESULT_OK && data != null) {

                    String passIntent = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
                    if (!passIntent.equals("FingerprintAuthenticationSuccess")) {
                        Passcode passcode = new Passcode(getApplicationContext());
                        String passDb = passcode.getPasscode();

                        if (passIntent != null && passDb != null) {
                            isAuthenticated = passIntent.equals(passDb);
                            if (!isAuthenticated) {
                                Toast.makeText(getApplicationContext(), R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    else {
                        isAuthenticated = true;
                    }

                }
                // close if not authenticated
                if (!isAuthenticated) {
                    this.finish();
                }
        }
    }
}
