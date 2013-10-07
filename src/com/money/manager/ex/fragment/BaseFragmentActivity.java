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

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;

public abstract class BaseFragmentActivity extends SherlockFragmentActivity {
	private boolean mShownRotateInDebugMode = false;
	
	@Override
	protected void onCreate(Bundle savedInstance) {
		// set theme
		Core core =  new Core(getApplicationContext());
		try {
			this.setTheme(core.getThemeApplication());
		} catch (Exception e) {
			Log.e(BaseFragmentActivity.class.getSimpleName(), e.getMessage());
		}
		super.onCreate(savedInstance);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//check if debug mode
		if (BuildConfig.DEBUG) {
			if (isShownRotateInDebugMode()) {
				MenuItem item = menu.add(0, R.id.menu_debug_rotate, 0, null);
				item.setIcon(android.R.drawable.ic_menu_always_landscape_portrait);
				item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*if (item.getItemId() == android.R.id.home) {
			this.finish();
		} else */
		if (item.getItemId() == R.id.menu_debug_rotate) {
			forceRotateScreenActivity();
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void forceRotateScreenActivity() {
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)  {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
	}

	/**
	 * @param mShownRotateInDebugMode the mShownRotateInDebugMode to set
	 */
	public void setShownRotateInDebugMode(boolean mShownRotateInDebugMode) {
		this.mShownRotateInDebugMode = mShownRotateInDebugMode;
	}

	/**
	 * @return the mShownRotateInDebugMode
	 */
	public boolean isShownRotateInDebugMode() {
		return mShownRotateInDebugMode;
	}
}
