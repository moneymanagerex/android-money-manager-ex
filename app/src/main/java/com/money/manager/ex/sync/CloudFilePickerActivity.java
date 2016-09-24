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

package com.money.manager.ex.sync;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;

public class CloudFilePickerActivity
    extends MmxBaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_toolbar_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FragmentManager fm = getSupportFragmentManager();

        // intent
//        if (getIntent() != null && getIntent().getExtras().containsKey(INTENT_DROBPOXFILE_PATH)) {
//            dropboxFile = getIntent().getExtras().getString(INTENT_DROBPOXFILE_PATH);
//        }

        // attach fragment to activity
        if (fm.findFragmentById(R.id.content) == null) {
            if (fm.findFragmentByTag(CloudFilePickerFragment.class.getSimpleName()) == null) {
                CloudFilePickerFragment fragment = new CloudFilePickerFragment();
                fm.beginTransaction().add(R.id.content, fragment,
                        CloudFilePickerFragment.class.getSimpleName()).commit();
            }
        }
    }
}
