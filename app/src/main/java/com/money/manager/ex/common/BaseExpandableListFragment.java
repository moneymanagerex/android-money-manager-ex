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
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.melnykov.fab.FloatingActionButton;
import com.money.manager.ex.R;
import com.money.manager.ex.core.SearchViewFormatter;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.fragment.TipsDialogFragment;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.settings.PreferenceConstants;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import timber.log.Timber;

public abstract class BaseExpandableListFragment
        extends ExpandableListFragment {

    // saved instance
    private static final String KEY_SHOWN_TIPS_WILDCARD = "BaseListFragment:isShowTipsWildcard";
    // menu items
    private boolean mDisplayShowCustomEnabled = false;
    private boolean mShowMenuItemSearch = false;
    private boolean mMenuItemSearchIconified = true;
    // flag for tips wildcard
    private boolean isShowTipsWildcard = false;
    FloatingActionButton mFloatingActionButton;

    public abstract String getSubTitle();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // set theme
        UIHelper uiHelper = new UIHelper(getActivity().getApplicationContext());
        try {
            getActivity().setTheme(uiHelper.getThemeId());
        } catch (Exception e) {
            Timber.e(e);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // set animation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getExpandableListView().setLayoutTransition(new LayoutTransition());
        }
        // saved instance
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_SHOWN_TIPS_WILDCARD)) {
                isShowTipsWildcard = savedInstanceState.getBoolean(KEY_SHOWN_TIPS_WILDCARD);
            }
        }
        // set subtitle in actionbar
        if (!(TextUtils.isEmpty(getSubTitle()))) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                activity.getSupportActionBar().setSubtitle(getSubTitle());
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

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

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.abs_list_fragment, container, false);
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isShowMenuItemSearch() && getActivity() != null && getActivity() instanceof AppCompatActivity) {
            // Place an action bar item for searching.
            final MenuItem itemSearch = menu.add(0, R.id.menu_query_mode, 1000, R.string.search);

            MenuItemCompat.setShowAsAction(itemSearch, MenuItem.SHOW_AS_ACTION_ALWAYS);
//            ActionBarActivity activity = (ActionBarActivity) getActivity();
//            AppCompatActivity activity = (AppCompatActivity) getActivity();

            SearchView searchView = new SearchView(getActivity());
            if (searchView != null) {
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return BaseExpandableListFragment.this.onPreQueryTextChange(s);
                    }
                });
                searchView.setIconified(isMenuItemSearchIconified());
                MenuItemCompat.setActionView(itemSearch, searchView);

                SearchViewFormatter formatter = new SearchViewFormatter();

                formatter.setSearchIconResource(R.drawable.ic_action_search_dark, true, true);
                formatter.setSearchCloseIconResource(R.drawable.ic_action_content_clear_dark);
                formatter.setSearchTextColorResource(R.color.abc_primary_text_material_dark);
                //formatter.setSearchHintColorResource(R.color.hint_foreground_material_dark);

                formatter.format(searchView);
            }
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
            case R.id.menu_query_mode:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                    onMenuItemSearchClick(item);
                return true; // consumed here
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onMenuItemSearchClick(MenuItem item) {
        View searchView = getActivity().getActionBar().getCustomView();
        final EditText edtSearch = (EditText) searchView.findViewById(R.id.editTextSearchView);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        // se in visualizzazione prendo l'edittext
        if (mDisplayShowCustomEnabled == false) {
            // rendo visibile l'edittext di ricerca
            edtSearch.setText("");
            edtSearch.requestFocus();
            // rendo visibile la keyboard
            imm.showSoftInput(edtSearch, 0);
            MenuItemCompat.setActionView(item, searchView);
            // aggiorno lo stato
            mDisplayShowCustomEnabled = true;
        } else {
            // controllo se ho del testo lo pulisco
            if (TextUtils.isEmpty(edtSearch.getText().toString())) {
                // nascondo la keyboard
                imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
                // tolgo la searchview
                MenuItemCompat.setActionView(item, null);
                // aggiorno lo stato
                mDisplayShowCustomEnabled = false;
            } else {
                // annullo il testo
                edtSearch.setText(null);
                onPreQueryTextChange("");
            }
        }
    }

    protected boolean onPreQueryTextChange(String newText) {
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(getString(PreferenceConstants.PREF_TEXT_SEARCH_TYPE), Boolean.TRUE))
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


    /**
     * @return the mShowMenuItemSearch
     */
    public boolean isShowMenuItemSearch() {
        return mShowMenuItemSearch;
    }

    /**
     *
     * @param mShowMenuItemSearch the mShowMenuItemSearch to set
     */
    public void setShowMenuItemSearch(boolean mShowMenuItemSearch) {
        this.mShowMenuItemSearch = mShowMenuItemSearch;
    }

    /**
     * metodo per l'implementazione del ritorno dei dati
     */
    protected void setResult() {
    }

    public void setResultAndFinish() {
        // chiamo l'impostazione dei dati e chiudo l'activity
        this.setResult();
        // chiudo l'activity dove sono collegato
        getActivity().finish();
    }

    public boolean isMenuItemSearchIconified() {
        return mMenuItemSearchIconified;
    }

    public void setMenuItemSearchIconified(boolean mMenuItemSearchIconified) {
        this.mMenuItemSearchIconified = mMenuItemSearchIconified;
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
            mFloatingActionButton.attachToListView(getExpandableListView());
        }
    }

    public void onFloatingActionButtonClickListener() {
        return;
    }

    // End floating button events.
}
