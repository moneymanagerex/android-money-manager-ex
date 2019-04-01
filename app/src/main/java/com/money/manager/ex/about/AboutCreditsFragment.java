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

package com.money.manager.ex.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.R;
import com.money.manager.ex.utils.MmxFileUtils;

import androidx.fragment.app.Fragment;
import timber.log.Timber;

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
        webView.setBackgroundColor(0);

//        webView.loadData(MmxFileUtils.getRawAsString(getActivity(), R.raw.credits_thirdparty), "text/html", "UTF-8");

        // Display Unicode characters.
        WebSettings settings = webView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");

        String htmlString = MmxFileUtils.getRawAsString(getActivity().getApplicationContext(), R.raw.credits_thirdparty);
        // Inject theme colors into html css
        // https://stackoverflow.com/questions/5026995/android-get-color-as-string-value/46831158#46831158
        UIHelper ui = new UIHelper(getActivity());
        String color1 = String.format("#%06X", 0xFFFFFF & ui.getPrimaryTextColor());
        String color2 = String.format("#%06X", 0xFFFFFF & ui.getSecondaryTextColor());
        // just use parent (SecondaryTextColor) for links in dark theme, otherwise default (blue)
        String color3 = ui.isUsingDarkTheme() ? "inherit" : "";
        try {
            htmlString = String.format(htmlString, color1, color2, color3);
        } catch (Exception e) {
            Timber.e(e, "setting css theme colors in credits html");
        }
        webView.loadData(htmlString, "text/html; charset=utf-8", null);
    }
}
