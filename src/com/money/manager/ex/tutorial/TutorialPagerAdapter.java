package com.money.manager.ex.tutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Adapter that returns the tutorial pages.
 * Created by Alen on 17/03/2015.
 */
public class TutorialPagerAdapter extends FragmentStatePagerAdapter {
    public TutorialPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment pageFragment;

        switch(i){
            case 0:
                pageFragment = getPage(i);
                break;
            case 1:
                pageFragment = getPage(i);
                break;
            case 2:
                pageFragment = TutorialPageCurrenciesFragment.newInstance();
                break;
            case 3:
                pageFragment = getPage(i);
                break;
            case 4:
                pageFragment = TutorialPageSyncFragment.newInstance();
                break;
            default:
                pageFragment = getPage(i);
        }

        return pageFragment;
    }

    private Fragment getPage(int i){
        Fragment fragment = new TutorialPageAccountsFragment();
        Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt(TutorialPageAccountsFragment.ARG_OBJECT, i + 1);
        fragment.setArguments(args);
        return fragment;
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
