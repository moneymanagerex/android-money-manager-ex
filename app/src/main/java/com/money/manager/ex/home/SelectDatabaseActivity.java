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

package com.money.manager.ex.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.docstorage.DocFileMetadata;
import com.money.manager.ex.core.docstorage.FileStorageHelper;
import com.money.manager.ex.core.IntentFactory;
import com.money.manager.ex.core.RequestCodes;
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

        switch (requestCode) {
//            case RequestCodes.SELECT_FILE:
//                //if (resultCode != RESULT_OK) return;
//                String selectedPath = UIHelper.getSelectedFile(data);
//                if(TextUtils.isEmpty(selectedPath)) {
//                    new UIHelper(this).showToast(R.string.invalid_database);
//                    return;
//                }
//
//                onDatabaseSelected(selectedPath);
//                break;

            case RequestCodes.SELECT_DOCUMENT:
                // file selected at a Storage Access Framework.
                FileStorageHelper storageHelper = new FileStorageHelper(this);
                storageHelper.selectDatabase(data);
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
        // show the create database screen
        Intent intent = new Intent(this, CreateDatabaseActivity.class);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        startActivity(intent);
    }

    @OnClick(R.id.openDatabaseButton)
    void onOpenDatabaseClick() {
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
