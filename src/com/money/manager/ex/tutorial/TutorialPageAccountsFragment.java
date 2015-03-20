package com.money.manager.ex.tutorial;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import com.money.manager.ex.R;

/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 */
public class TutorialPageAccountsFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

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
        return inflater.inflate(R.layout.fragment_tutorial_page_accounts, container, false);
    }


}
