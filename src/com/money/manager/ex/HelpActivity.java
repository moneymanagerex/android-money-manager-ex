package com.money.manager.ex;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;

import com.money.manager.ex.fragment.BaseFragmentActivity;

public class HelpActivity extends BaseFragmentActivity {
	private static final String LOGCAT = HelpActivity.class.getSimpleName();
	public static final String INTENT_ID_RAW = "HelpActiviy:IdRaw";
	private WebView mWebView;
	
	@Override
	protected void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(R.layout.webview_activity);
		
		// adjust actionbar
		setDisplayHomeAsUpEnabled(true);
		
		mWebView = (WebView)findViewById(R.id.webViewContent);
		// enable javascript
		mWebView.getSettings().setJavaScriptEnabled(true);
		// get intent
		if (getIntent() != null) {
			try {
				if ("android.resource".equals(getIntent().getData().getScheme())) {
					int rawId = Integer.parseInt(getIntent().getData().getPathSegments().get(getIntent().getData().getPathSegments().size()-1));
					mWebView.loadData(MoneyManagerApplication.getRawAsString(getApplicationContext(), rawId), "text/html", "UTF-8");
				} else {
					mWebView.loadUrl(getIntent().getData().toString());
				}
			} catch (Exception e) {
				Log.e(LOGCAT, e.getMessage());
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
				mWebView.goBack();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
