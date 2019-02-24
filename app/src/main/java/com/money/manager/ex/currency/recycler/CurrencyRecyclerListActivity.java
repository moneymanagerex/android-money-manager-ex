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

package com.money.manager.ex.currency.recycler;

import android.os.Bundle;

import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;

import androidx.fragment.app.FragmentManager;

public class CurrencyRecyclerListActivity
    extends MmxBaseFragmentActivity {

    private static final String FRAGMENTTAG = CurrencyRecyclerListFragment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_toolbar_activity);

        // change home icon to 'back'.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CurrencyRecyclerListFragment fragment = CurrencyRecyclerListFragment.createInstance();
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(R.id.content) == null) {
            fm.beginTransaction().add(R.id.content, fragment, FRAGMENTTAG).commit();
        }
    }
}
