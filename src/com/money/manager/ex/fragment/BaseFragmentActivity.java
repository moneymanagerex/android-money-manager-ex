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

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.money.manager.ex.MoneyManagerApplication;

public abstract class BaseFragmentActivity extends SherlockFragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// apply theme application
		((MoneyManagerApplication)getApplication()).setThemeApplication(this);
	}
}
