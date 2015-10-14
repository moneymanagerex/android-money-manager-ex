package com.money.manager.ex.assetallocation;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseListFragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class AssetAllocationPickerFragment
    extends BaseListFragment {

    public AssetAllocationPickerFragment() {
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_asset_allocation_picker, container, false);
//    }

    @Override
    public String getSubTitle() {
        return null;
    }
}
