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

package com.money.manager.ex.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.account.AccountEditActivity;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.home.createdb.CreateDbStepper;
import com.money.manager.ex.home.events.RequestOpenDatabaseEvent;
import com.money.manager.ex.settings.GeneralSettingsActivity;
import com.money.manager.ex.settings.SyncPreferencesActivity;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateDatabaseActivity
    extends BaseFragmentActivity {

    @BindView(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_database);

        ButterKnife.bind(this);
        getToolbar().setSubtitle(R.string.create_db);

//        createWelcomeView();

        // todo language

        // todo Create database; use the existing functionality from the database preferences.
        // Set the database as current in the preferences.

        // todo Create account. Allow multiple times.
        // todo Default account. When the first account is created, use that. Allow changing if multiple accounts are created.
        // todo Default currency. Check if set on db creation. Set after the first account and allow changing.
    }

    @OnClick(R.id.runButton)
    void onRunClick() {
        // todo enable the button once the preferences have been done

        // open the main activity
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);

        Intent stepper = new Intent(this, CreateDbStepper.class);
        startActivityForResult(stepper, 1);
    }

    private void createWelcomeView() {
//        linearWelcome = (ViewGroup) findViewById(R.id.linearLayoutWelcome);

        // basic settings
        Button buttonSettings = (Button) this.findViewById(R.id.buttonSettings);
        if (buttonSettings != null) {
            buttonSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(CreateDatabaseActivity.this, GeneralSettingsActivity.class));
                }
            });
        }

        // Show current database
        TextView currentDatabaseTextView = (TextView) this.findViewById(R.id.currentDatabaseTextView);
        if (currentDatabaseTextView != null) {
            String path = MoneyManagerApplication.getDatabasePath(this);
            currentDatabaseTextView.setText(path);
        }

        // add account button
        Button btnAddAccount = (Button) this.findViewById(R.id.buttonAddAccount);
        if (btnAddAccount != null) {
            btnAddAccount.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CreateDatabaseActivity.this, AccountEditActivity.class);
                    intent.setAction(Intent.ACTION_INSERT);
                    startActivity(intent);
                }
            });
        }

        Button btnOpenDatabase = (Button) this.findViewById(R.id.buttonOpenDatabase);
        if (btnOpenDatabase != null) {
            btnOpenDatabase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventBus.getDefault().post(new RequestOpenDatabaseEvent());
                }
            });
        }

        // Setup Synchronization
        Button btnSetupSync = (Button) this.findViewById(R.id.buttonSetupSync);
        if (btnSetupSync != null) {
            btnSetupSync.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CreateDatabaseActivity.this, SyncPreferencesActivity.class);
                    //intent.putExtra(Constants.INTENT_REQUEST_PREFERENCES_SCREEN, PreferenceConstants.PREF_DROPBOX_HOWITWORKS);
                    startActivity(intent);
                }
            });
        }

        // Database migration v1.4 -> v2.0 location.
        //setUpMigrationButton(view);
    }

}
