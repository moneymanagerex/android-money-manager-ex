/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.money.manager.ex.tutorial;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Adapter that returns the tutorial pages.
 * Created by Alen on 17/03/2015.
 */
public class TutorialPagerAdapter extends FragmentStatePagerAdapter {
    public TutorialPagerAdapter(FragmentManager fm, Activity parent) {
        super(fm);

        mParentActivity = parent;
    }

    private Activity mParentActivity;

    @Override
    public Fragment getItem(int i) {
        Fragment pageFragment;

        switch(i){
            case 0:
                pageFragment = TutorialAccountsFragment.newInstance();
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
                pageFragment = TutorialAccountsFragment.newInstance();
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
