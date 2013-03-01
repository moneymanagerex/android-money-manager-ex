/*******************************************************************************
 * Copyright (C) 2012 The Android Money Manager Ex Project
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
 ******************************************************************************/
package com.money.manager.ex.fragment;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;

public class BaseListFragment extends SherlockListFragment {
	// ID MENU
	private static final int MENU_ITEM_SEARCH = 1000;
	// stato della visualizzazione menu
	private boolean mDisplayShowCustomEnabled = false;
	// flag che per la visualizzazione del menu'
	private boolean mShowMenuItemSearch = false;
	
	@Override
	public void onCreateOptionsMenu(Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		if (isShowMenuItemSearch()) {
	        // Place an action bar item for searching.
	        final MenuItem itemSearch = menu.add(0, MENU_ITEM_SEARCH, MENU_ITEM_SEARCH, R.string.search);
	        itemSearch.setIcon(new Core(getActivity()).resolveIdAttribute(R.attr.ic_action_search));
	        itemSearch.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	        // uso il Compat per avere l'oggetto
	        View searchView = SearchViewCompat.newSearchView(getActivity());
	        if (searchView != null) {
	            SearchViewCompat.setOnQueryTextListener(searchView,
	                    new OnQueryTextListenerCompat() {
	                @Override
	                public boolean onQueryTextChange(String newText) {
	                    return BaseListFragment.this.onQueryTextChange(newText);
	                }
	            });
	            itemSearch.setActionView(searchView);
	        } else {
	        	// prendo il layout
	        	/*searchView = LayoutInflater.from(getActivity()).inflate(R.layout.actionbar_searchview, null);
	        	// prendo l'edit text
	        	final EditText edtSearch = (EditText)searchView.findViewById(R.id.editTextSearchView);
	        	// prendo il menu
	        	Drawable drawable = getResources().getDrawable(R.drawable.ic_clear_search_api_holo_light); 
	        	drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
	        	// lo imposto nell'edit
	        	edtSearch.setCompoundDrawables(null, null, drawable, null);
	        	// imposto l'hint
	        	edtSearch.setHint(R.string.search);
	        	edtSearch.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						BaseListFragment.this.onQueryTextChange(edtSearch.getText().toString());
						return false;
					}
				});
	        	edtSearch.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_UP && edtSearch.getCompoundDrawables()[2] != null) {
							Rect rectangleBounds = edtSearch.getCompoundDrawables()[2].getBounds();
							final int x = (int) event.getX();
							final int y = (int) event.getY();

							if (x >= (edtSearch.getRight() - rectangleBounds.width()) && x <= (edtSearch.getRight() - edtSearch.getPaddingRight()) &&
								y >= edtSearch.getPaddingTop() && y <= (edtSearch.getHeight() - edtSearch.getPaddingBottom())) {
								BaseListFragment.this.onMenuItemSearchClick(itemSearch);
							}
						}
						return false;
					}
				});
	        	((SherlockFragmentActivity)getActivity()).getSupportActionBar().setCustomView(searchView);*/
	        	SearchView actionSearchView = new SearchView(getSherlockActivity().getSupportActionBar().getThemedContext());
	        	actionSearchView.setOnQueryTextListener(new OnQueryTextListener() {
					
					@Override
					public boolean onQueryTextSubmit(String query) {
						return false;
					}
					
					@Override
					public boolean onQueryTextChange(String newText) {
						return BaseListFragment.this.onQueryTextChange(newText);
					}
				});
	        	itemSearch.setActionView(actionSearchView);
	        }
	        String textToast = "<small><b>" + getString(R.string.lookups_wildcard) + "</b></small>";
	        Toast.makeText(getActivity(), Html.fromHtml(textToast), Toast.LENGTH_LONG).show();
		}
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case android.R.id.home:
    		//imposto il risultato ed esco
    		this.setResultAndFinish();
    		
    		break;
    	case MENU_ITEM_SEARCH:
    		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
    			onMenuItemSearchClick(item);
    		}
    		break;
    	}
    	return false;
    }
	
	protected boolean onQueryTextChange(String newText) {
		return true;
	}
	
    protected void onMenuItemSearchClick(MenuItem item) {
		View searchView = ((SherlockFragmentActivity)getActivity()).getSupportActionBar().getCustomView();
		final EditText edtSearch = (EditText)searchView.findViewById(R.id.editTextSearchView);
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		// se in visualizzazione prendo l'edittext
		if (mDisplayShowCustomEnabled == false) {
			// rendo visibile l'edittext di ricerca
			edtSearch.setText("");
			edtSearch.requestFocus();
			// rendo visibile la keyboard
			imm.showSoftInput(edtSearch, 0);
			item.setActionView(searchView);
			// aggiorno lo stato
			mDisplayShowCustomEnabled = true;
		} else {
			// controllo se ho del testo lo pulisco
			if (TextUtils.isEmpty(edtSearch.getText().toString())) {
				// nascondo la keyboard
				imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
				// tolgo la searchview
				item.setActionView(null);
				// aggiorno lo stato
				mDisplayShowCustomEnabled = false;
			} else {
				// annullo il testo
				edtSearch.setText(null);
				onQueryTextChange("");
			}
		}	
    }

	/**
	 * @return the mShowMenuItemSearch
	 */
	public boolean isShowMenuItemSearch() {
		return mShowMenuItemSearch;
	}

	/**
	 * @param mShowMenuItemSearch the mShowMenuItemSearch to set
	 */
	public void setShowMenuItemSearch(boolean mShowMenuItemSearch) {
		this.mShowMenuItemSearch = mShowMenuItemSearch;
	}
	
	/**
	 * metodo per l'implementazione del ritorno dei dati
	 */
	protected void setResult() {}
	
	public void setResultAndFinish() {
		//chiamo l'impostazione dei dati e chiudo l'activity
		this.setResult();
		//chiudo l'activity dove sono collegato
		getActivity().finish();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// set animation
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getListView().setLayoutTransition(new LayoutTransition());
	}
}
	
