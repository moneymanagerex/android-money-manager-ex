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
 * A simple {@link Fragment} subclass.
 * Use the {@link TutorialPageTransactionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TutorialPageTransactionsFragment extends Fragment {
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TutorialPageTransactionsFragment.
     */
    public static TutorialPageTransactionsFragment newInstance() {
        TutorialPageTransactionsFragment fragment = new TutorialPageTransactionsFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public TutorialPageTransactionsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_tutorial_page_transactions, container, false);

        // load graphics
        WebView webView = (WebView)view.findViewById(R.id.webViewTransactions);
//        webView.loadUrl("file:///android_asset/tutorial/transactions.html");

        // localization of the text.
        String content = RawFileUtils.getRawAsString(getActivity(), R.raw.tutorial_transactions);
        webView.loadData(content, "text/html", "UTF-8");

        return view;
    }

}
