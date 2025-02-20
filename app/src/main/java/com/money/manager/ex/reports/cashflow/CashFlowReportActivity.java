/*
 * Copyright (C) 2012-2025 The Android Money Manager Ex Project Team
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
 *
 * developer wolfsolver
 */
package com.money.manager.ex.reports.cashflow;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;

public class CashFlowReportActivity extends MmxBaseFragmentActivity {

    public long MONTH_LIMIT = 12;
    public boolean mIsDualPanel = false;
    private final CashFlowReportListFragment listFragment = new CashFlowReportListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.report_chart_fragments_activity);
        setContentView(R.layout.chart_line_fragment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // set actionbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // check if is dual panel
        mIsDualPanel = findViewById(R.id.fragmentChart) != null;

        FragmentManager fm = getSupportFragmentManager();
        // attach fragment activity
        if (fm.findFragmentById(R.id.fragmentMain) == null) {
            fm.beginTransaction()
                    .replace(R.id.fragmentMain, listFragment, CashFlowReportListFragment.class.getSimpleName())
                    .commit();
        }
    }
}
