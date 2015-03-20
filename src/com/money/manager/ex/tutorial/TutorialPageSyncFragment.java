package com.money.manager.ex.tutorial;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.money.manager.ex.R;
import com.money.manager.ex.preferences.PreferencesConstant;

/**
 * Page displaying Dropbox functionality in tutorial.
 * Also, being the last page, it contains the OK button that closes the Tutorial.
 * A simple {@link Fragment} subclass.
 */
public class TutorialPageSyncFragment extends Fragment {

    public TutorialPageSyncFragment() {

    }

    public static TutorialPageSyncFragment newInstance(){
        TutorialPageSyncFragment fragment = new TutorialPageSyncFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tutorial_page_sync, container, false);

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

                // Mark tutorial as seen, in the settings.
                Context context = parent.getApplicationContext();
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                String key = context.getString(PreferencesConstant.PREF_SHOW_TUTORIAL);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(key, false);
                editor.commit();

                // Close Tutorial.
                parent.finish();
            }
        });

//        this.loadSvg(view);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void loadSvg(View view){
        // test: show vector image.
        // imageView
        // Create a new ImageView
//        ImageView imageView = new ImageView(this);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        // Set the background color to white
        imageView.setBackgroundColor(Color.WHITE);
        // Parse the SVG file from the resource
//        SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.sync_screen);
        // Get a drawable from the parsed SVG and set it as the drawable for the ImageView
//        imageView.setImageDrawable(svg.createPictureDrawable());
        // Set the ImageView as the content view for the Activity
//        setContentView(imageView);

    }
}
