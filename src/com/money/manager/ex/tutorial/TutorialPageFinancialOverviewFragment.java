package com.money.manager.ex.tutorial;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.money.manager.ex.R;

import java.io.IOException;

/**
 * Here we try SVG-Android 2.
 * A simple {@link Fragment} subclass.
 * Use the {@link TutorialPageFinancialOverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TutorialPageFinancialOverviewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TutorialPageFinancialOverviewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TutorialPageFinancialOverviewFragment newInstance() {
        TutorialPageFinancialOverviewFragment fragment = new TutorialPageFinancialOverviewFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
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
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tutorial_page_financial_overview, container, false);

        // android svg2
//        showGraphics(view);

        return view;
    }

//    private void showGraphics(View view){
//        Context context = view.getContext();
//
//        // Create a new ImageView
//        ImageView imageView = new ImageView(context);
//        // Set the background color to white
//        imageView.setBackgroundColor(Color.WHITE);
//        // Parse the SVG file from the resource
//        //SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.tutorial_sync);
//        SVG svg = null;
//        try {
//            svg = SVGParser.getSVGFromAsset(context.getAssets(), "tutorial_sync.svg");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // Get a drawable from the parsed SVG and set it as the drawable for the ImageView
//        imageView.setImageDrawable(svg.createPictureDrawable());
//
//        // Set the ImageView as the content view for the Activity
//        //setContentView(imageView);
//        getActivity().setContentView(imageView);
//    }
}
