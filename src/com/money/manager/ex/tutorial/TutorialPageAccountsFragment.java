package com.money.manager.ex.tutorial;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.money.manager.ex.R;
import com.money.manager.ex.utils.RawFileUtils;

/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 */
public class TutorialPageAccountsFragment extends Fragment {

    public TutorialPageAccountsFragment() {
        // Required empty public constructor
    }

    public static TutorialPageAccountsFragment newInstance(){
        TutorialPageAccountsFragment fragment = new TutorialPageAccountsFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tutorial_page_accounts, container, false);

        // Load graphics.
        WebView webView = (WebView)view.findViewById(R.id.webViewAccounts);

        // Show web view only after the page is loaded to prevent showing white background.
        // http://stackoverflow.com/questions/9589365/splash-screen-while-loading-a-url-in-a-webview-in-android-app
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url){
                // show web view.
                getActivity().findViewById(R.id.webViewAccounts).setVisibility(View.VISIBLE);
            }
        });

        //webView.loadUrl("file:///android_asset/tutorial/accounts.html");

        // localization of the text.
        String content = RawFileUtils.getRawAsString(getActivity(), R.raw.tutorial_accounts);
        webView.loadData(content, "text/html", "UTF-8");

        return view;
    }

}
