package com.money.manager.ex.tutorial;

import android.app.Activity;
import android.os.Bundle;
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
                //pageFragment = TutorialPageAccountsFragment.newInstance();
                pageFragment = TutorialNativePageAccountsFragment.newInstance();
                break;
            case 1:
                pageFragment = TutorialPageTransactionsFragment.newInstance();
                break;
            case 2:
                pageFragment = TutorialPageGlobalFragment.newInstance();
                break;
            case 3:
                pageFragment = TutorialPageFinancialOverviewFragment.newInstance();
                break;
            case 4:
                pageFragment = TutorialPageSyncFragment.newInstance();
                break;
            default:
//                pageFragment = getPage(i);
                pageFragment = TutorialPageAccountsFragment.newInstance();
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
