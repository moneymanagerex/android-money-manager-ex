package com.money.manager.ex.tutorial;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.money.manager.ex.R;

/**
 * Page displaying Dropbox functionality in tutorial.
 * Also, being the last page, it contains the OK button that closes the Tutorial.
 * A simple {@link Fragment} subclass.
 */
public class TutorialPageDropboxFragment extends Fragment {

    public TutorialPageDropboxFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tutorial_page_dropbox, container, false);

        // Handle OK button.

        Button okButton = (Button)view.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles OK button click. Closes the tutorial activity.
             * @param view
             */
            @Override
            public void onClick(View view) {
                Activity parent = getActivity();
                parent.finish();
            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

}
