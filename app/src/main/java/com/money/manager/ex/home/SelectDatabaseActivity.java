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
import com.money.manager.ex.utils.MyFileUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class SelectDatabaseActivity
    extends BaseFragmentActivity {

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
        // handle permissions
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
        // todo: show the file picker
    }

    @OnClick(R.id.setupSyncButton)
    void onSetupSyncClick() {
        // todo: show the create database screen
    }
}
