/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.scheduled;

import static android.app.PendingIntent.getActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.money.manager.ex.passcode.PasscodeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.Passcode;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.servicelayer.RecurringTransactionService;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.money.manager.ex.transactions.EditTransactionActivityConstants;

/**
 * Not used.
 */
public class ScheduledTransactionListActivity
    extends MmxBaseFragmentActivity {

    public static final String INTENT_EXTRA_LAUNCH_NOTIFICATION = "ScheduledTransactionListActivity:LaunchNotification";
    public static final int INTENT_REQUEST_PASSCODE = 2;
    private static final String FRAGMENTTAG = ScheduledTransactionListActivity.class.getSimpleName() + "_Fragment";

    private ScheduledTransactionListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_toolbar_activity);

        Long trxid = 0L;
        String action = "";

        // check if launch from notification
        if (getIntent() != null && getIntent().getBooleanExtra(INTENT_EXTRA_LAUNCH_NOTIFICATION, false)) {
            action = getIntent().getStringExtra("ACTION");
            trxid = getIntent().getLongExtra("ID", 0);
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
        listFragment = new ScheduledTransactionListFragment();
        // attach fragment on activity
        if (fm.findFragmentById(R.id.content) == null) {
            fm.beginTransaction().add(R.id.content, listFragment, FRAGMENTTAG).commit();
        }

        if ( action.equals("SKIP") || action.equals("ENTER")) {
            // Skip or enter Occurrence
            NotificationManager notificationManager = (NotificationManager) getApplication().getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(trxid.intValue());

            if (action.equals("SKIP")) {
                RecurringTransactionService recurringTransaction = new RecurringTransactionService(trxid, this);
                recurringTransaction.moveNextOccurrence();
            }

            if (action.equals("ENTER")) {
                // ToDo: autopost automtically, try to find a way to open directly edit
                // showCreateTransactionActivity(trxid);

//                boolean isAutoExecution = (new AppSettings(this)).getBehaviourSettings().getNotificationRecurringTransaction();
//                if (!isAutoExecution) {  // TODO: set with dialog
                    // showCreateTransactionActivity(trxid);
//                } else {
                    RecurringTransactionService service = new RecurringTransactionService(trxid, this);
                    RecurringTransaction tx = service.load(trxid);
                    if ( tx.isRecurringModeAuto()) {
                        AccountTransactionRepository accountTransactionRepository = new AccountTransactionRepository(getApplicationContext());
                        AccountTransaction accountTrx = service.getAccountTransactionFromRecurring();
                        accountTransactionRepository.insert(accountTrx);
                        service.moveNextOccurrence();
                    } else {
                        // showCreateTransactionActivity(trxid);
                        Intent intent = new Intent(this, CheckingTransactionEditActivity.class);
                        intent.setAction(Intent.ACTION_INSERT);
                        intent.putExtra(EditTransactionActivityConstants.KEY_BDID_ID, trxid);
                        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_SOURCE, "ScheduledTransactionListFragment.java");
                        // start for insert new transaction
                        startActivity(intent, savedInstanceState);
//                        startActivityForResult(intent, 1002); // TODO REQUEST_ADD_TRANSACTION
                    }

//                }

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check request code
        if (requestCode == INTENT_REQUEST_PASSCODE) {
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
                } else {
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
