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
 */
public class TutorialPageGlobalFragment extends Fragment {

    public static TutorialPageGlobalFragment newInstance(){
        TutorialPageGlobalFragment fragment = new TutorialPageGlobalFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    public TutorialPageGlobalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tutorial_page_global, container, false);

        // load graphics
        this.loadGraphics(view);

        return view;
    }

    private void loadGraphics(View view){
        WebView webView = (WebView)view.findViewById(R.id.webViewGlobal);
//        webView.loadUrl("file:///android_asset/tutorial/global.html");

        // localization of the text.
        String content = RawFileUtils.getRawAsString(getActivity(), R.raw.tutorial_global);
        // paragraphs
        content = content.replace("R.string.global", getString(R.string.global));
        content = content.replace("R.string.tutorial_global_1", getString(R.string.tutorial_global_1));
        // load page.
        webView.loadData(content, "text/html", "UTF-8");
    }

}
