/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
 */

package com.money.manager.ex.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.money.manager.ex.R;
import com.money.manager.ex.utils.RawFileUtils;

public class AboutCreditsFragment extends Fragment {
    private static Fragment mInstance;

    public static Fragment newInstance(int page) {
        if (mInstance == null) {
            mInstance = new AboutCreditsFragment();
        }
        return mInstance;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup group,
                             Bundle saved) {
        return inflater.inflate(R.layout.about_content, group, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        WebView webView = (WebView) getActivity().findViewById(R.id.about_thirdsparty_credits);

//        webView.loadData(RawFileUtils.getRawAsString(getActivity(), R.raw.credits_thirdparty), "text/html", "UTF-8");

        // Display Unicode characters.
        WebSettings settings = webView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");

        webView.loadData(RawFileUtils.getRawAsString(getActivity().getApplicationContext(), R.raw.credits_thirdparty),
                "text/html; charset=utf-8", null);
    }
}
