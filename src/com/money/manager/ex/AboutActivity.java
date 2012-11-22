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

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.view.MenuItem;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.money.manager.ex.fragment.BaseFragmentActivity;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
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
		TextView txtFeedback = (TextView)findViewById(R.id.textViewLinkFeedback);
		text = "<a href=\"mailto:android.money.manager.ex@gmail.com\">" + (String) txtFeedback.getText() + "</a>";
		txtFeedback.setText(Html.fromHtml(text));
		txtFeedback.setMovementMethod(LinkMovementMethod.getInstance());
		// report set link
		TextView txtReport = (TextView)findViewById(R.id.textViewLinkProblem);
		text = "<a href=\"http://code.google.com/p/android-money-manager-ex/issues/entry\">" + (String) txtReport.getText() + "</a>";
		txtReport.setText(Html.fromHtml(text));
		txtReport.setMovementMethod(LinkMovementMethod.getInstance());
		// GPLv2 license
		TextView txtLicense = (TextView)findViewById(R.id.textViewLicense);
		text = "<a href=\"http://www.gnu.org/licenses/old-licenses/gpl-2.0.html\">" + (String) txtLicense.getText() + "</a>";
		txtLicense.setText(Html.fromHtml(text));
		txtLicense.setMovementMethod(LinkMovementMethod.getInstance());
		// Version application
		TextView txtVersion = (TextView)findViewById(R.id.textViewVersion);
		TextView txtBuild = (TextView)findViewById(R.id.textViewBuild);
		try {
			String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			txtVersion.setText(txtVersion.getText() + " " + version);
			String build = getString(R.string.application_build) + "." + 
					 	   getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			
			txtBuild.setText(txtBuild.getText() + " " + build);
		} catch (NameNotFoundException e) {
			Log.e(LOGCAT, e.getMessage());
		}
		// text changelog
		TextView txtChangeLog = (TextView)findViewById(R.id.textViewChangeLog);
		txtChangeLog.setText(Html.fromHtml("<u>" + getString(R.string.changelog) + "</u>"));
		txtChangeLog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyManagerApplication.showStartupChangeLog(AboutActivity.this, true);
			}
		});
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
