/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.money.manager.ex.R;
import com.money.manager.ex.core.HttpMethods;
import com.money.manager.ex.core.MyWebChromeClient;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Used for PayPal online donations handling.
 */
public class WebViewActivity
    extends MmxBaseFragmentActivity {

    public static final String URL = "URL";
    /**
     * GET, POST
     */
    public static final String METHOD = "METHOD";
    public static final String POST_VALUES = "POST_VALUES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_web_view);
//        getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        // show progress bar

        getProgressBar().setProgress(0);

        handleIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically e clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateProgress(int value) {
        getProgressBar().setProgress(value);
    }

    private ProgressBar getProgressBar() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        return progressBar;
    }

    private WebView getWebView() {
        final WebView webView = (WebView) findViewById(R.id.webView);
        if (webView == null) return null;

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setBuiltInZoomControls(true);
//        webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);

        // appearance
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_INSET);

        // prevent opening the browser app.
        webView.setWebViewClient(new WebViewClient(){
            @Override
            //show the web page in webview but not in web browser
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl (url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                ViewGroup progressDisplay = (ViewGroup) findViewById(R.id.progressDisplay);
                progressDisplay.setVisibility(View.GONE);

//        findViewById(R.id.progressBar1).setVisibility(View.GONE);
//        findViewById(R.id.activity_main_webview).setVisibility(View.VISIBLE);

                webView.setVisibility(View.VISIBLE);
            }
        });

        MyWebChromeClient chromeWebClient = new MyWebChromeClient(this);
        webView.setWebChromeClient(chromeWebClient);

        return webView;
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        HttpMethods method = (HttpMethods) intent.getSerializableExtra(METHOD);
        if (method == null) return;

        if (method.equals(HttpMethods.POST)) {
            post(intent);
        }
    }

    private void post(Intent intent) {
        if (intent == null) return;

        WebView webView = getWebView();
        if (webView == null) return;

        String url = intent.getStringExtra(URL);
        HashMap<String, String> postParams = (HashMap<String, String>) intent.getSerializableExtra(POST_VALUES);
        if (postParams != null) {
            String postDataString = "";
            for (String key : postParams.keySet()) {
                if (!TextUtils.isEmpty(postDataString)) {
                    postDataString += "&";
                }
                postDataString += key + "=" + postParams.get(key);
            }

            // send a POST request
            byte[] postData;
            try {
                postData = postDataString.getBytes("BASE64");
            } catch (Exception e) {
                Timber.w(e);
                postData = postDataString.getBytes();
            }

            webView.postUrl(url, postData);
        }

    }

}
