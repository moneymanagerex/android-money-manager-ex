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
package com.money.manager.ex.investment;

import android.os.Bundle;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;

import androidx.fragment.app.FragmentManager;

public class PortfolioActivity
    extends MmxBaseFragmentActivity {

    private static final String FRAGMENT_TAG = PortfolioFragment.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_portfolio);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setDisplayHomeAsUpEnabled(true);

        // todo: pass the correct account id.
//        Intent intent = getIntent();
        // todo: action
//        if (intent != null && !(TextUtils.isEmpty(intent.getAction()))) {
//            listFragment.mAction = intent.getAction();
//        }
        FragmentManager fm = getSupportFragmentManager();
        // attach fragment to activity
        if (fm.findFragmentById(R.id.content) == null) {
            PortfolioFragment listFragment = PortfolioFragment.newInstance(Constants.NOT_SET);
            fm.beginTransaction().add(R.id.content, listFragment, FRAGMENT_TAG).commit();
        }

//        Answers.getInstance().logCustom(new CustomEvent(AnswersEvents.Portfolio.name()));
    }


}
