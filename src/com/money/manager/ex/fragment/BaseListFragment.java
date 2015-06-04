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
package com.money.manager.ex.fragment;

import android.animation.LayoutTransition;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.melnykov.fab.FloatingActionButton;
import com.money.manager.ex.MainActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.core.AbsListFragment;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.SearchViewFormatter;
import com.money.manager.ex.settings.PreferenceConstants;

/**
 *
 */
public abstract class BaseListFragment
        extends AbsListFragment {

    private static final String KEY_SHOWN_TIPS_WILDCARD = "BaseListFragment:isShowTipsWildcard";

    FloatingActionButton mFloatingActionButton;
    // menu items
    private boolean mShowMenuItemSearch = false;
    private boolean mMenuItemSearchIconified = true;
    // flag for tips wildcard
    private boolean isShowTipsWildcard = false;
    // hint searchview
    private String mSearchHint = "";

    // abstract method
    public abstract String getSubTitle();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // set theme
        Core core = new Core(getActivity().getApplicationContext());
        try {
            getActivity().setTheme(core.getThemeApplication());
        } catch (Exception e) {
            Log.e(BaseListFragment.class.getSimpleName(), e.getMessage());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mFloatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab);
        if (mFloatingActionButton != null) {
            mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFloatingActionButtonClickListener();
                }
            });
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set animation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            getListView().setLayoutTransition(new LayoutTransition());
        // saved instance
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_SHOWN_TIPS_WILDCARD))
                isShowTipsWildcard = savedInstanceState.getBoolean(KEY_SHOWN_TIPS_WILDCARD);
        }
        // set subtitle in actionbar
        if (!(TextUtils.isEmpty(getSubTitle()))) {
            if (getActivity() instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                if (activity != null) {
                    activity.getSupportActionBar().setSubtitle(getSubTitle());
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // show tooltip wildcard
        // check search type
        Boolean searchType = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(getString(PreferenceConstants.PREF_TEXT_SEARCH_TYPE), Boolean.TRUE);

        if (isShowMenuItemSearch() && !searchType && !isShowTipsWildcard) {
            // show tooltip for wildcard
            TipsDialogFragment tipsDropbox = TipsDialogFragment.getInstance(getActivity().getApplicationContext(), "lookupswildcard");
            if (tipsDropbox != null) {
                tipsDropbox.setTips(getString(R.string.lookups_wildcard));
                // tipsDropbox.setCheckDontShowAgain(true);
                tipsDropbox.show(getActivity().getSupportFragmentManager(), "lookupswildcard");
                isShowTipsWildcard = true; // set shown
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isShowMenuItemSearch() && getActivity() != null && getActivity() instanceof AppCompatActivity) {
            // Place an action bar item for searching.
            final MenuItem itemSearch = menu.add(0, R.id.menu_query_mode, 1000, R.string.search);
            itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            SearchView searchView = new SearchView(getActivity());
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return BaseListFragment.this.onPreQueryTextChange(s);
                }
            });
            searchView.setIconified(isMenuItemSearchIconified());
            itemSearch.setActionView(searchView);

            SearchViewFormatter formatter = new SearchViewFormatter();

            formatter.setSearchIconResource(R.drawable.ic_action_search_dark, true, true);
            formatter.setSearchCloseIconResource(R.drawable.ic_action_content_clear_dark);
            formatter.setSearchTextColorResource(R.color.abc_primary_text_material_dark);
            formatter.setSearchHintColorResource(R.color.hint_foreground_material_dark);

            formatter.setSearchHintText(getSearchHint());

            formatter.format(searchView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getActivity() != null && getActivity() instanceof MainActivity)
                    return super.onOptionsItemSelected(item);
                // set result and exit
                this.setResultAndFinish();
                return true; // consumed here
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected boolean onPreQueryTextChange(String newText) {
        if (PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(getString(PreferenceConstants.PREF_TEXT_SEARCH_TYPE), Boolean.TRUE))
            newText = "%" + newText;

        return onQueryTextChange(newText);
    }

    protected boolean onQueryTextChange(String newText) {
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_SHOWN_TIPS_WILDCARD, isShowTipsWildcard);
        super.onSaveInstanceState(outState);
    }

    public boolean isShowMenuItemSearch() {
        return mShowMenuItemSearch;
    }

    public void setShowMenuItemSearch(boolean mShowMenuItemSearch) {
        this.mShowMenuItemSearch = mShowMenuItemSearch;
    }

    /**
     * metodo per l'implementazione del ritorno dei dati
     */
    protected void setResult() {
    }

    public void setResultAndFinish() {
        this.setResult();
        getActivity().finish();
    }

    public boolean isMenuItemSearchIconified() {
        return mMenuItemSearchIconified;
    }

    public void setMenuItemSearchIconified(boolean mMenuItemSearchIconified) {
        this.mMenuItemSearchIconified = mMenuItemSearchIconified;
    }

    public String getSearchHint() {
        return mSearchHint;
    }

    public void setSearchHint(@NonNull String mSearchHint) {
        this.mSearchHint = mSearchHint;
    }

    // Floating button methods

    public FloatingActionButton getFloatingActionButton() {
        return mFloatingActionButton;
    }

    public void setFloatingActionButtonVisible(boolean visible) {
        if (mFloatingActionButton != null) {
            mFloatingActionButton.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setFloatingActionButtonAttachListView(boolean attachListView) {
        if (mFloatingActionButton != null) {
            mFloatingActionButton.attachToListView(getListView());
        }
    }

    public void onFloatingActionButtonClickListener() {
        return;
    }

    // End floating button methods.
}
