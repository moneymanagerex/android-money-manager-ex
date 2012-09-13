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
package com.money.manager.ex;

import java.util.Formatter;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.view.MenuItem;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.fragment.BaseFragmentActivity;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 0.0.1
 * 
 */
public class AboutActivity extends BaseFragmentActivity {
	private static final String LOGCAT = AboutActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_activity);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// take a object into layout
		String text;
		// report set link
		TextView txtReport = (TextView)findViewById(R.id.textViewLinkProblem);
		text = "<a href=\"http://code.google.com/p/android-money-manager-ex/issues/list\">" + (String) txtReport.getText() + "</a>";
		txtReport.setText(Html.fromHtml(text));
		txtReport.setMovementMethod(LinkMovementMethod.getInstance());
		// GPLv2 license
		TextView txtLicense = (TextView)findViewById(R.id.textViewLicense);
		text = "<a href=\"http://www.gnu.org/licenses/old-licenses/gpl-2.0.html\">" + (String) txtLicense.getText() + "</a>";
		txtLicense.setText(Html.fromHtml(text));
		txtLicense.setMovementMethod(LinkMovementMethod.getInstance());
		// Version application
		TextView txtVersion = (TextView)findViewById(R.id.textViewVersion);
		try {
			text = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			text += " (build " + new Formatter().format("%05d", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode) + ")";
		} catch (NameNotFoundException e) {
			Log.e(LOGCAT, e.getMessage());
		}
		txtVersion.setText(txtVersion.getText() + " " + text);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return false;
		}
		return super.onOptionsItemSelected(item);
	}
}
