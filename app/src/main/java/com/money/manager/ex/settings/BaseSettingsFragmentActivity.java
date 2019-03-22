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

package com.money.manager.ex.settings;

import android.os.Bundle;

import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.log.ErrorRaisedEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Base activity for the settings activities.
 */
public class BaseSettingsFragmentActivity
    extends MmxBaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.settings_activity);
        setDisplayHomeAsUpEnabled(true);

        //
    }

    @Subscribe
    public void onEvent(ErrorRaisedEvent event) {
        new UIHelper(this).showToast(event.message);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically e clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        if (id == android.R.id.home) {
//            Log.d("test", "action bar clicked");
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    protected void setSettingFragment(PreferenceFragmentCompat fragment) {
        // use the class name as the fragment tag.
        String tag = fragment.getClass().getSimpleName();

        FragmentManager manager = getSupportFragmentManager();

        FragmentTransaction tx = manager.beginTransaction();
        tx.replace(R.id.content, fragment, tag);

        // Add to backstack only if this is not the first fragment, and the fragment is not already added.
        List<Fragment> fragments = manager.getFragments();
        boolean isFirstFragment = fragments == null || fragments.size() == 0;

        Fragment existing = manager.findFragmentByTag(tag);
        boolean isAdded = existing != null;

        if (!isFirstFragment && !isAdded) {
            tx.addToBackStack(null);
        }

        tx.commit();
    }
}
