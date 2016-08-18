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
import android.support.v4.text.TextUtilsCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.core.IntentFactory;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.utils.MmxDatabaseUtils;
import com.money.manager.ex.view.RobotoButton;
import com.money.manager.ex.view.RobotoTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.money.manager.ex.Constants.DEFAULT_DB_FILENAME;

public class CreateDatabaseActivity
    extends BaseFragmentActivity {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.runButton) RobotoButton runButton;
    @BindView(R.id.dbPathTextView) RobotoTextView dbPathTextView;
    @BindView(R.id.statusReport) LinearLayout statusReportView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_database);

        ButterKnife.bind(this);
        getToolbar().setSubtitle(R.string.create_db);

        runButton.setEnabled(false);

        createDatabase();

        // todo Create account. Allow multiple times.
        // todo Default account. When the first account is created, use that. Allow changing if multiple accounts are created.
        // todo Default currency. Check if set on db creation. Set after the first account and allow changing.
    }

    private void createDatabase() {
        MmxDatabaseUtils dbUtils = new MmxDatabaseUtils(this);

        String dbPath = dbUtils.createDatabase(DEFAULT_DB_FILENAME);
        if (TextUtils.isEmpty(dbPath)) return;

        // Read the full path from the preferences.
//        String filePath = new AppSettings(this).getDatabaseSettings().getDatabasePath();

        dbUtils.useDatabase(dbPath);

        // show message

        statusReportView.setVisibility(View.VISIBLE);
        UIHelper.showToast(this, R.string.create_db_success);
        dbPathTextView.setText(dbPath);

        // enable run button
        runButton.setEnabled(true);
    }

    @OnClick(R.id.runButton)
    void onRunClick() {
        // Open the main activity.
        Intent intent = IntentFactory.getMainActivityNew(this);
        startActivity(intent);

        finish();
    }

}
