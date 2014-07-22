package com.money.manager.ex.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.money.manager.ex.R;

public class AboutChangelogFragment extends SherlockFragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup group,
                             Bundle saved) {

        return inflater.inflate(R.layout.changelog_layout, group, false);
    }

}
