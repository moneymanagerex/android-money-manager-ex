package com.money.manager.ex.tutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.money.manager.ex.R;
import com.money.manager.ex.utils.RawFileUtils;

/**
 * Here we try SVG-Android 2.
 * A simple {@link Fragment} subclass.
 * Use the {@link TutorialPageFinancialOverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TutorialPageFinancialOverviewFragment extends Fragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TutorialPageFinancialOverviewFragment.
     */
    public static TutorialPageFinancialOverviewFragment newInstance() {
        TutorialPageFinancialOverviewFragment fragment = new TutorialPageFinancialOverviewFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public TutorialPageFinancialOverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tutorial_page_financial_overview, container, false);

        WebView webView = (WebView)view.findViewById(R.id.webViewOverview);

        // localization of the text.
        String content = RawFileUtils.getRawAsString(getActivity(), R.raw.tutorial_overview);
        // paragraphs
        content = content.replace("R.string.financial_overview", getString(R.string.financial_overview));
        content = content.replace("R.string.tutorial_overview_1", getString(R.string.tutorial_overview_1));
        // load page.
        webView.loadDataWithBaseURL("file:///android_asset/tutorial_images/", content, "text/html", "UTF-8", null);

        return view;
    }

}
