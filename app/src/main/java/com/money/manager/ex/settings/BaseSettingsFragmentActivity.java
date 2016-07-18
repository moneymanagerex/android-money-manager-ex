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

package com.money.manager.ex.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;

/**
 */
public class BaseSettingsFragmentActivity
    extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.settings_activity);
        setDisplayHomeAsUpEnabled(true);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        if (id == android.R.id.home) {
//            Log.d("test", "action bar clicked");
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    protected void setSettingFragment(PreferenceFragment fragment) {
        getFragmentManager().beginTransaction()
            .replace(R.id.content, fragment)
            .commit();
    }

    protected void setSettingFragment(android.support.v14.preference.PreferenceFragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }

    protected void setSettingFragment(PreferenceFragmentCompat fragment) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.content, fragment)
            .commit();
    }
}
