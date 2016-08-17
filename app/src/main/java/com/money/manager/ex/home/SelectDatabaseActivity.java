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

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.utils.MmexDatabaseUtils;
import com.money.manager.ex.utils.MyFileUtils;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class SelectDatabaseActivity
    extends BaseFragmentActivity {

    public static final int REQUEST_PICKFILE = 1;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_database);

        ButterKnife.bind(this);

        // Request external storage permissions for Android 6+.
        MyFileUtils fileUtils = new MyFileUtils(this);
        fileUtils.requestExternalStoragePermissions(this);

        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_PICKFILE:
                if (resultCode != RESULT_OK) return;
                if (data == null || !data.hasExtra(FilePickerActivity.RESULT_FILE_PATH)) return;

                // data.getData().getPath()
                String selectedPath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
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
        Timber.d("returning from permissions request %s", permissions);
    }

    @OnClick(R.id.createDatabaseButton)
    void onCreateDatabaseClick() {
        // todo: show the create database screen
    }

    @OnClick(R.id.openDatabaseButton)
    void onOpenDatabaseClick() {
        MmexDatabaseUtils dbUtils = new MmexDatabaseUtils(this);
        // todo inspect what happens here
        String dbDirectory = dbUtils.getDefaultDatabaseDirectory();

        // show the file picker
        try {
            UIHelper helper = new UIHelper(this);
            helper.pickFileInternal(dbDirectory, REQUEST_PICKFILE);
        } catch (Exception e) {
            Timber.e(e, "opening file picker");
        }
    }

    @OnClick(R.id.setupSyncButton)
    void onSetupSyncClick() {
        // todo: show the create database screen
    }

    private void onDatabaseSelected(String dbPath) {
        // check if the file is a valid database
        MmexDatabaseUtils dbUtils = new MmexDatabaseUtils(this);
        if (!dbUtils.isValidDbFile(dbPath)) {
            UIHelper.showToast(this, R.string.invalid_database);
            return;
        }

        // store db setting
        new AppSettings(this).getDatabaseSettings().setDatabasePath(dbPath);

        // open the main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
