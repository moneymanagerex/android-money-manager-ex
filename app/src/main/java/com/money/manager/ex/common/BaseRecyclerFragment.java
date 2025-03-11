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

import com.melnykov.fab.FloatingActionButton;
import com.money.manager.ex.R;
import com.money.manager.ex.core.AbsRecyclerFragment;
import com.money.manager.ex.core.SearchViewFormatter;
import com.money.manager.ex.fragment.TipsDialogFragment;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.settings.PreferenceConstants;

public abstract class BaseRecyclerFragment extends AbsRecyclerFragment {
    // 复用BaseListFragment中的FAB、搜索菜单等逻辑
    private FloatingActionButton mFloatingActionButton;
    private static final String KEY_SHOWN_TIPS_WILDCARD = "BaseListFragment:isShowTipsWildcard";

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
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupFloatingActionButton(view);
        attachFloatingActionButtonToRecyclerView();
    }

    private void setupFloatingActionButton(View view) {
        mFloatingActionButton = view.findViewById(R.id.fab);
        if (mFloatingActionButton != null) {
            mFloatingActionButton.setOnClickListener(v -> onFloatingActionButtonClicked());
        }
    }

    private void attachFloatingActionButtonToRecyclerView() {
        if (mFloatingActionButton != null) {
            mFloatingActionButton.attachToRecyclerView(getRecyclerView());
        }
    }

    public RecyclerView getRecyclerView() {
        return (RecyclerView) getView().findViewById(R.id.recyclerView);
    }

    // 其他复用BaseListFragment的代码（搜索菜单等）
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
                    return BaseRecyclerFragment.this.onPreQueryTextChange(s);
                }
            });
//            searchView.setIconifiedByDefault(isMenuItemSearchIconified());
            searchView.setIconified(isMenuItemSearchIconified());
            itemSearch.setActionView(searchView);

            SearchViewFormatter formatter = new SearchViewFormatter();
            formatter.setSearchIconResource(R.drawable.ic_action_search_dark, true, true);
            formatter.setSearchCloseIconResource(R.drawable.ic_action_content_clear_dark);
            formatter.setSearchTextColorResource(R.color.abc_primary_text_material_dark);
            //formatter.setSearchHintColorResource(R.color.mmx_hint_foreground_material_dark);

            formatter.setSearchHintText(getSearchHint());

            formatter.format(searchView);

//            if (getSearchCollapsed()) {
//                itemSearch.collapseActionView();
//            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getActivity() != null && getActivity() instanceof MainActivity)
                return super.onOptionsItemSelected(item);
            // set result and exit
            this.setResultAndFinish();
            return true; // consumed here
        }
        return super.onOptionsItemSelected(item);
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

    public com.melnykov.fab.FloatingActionButton getFloatingActionButton() {
        return mFloatingActionButton;
    }

    public void setFloatingActionButtonVisible(boolean visible) {
        if (mFloatingActionButton != null) {
            mFloatingActionButton.setVisibility(visible ? View.VISIBLE : View.GONE);
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

}