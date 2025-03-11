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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.money.manager.ex.R;
import com.money.manager.ex.core.AbsRecyclerFragment;
import com.money.manager.ex.core.SearchViewFormatter;
import com.money.manager.ex.fragment.TipsDialogFragment;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.settings.PreferenceConstants;

public abstract class BaseRecyclerFragment extends AbsRecyclerFragment {
    private FloatingActionButton mFloatingActionButton;
    private static final String KEY_SHOWN_TIPS_WILDCARD = "BaseRecyclerFragment:isShowTipsWildcard";

    private boolean mShowMenuItemSearch = false;
    private boolean mMenuItemSearchIconified = true;
    private boolean isShowTipsWildcard = false;
    private String mSearchHint = "";

    public static String mAction = null;

    // Abstract method to get subtitle for the action bar
    public abstract String getSubTitle();

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupFloatingActionButton(view);
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
    }

    private void setupFloatingActionButton(View view) {
        mFloatingActionButton = view.findViewById(R.id.fab);
        if (mFloatingActionButton != null) {
            mFloatingActionButton.setOnClickListener(v -> onFloatingActionButtonClicked());
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isSearchMenuVisible() && getActivity() instanceof AppCompatActivity) {
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
                    return BaseRecyclerFragment.this.onPreQueryTextChange(s);
                }
            });
            searchView.setIconified(isMenuItemSearchIconified());
            itemSearch.setActionView(searchView);

            SearchViewFormatter formatter = new SearchViewFormatter();
            formatter.setSearchIconResource(R.drawable.ic_action_search_dark, true, true);
            formatter.setSearchCloseIconResource(R.drawable.ic_action_content_clear_dark);
            formatter.setSearchTextColorResource(R.color.abc_primary_text_material_dark);
            formatter.setSearchHintText(getSearchHint());
            formatter.format(searchView);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getActivity() instanceof MainActivity)
                return super.onOptionsItemSelected(item);
            this.setResultAndFinish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    public void setSearchMenuVisible(boolean show) {
        this.mShowMenuItemSearch = show;
    }

    public void setResultAndFinish() {
        this.setResult();
        getActivity().finish();
    }

    public boolean isMenuItemSearchIconified() {
        return mMenuItemSearchIconified;
    }

    public void setMenuItemSearchIconified(boolean iconified) {
        this.mMenuItemSearchIconified = iconified;
    }

    public String getSearchHint() {
        return mSearchHint;
    }

    public void setSearchHint(@NonNull String hint) {
        this.mSearchHint = hint;
    }

    // Floating action button methods
    public FloatingActionButton getFloatingActionButton() {
        return mFloatingActionButton;
    }

    public void setFabVisible(boolean isVisible) {
        if (mFloatingActionButton != null) {
            mFloatingActionButton.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    public void onFloatingActionButtonClicked() {
        // Override this method to handle FAB click
    }

    protected boolean onPreQueryTextChange(String newText) {
        if (PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(getString(PreferenceConstants.PREF_TEXT_SEARCH_TYPE), Boolean.TRUE)) {
            newText = "%" + newText;
        }
        return onQueryTextChange(newText);
    }

    protected boolean onQueryTextChange(String newText) {
        // Implement query handling logic
        return true;
    }

    protected void setResult() {
        // Implement result handling logic
    }
}