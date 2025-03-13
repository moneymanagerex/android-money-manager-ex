/*
 * Copyright (C) 2025-2025 The Android Money Manager Ex Project Team
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

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.money.manager.ex.R;
import com.money.manager.ex.core.AbsRecyclerFragment;
import com.money.manager.ex.fragment.TipsDialogFragment;
import com.money.manager.ex.settings.PreferenceConstants;

import java.util.Objects;

public abstract class BaseRecyclerFragment extends AbsRecyclerFragment {
    private FloatingActionButton mFab;
    private MenuProvider menuProvider;
    private SearchView mSearchView;
    private static final String KEY_SHOWN_TIPS_WILDCARD = "BaseRecyclerFragment:isShowTipsWildcard";

    private boolean mShowMenuItemSearch = false;
    private boolean isShowTipsWildcard = false;
    private String mSearchHint = "";

    public static String mAction = null;

    private boolean mEnableFab = true;
    private boolean mEnableSearch = false;

    // Abstract method to get subtitle for the action bar
    public abstract String getSubTitle();

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initFab(view);
        initSearch();

        getRecyclerView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            private boolean isFabVisible = true;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && isFabVisible) {
                    isFabVisible = false;
                } else if (dy < 0 && !isFabVisible) {
                    isFabVisible = true;
                }
                setFabVisible(isFabVisible);
            }
        });

        // set subtitle in actionbar
        String subTitle = getSubTitle();
        if (!(TextUtils.isEmpty(subTitle)) && getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                Objects.requireNonNull(activity.getSupportActionBar()).setSubtitle(subTitle);
            }
        }
    }

    private void initFab(View view) {
        mFab = view.findViewById(R.id.fab);
        if (mFab != null && mEnableFab) {
            mFab.setVisibility(View.VISIBLE);
            mFab.setOnClickListener(v -> onFabClicked());
        }
    }

    public RecyclerView getRecyclerView() {
        return getView().findViewById(R.id.recyclerView);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Show wildcard tip if necessary
        Boolean searchType = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(getString(PreferenceConstants.PREF_TEXT_SEARCH_TYPE), Boolean.TRUE);

        if (isSearchMenuVisible() && !searchType && !isShowTipsWildcard) {
            TipsDialogFragment tipsSync = TipsDialogFragment.getInstance(getActivity().getApplicationContext(), "lookupswildcard");
            if (tipsSync != null) {
                tipsSync.setTips(getString(R.string.lookups_wildcard));
                tipsSync.show(getActivity().getSupportFragmentManager(), "lookupswildcard");
                isShowTipsWildcard = true;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_SHOWN_TIPS_WILDCARD, isShowTipsWildcard);
        super.onSaveInstanceState(outState);
    }

    // Search-related methods
    public boolean isSearchMenuVisible() {
        return mShowMenuItemSearch;
    }

    public String getSearchHint() {
        return mSearchHint;
    }

    public void setFabVisible(boolean isVisible) {
        if (mFab != null) {
            mFab.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    public void onFabClicked() {
        // Override this method to handle FAB click
    }

    private void initSearch() {
        if (!mEnableSearch) return;

        if (menuProvider != null) return;
        menuProvider = new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
                inflater.inflate(R.menu.base_search_menu, menu);
                MenuItem searchItem = menu.findItem(R.id.action_search);
                mSearchView = (SearchView) searchItem.getActionView();
                setupSearchView();
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                return false;
            }
        };
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner());
    }

    private void setupSearchView() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                handleSearchQuery(newText);
                return true;
            }
        });
    }

    protected void setResult() {
        // Implement result handling logic
    }

    protected void enableFab(boolean enable) {
        mEnableFab = enable;
    }

    protected void handleSearchQuery(String query) {
        // Default empty implementation
    }

    protected void enableSearch(boolean enable) {
        mEnableSearch = enable;
    }
}