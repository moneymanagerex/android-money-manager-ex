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

package com.money.manager.ex;

import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.utils.MmxFileUtils;

import timber.log.Timber;

public class HelpActivity
    extends MmxBaseFragmentActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.webview_activity);

        // adjust actionbar
        setDisplayHomeAsUpEnabled(true);

        mWebView = (WebView) findViewById(R.id.webViewContent);
        // enable javascript
        mWebView.getSettings().setJavaScriptEnabled(true);

        if (getIntent() == null) return;

        try {
            if ("android.resource".equals(getIntent().getData().getScheme())) {
                int rawId = Integer.parseInt(getIntent().getData().getPathSegments()
                        .get(getIntent().getData().getPathSegments().size() - 1));

                WebSettings settings = mWebView.getSettings();
                settings.setDefaultTextEncodingName("utf-8");
                mWebView.loadData(MmxFileUtils.getRawAsString(getApplicationContext(), rawId),
                        "text/html; charset=utf-8", null);
            } else {
                mWebView.loadUrl(getIntent().getData().toString());
            }
        } catch (Exception e) {
            Timber.e(e, "setting content of web view");
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
