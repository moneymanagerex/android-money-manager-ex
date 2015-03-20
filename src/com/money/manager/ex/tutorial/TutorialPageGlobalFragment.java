package com.money.manager.ex.tutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;

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
        return inflater.inflate(R.layout.fragment_tutorial_page_global, container, false);
    }

}
