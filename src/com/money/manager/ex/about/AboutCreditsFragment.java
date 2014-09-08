package com.money.manager.ex.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;

public class AboutCreditsFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup group,
                             Bundle saved) {
        return inflater.inflate(R.layout.about_content, group, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        WebView creditsWebView = (WebView) getActivity().findViewById(R.id.about_thirdsparty_credits);

        creditsWebView.loadData(Core.getRawAsString(getActivity(), R.raw.credits_thirdparty), "text/html", "UTF-8");
    }
}
