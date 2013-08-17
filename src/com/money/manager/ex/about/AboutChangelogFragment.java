package com.money.manager.ex.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockFragment;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;

public class AboutChangelogFragment extends SherlockFragment {
	
	public View onCreateView(LayoutInflater inflater, ViewGroup group,
			Bundle saved) {
		return inflater.inflate(R.layout.changelog, group, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		WebView creditsWebView = (WebView) getSherlockActivity().findViewById(R.id.changelogcontent);
		
		creditsWebView.loadData(MoneyManagerApplication.getRawAsString(getActivity(), R.raw.changelog), "text/html", "UTF-8");
	}	
}
