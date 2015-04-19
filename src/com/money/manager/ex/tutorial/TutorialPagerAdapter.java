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
