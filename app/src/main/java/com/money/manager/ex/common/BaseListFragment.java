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
package com.money.manager.ex.common;

import android.animation.LayoutTransition;
import android.os.Bundle;

import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.money.manager.ex.R;
import com.money.manager.ex.core.AbsListFragment;
import com.money.manager.ex.core.SearchViewFormatter;
import com.money.manager.ex.fragment.TipsDialogFragment;
import com.money.manager.ex.settings.PreferenceConstants;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

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
    // hint search view
    private String mSearchHint = "";

    public static String mAction = null;

    // abstract method
    public abstract String getSubTitle();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupMenuProviders();

        setupFloatingActionButton(view);

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            private boolean isFabVisible = true;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem > 0 && isFabVisible) {
                    isFabVisible = false;
                } else if (firstVisibleItem == 0 && !isFabVisible) {
                    isFabVisible = true;
                }
                setFabVisible(isFabVisible);
            }
        });
    }

    private void setupMenuProviders() {
        // Get the MenuHost from the parent Activity
        MenuHost menuHost = requireActivity();
        // Add the MenuProvider to the MenuHost
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                if (isSearchMenuVisible() && getActivity() != null && getActivity() instanceof AppCompatActivity) {
                    // Place an action bar item for searching.
                    final MenuItem itemSearch = menu.add(Menu.NONE, R.id.menu_query_mode, 1000, R.string.search);
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
                    //formatter.setSearchHintColorResource(R.color.mmx_hint_foreground_material_dark);

                    formatter.setSearchHintText(getSearchHint());

                    formatter.format(searchView);
                }
                old_onCreateOptionsMenu(menu, menuInflater);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == android.R.id.home) {
                    // set result and exit
                    setResultAndFinish();
                    return true; // consumed here
                }
                return old_onOptionsItemSelected(menuItem);
            }

            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                old_onPrepareOptionsMenu(menu);
            }

        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        // Chiamata al metodo che le classi derivate possono sovrascrivere
        addCustomMenuProviders(menuHost);
    }

    // Metodo hook che le classi derivate possono sovrascrivere
    protected void addCustomMenuProviders(@SuppressWarnings("unused") MenuHost menuHost) {
        // Implementazione di default vuota
    }

    // for retrocompatibility
    public void old_onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    }
    // for retrocompatibility
    public boolean old_onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle menu item clicks
        return false;
    }
    // for retrocompatibility
    public void old_onPrepareOptionsMenu(@NonNull Menu menu) {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set animation
        getListView().setLayoutTransition(new LayoutTransition());
        // saved instance
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SHOWN_TIPS_WILDCARD)) {
            isShowTipsWildcard = savedInstanceState.getBoolean(KEY_SHOWN_TIPS_WILDCARD);
        }

        // set subtitle in actionbar
        String subTitle = getSubTitle();
        if (!(TextUtils.isEmpty(subTitle)) && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                activity.getSupportActionBar().setSubtitle(subTitle);
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

        if (isSearchMenuVisible() && !searchType && !isShowTipsWildcard) {
            // show tooltip for wildcard
            TipsDialogFragment tipsSync = TipsDialogFragment.getInstance(getActivity().getApplicationContext(), "lookupswildcard");
            if (tipsSync != null) {
                tipsSync.setTips(getString(R.string.lookups_wildcard));
                // tipsSync.setCheckDontShowAgain(true);
                tipsSync.show(getActivity().getSupportFragmentManager(), "lookupswildcard");
                isShowTipsWildcard = true; // set shown
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_SHOWN_TIPS_WILDCARD, isShowTipsWildcard);
        super.onSaveInstanceState(outState);
    }

    public boolean isSearchMenuVisible() {
        return mShowMenuItemSearch;
    }

    public void setSearchMenuVisible(boolean mShowMenuItemSearch) {
        this.mShowMenuItemSearch = mShowMenuItemSearch;
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

    public void setFabVisible(boolean isVisible) {
        if (mFloatingActionButton != null) {
            mFloatingActionButton.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    public void onFloatingActionButtonClicked() {
    }

    // End floating button methods.

    protected boolean onPreQueryTextChange(String newText) {
        if (PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(getString(PreferenceConstants.PREF_TEXT_SEARCH_TYPE), Boolean.TRUE))
            newText = "%" + newText;

        return onQueryTextChange(newText);
    }

    protected boolean onQueryTextChange(String newText) {
        return true;
    }

    /**
     * metodo per l'implementazione del ritorno dei dati
     */
    protected void setResult() { }

    public void setupFloatingActionButton(View view) {
        mFloatingActionButton = view.findViewById(R.id.fab);
        if (mFloatingActionButton != null) {
            mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFloatingActionButtonClicked();
                }
            });
        }
    }
}
