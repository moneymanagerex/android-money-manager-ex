/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.IntentFactory;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.core.docstorage.FileStorageHelper;
import com.money.manager.ex.database.PasswordActivity;
import com.money.manager.ex.utils.MmxFileUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.Lazy;
import timber.log.Timber;

/**
 * Activity for selecting a database in the initial setup of the app.
 */
public class SelectDatabaseActivity
    extends MmxBaseFragmentActivity {

    @Inject Lazy<RecentDatabasesProvider> mDatabasesLazy;

    @BindView(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_database);

        MmexApplication.getApp().iocComponent.inject(this);

        ButterKnife.bind(this);

        // Request external storage permissions for Android 6+.
        MmxFileUtils fileUtils = new MmxFileUtils(this);
        fileUtils.requestExternalStoragePermissions(this);

        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Timber.w("The activity result is not OK");
            return;
        }
        if (requestCode == RequestCodes.SELECT_DOCUMENT) {// file selected at a Storage Access Framework.
            FileStorageHelper storageHelper = new FileStorageHelper(this);
            storageHelper.selectDatabase(data);
            onDatabaseSelected();
        } else if (requestCode == RequestCodes.CREATE_DOCUMENT) {
            FileStorageHelper storageHelper = new FileStorageHelper(this);
            storageHelper.createDatabase(data);
            onDatabaseSelected();
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
        startActivity(new Intent(this, PasswordActivity.class));
        FileStorageHelper helper = new FileStorageHelper(this);
        helper.showCreateFilePicker();
    }

    @OnClick(R.id.openDatabaseButton)
    void onOpenDatabaseClick() {
        startActivity(new Intent(this, PasswordActivity.class));
        FileStorageHelper helper = new FileStorageHelper(this);
        helper.showStorageFilePicker();
    }

//    @OnClick(R.id.setupSyncButton)
//    void onSetupSyncClick() {
//        Intent intent = new Intent(this, SyncPreferencesActivity.class);
//        startActivity(intent);
//    }

    private void onDatabaseSelected() {
        // open the main activity
        Intent intent = IntentFactory.getMainActivityNew(this);
        startActivity(intent);
    }
}
