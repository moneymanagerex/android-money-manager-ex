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

package com.money.manager.ex.tutorial;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Adapter that returns the tutorial pages.
 */
public class TutorialPagerAdapter
    extends FragmentStatePagerAdapter {

    public TutorialPagerAdapter(FragmentManager fm) {
        super(fm);

    }

    @Override
    public Fragment getItem(int i) {
        Fragment pageFragment;

        switch(i){
            case 0:
                pageFragment = new TutorialAccountsFragment();
                break;
            case 1:
                pageFragment = TutorialTransactionsFragment.newInstance();
                break;
            case 2:
                pageFragment = TutorialGlobalFragment.newInstance();
                break;
            case 3:
                pageFragment = TutorialFinancialOverviewFragment.newInstance();
                break;
            case 4:
                pageFragment = TutorialSyncFragment.newInstance();
                break;
            default:
                pageFragment = new TutorialAccountsFragment();
        }

        return pageFragment;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }
}
