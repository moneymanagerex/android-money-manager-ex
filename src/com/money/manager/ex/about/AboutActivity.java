/*******************************************************************************
 * Copyright (C) 2012 alessandro
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 ******************************************************************************/
package com.money.manager.ex.about;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.money.manager.ex.R;
import com.money.manager.ex.fragment.BaseFragmentActivity;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
 * 
 */
public class AboutActivity extends BaseFragmentActivity implements ActionBar.TabListener{
	@SuppressWarnings("unused")
	private static final String LOGCAT = AboutActivity.class.getSimpleName();
	private static final String BUNDLE_KEY_TABINDEX = "AboutActivity:tabindex";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setDisplayHomeAsUpEnabled(true);
		
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		ActionBar.Tab tab1 = getSupportActionBar().newTab();
		tab1.setText(R.string.about);
		tab1.setTabListener(this);

		ActionBar.Tab tab2 = getSupportActionBar().newTab();
		tab2.setText(R.string.changelog);
		tab2.setTabListener(this);
		
		ActionBar.Tab tab3 = getSupportActionBar().newTab();
		tab3.setText(R.string.credits);
		tab3.setTabListener(this);

		getSupportActionBar().addTab(tab1);
		getSupportActionBar().addTab(tab2);
		getSupportActionBar().addTab(tab3);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putInt(BUNDLE_KEY_TABINDEX, getSupportActionBar()
				.getSelectedTab().getPosition());
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		getSupportActionBar().setSelectedNavigationItem(
				savedInstanceState.getInt(BUNDLE_KEY_TABINDEX));
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		SherlockFragment fragment = null;
		switch (tab.getPosition()) {
		case 0: //about
			fragment = new AboutFragment();
			break;
		case 1: //changelog
			fragment = new AboutChangelogFragment();
			break;
		case 2: //credits
			fragment = new AboutCreditsFragment();
			break;
		default:
			break;
		}
		if (fragment != null)
			ft.replace(android.R.id.content, fragment);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
}
