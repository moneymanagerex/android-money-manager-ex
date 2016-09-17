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
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.core.IntentFactory;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.SyncPreferencesActivity;
import com.money.manager.ex.utils.MmxDatabaseUtils;
import com.money.manager.ex.utils.MmxFileUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.Lazy;
import timber.log.Timber;

public class SelectDatabaseActivity
    extends BaseFragmentActivity {

    public static final int REQUEST_PICKFILE = 1;

    @Inject Lazy<RecentDatabasesProvider> mDatabasesLazy;

    @BindView(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_database);

        MoneyManagerApplication.getApp().iocComponent.inject(this);

        ButterKnife.bind(this);

        // Request external storage permissions for Android 6+.
        MmxFileUtils fileUtils = new MmxFileUtils(this);
        fileUtils.requestExternalStoragePermissions(this);

        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_PICKFILE:
                if (resultCode != RESULT_OK) return;
                String selectedPath = UIHelper.getSelectedFile(data);
                if(TextUtils.isEmpty(selectedPath)) {
                    new UIHelper(this).showToast(R.string.invalid_database);
                    return;
                }

                onDatabaseSelected(selectedPath);
                break;
        }
    }

    // Permissions

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // cancellation
        //if (permissions.length == 0) return;
        Timber.d("returning from permissions request"); // permissions
    }

    @OnClick(R.id.createDatabaseButton)
    void onCreateDatabaseClick() {
        // show the create database screen
        Intent intent = new Intent(this, CreateDatabaseActivity.class);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(intent);
    }

    @OnClick(R.id.openDatabaseButton)
    void onOpenDatabaseClick() {
        MmxDatabaseUtils dbUtils = new MmxDatabaseUtils(this);
        String dbDirectory = dbUtils.getDefaultDatabaseDirectory();

        // show the file picker
        try {
            UIHelper.pickFileDialog(this, dbDirectory, REQUEST_PICKFILE);
        } catch (Exception e) {
            Timber.e(e, "opening file picker");
        }
    }

    @OnClick(R.id.setupSyncButton)
    void onSetupSyncClick() {
        Intent intent = new Intent(this, SyncPreferencesActivity.class);
        startActivity(intent);
    }

    private void onDatabaseSelected(String dbPath) {
        // check if the file is a valid database
//        MmxDatabaseUtils dbUtils = new MmxDatabaseUtils(this);
        if (!MmxDatabaseUtils.isValidDbFile(dbPath)) {
            new UIHelper(this).showToast(R.string.invalid_database);
            return;
        }

        // store db setting
        new AppSettings(this).getDatabaseSettings().setDatabasePath(dbPath);
        // Add the current db to the recent db list.
        DatabaseMetadata currentDb = mDatabasesLazy.get().getCurrent();
        mDatabasesLazy.get().add(currentDb);

        // open the main activity
        Intent intent = IntentFactory.getMainActivityNew(this);
        startActivity(intent);
    }
}
