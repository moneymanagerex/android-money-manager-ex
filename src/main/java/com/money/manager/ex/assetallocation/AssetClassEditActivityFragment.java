package com.money.manager.ex.assetallocation;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class AssetClassEditActivityFragment extends Fragment {

    public AssetClassEditActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_asset_class_edit, container, false);
    }
}
