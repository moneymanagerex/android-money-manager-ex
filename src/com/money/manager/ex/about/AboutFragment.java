package com.money.manager.ex.about;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.money.manager.ex.DonateActivity;
import com.money.manager.ex.R;

public class AboutFragment extends SherlockFragment {
	private static final String LOGCAT = AboutFragment.class.getSimpleName();
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.about_activity, container, false);
		
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// take a object into layout
		String text;
		TextView txtFeedback = (TextView)view.findViewById(R.id.textViewLinkFeedback);
		text = "<a href=\"mailto:android.money.manager.ex@gmail.com\">" + (String) txtFeedback.getText() + "</a>";
		txtFeedback.setText(Html.fromHtml(text));
		txtFeedback.setMovementMethod(LinkMovementMethod.getInstance());
		// report set link
		TextView txtReport = (TextView)view.findViewById(R.id.textViewLinkProblem);
		text = "<a href=\"http://code.google.com/p/android-money-manager-ex/issues/entry\">" + (String) txtReport.getText() + "</a>";
		txtReport.setText(Html.fromHtml(text));
		txtReport.setMovementMethod(LinkMovementMethod.getInstance());
		// GPLv2 license
		TextView txtLicense = (TextView)view.findViewById(R.id.textViewLicense);
		text = "<a href=\"http://www.gnu.org/licenses/old-licenses/gpl-2.0.html\">" + (String) txtLicense.getText() + "</a>";
		txtLicense.setText(Html.fromHtml(text));
		txtLicense.setMovementMethod(LinkMovementMethod.getInstance());
		// Version application
		TextView txtVersion = (TextView)view.findViewById(R.id.textViewVersion);
		TextView txtBuild = (TextView)view.findViewById(R.id.textViewBuild);
		try {
			String version = getSherlockActivity().getPackageManager().getPackageInfo(getSherlockActivity().getPackageName(), 0).versionName;
			txtVersion.setText(txtVersion.getText() + " " + version);
			String build = getString(R.string.application_build) + "r" + 
					 	   getSherlockActivity().getPackageManager().getPackageInfo(getSherlockActivity().getPackageName(), 0).versionCode;
			
			txtBuild.setText(txtBuild.getText() + " " + build);
		} catch (NameNotFoundException e) {
			Log.e(LOGCAT, e.getMessage());
		}
		// text changelog
		/*TextView txtChangeLog = (TextView)view.findViewById(R.id.textViewChangeLog);
		txtChangeLog.setText(Html.fromHtml("<u>" + getString(R.string.changelog) + "</u>"));
		txtChangeLog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MoneyManagerApplication.showChangeLog(getSherlockActivity(), true);
			}
		});*/
		// donate
		Button buttonDonate = (Button)view.findViewById(R.id.buttonDonateInApp);
		buttonDonate.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getSherlockActivity(), DonateActivity.class));
			}
		});
		
		return view;
	}
	
}
