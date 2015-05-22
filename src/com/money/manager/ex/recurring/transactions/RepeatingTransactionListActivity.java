/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.recurring.transactions;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.PasscodeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.adapter.AllDataAdapter.TypeCursor;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.Passcode;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.database.TableBudgetSplitTransactions;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.BaseListFragment;
import com.money.manager.ex.utils.DateUtils;

import java.util.Date;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 */
public class RepeatingTransactionListActivity extends BaseFragmentActivity {
    public static final String INTENT_EXTRA_LAUNCH_NOTIFICATION = "RepeatingTransactionListActivity:LaunchNotification";
    public static final int INTENT_REQUEST_PASSCODE = 2;
    public static final String INTENT_RESULT_ACCOUNTID = "AccountListActivity:ACCOUNTID";
    public static final String INTENT_RESULT_ACCOUNTNAME = "AccountListActivity:ACCOUNTNAME";
    @SuppressWarnings("unused")
    private static final String LOGCAT = RepeatingTransactionListActivity.class.getSimpleName();
    private static final String FRAGMENTTAG = RepeatingTransactionListActivity.class.getSimpleName() + "_Fragment";
    private RepeatingTransactionListFragment listFragment;

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
        listFragment = new RepeatingTransactionListFragment();
        // attach fragment on activity
        if (fm.findFragmentById(R.id.content) == null) {
            fm.beginTransaction().add(R.id.content, listFragment, FRAGMENTTAG).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check request code
        switch (requestCode) {
            case INTENT_REQUEST_PASSCODE:
                boolean isAuthenticated = false;
                if (resultCode == RESULT_OK && data != null) {
                    Passcode passcode = new Passcode(getApplicationContext());
                    String passIntent = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
                    String passDb = passcode.getPasscode();
                    if (passIntent != null && passDb != null) {
                        isAuthenticated = passIntent.equals(passDb);
                        if (!isAuthenticated) {
                            Toast.makeText(getApplicationContext(), R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                // close if not authenticated
                if (!isAuthenticated) {
                    this.finish();
                }
        }
    }
}
