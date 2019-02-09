/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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
import android.webkit.WebView;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.utils.NetworkUtils;

import androidx.fragment.app.Fragment;

/**
 * Use the {@link WebChangelogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WebChangelogFragment
    extends Fragment {

    public WebChangelogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WebChangelogFragment.
     */
    public static WebChangelogFragment newInstance() {
        WebChangelogFragment fragment = new WebChangelogFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_web_changelog, container, false);

        loadChangelog(view);

        return view;
    }

    private void loadChangelog(View view) {
        // check if there is network access
        NetworkUtils utils = new NetworkUtils(getActivity());
        if (!utils.isOnline()) {
            new Core(getActivity()).alert(R.string.no_network);
            return;
        }

        // Set up the URL.

//        String url = "https://github.com/moneymanagerex/android-money-manager-ex/issues?q=milestone%3A";
//        Core core = new Core(getActivity());
//        String version = core.getFullAppVersion(); // "2016.01.21.763";
//        url += version;

        // Show all the versions
        String url = "https://github.com/moneymanagerex/android-money-manager-ex/milestones?direction=desc&sort=due_date&state=closed";
        
        WebView webView = (WebView) view.findViewById(R.id.webView);
        if (webView != null) {
            webView.loadUrl(url);
        }
    }
}
