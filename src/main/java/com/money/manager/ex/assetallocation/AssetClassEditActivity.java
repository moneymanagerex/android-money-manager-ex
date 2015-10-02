package com.money.manager.ex.assetallocation;

import android.os.Bundle;
import android.app.Activity;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;

public class AssetClassEditActivity
    extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_class_edit);

        setToolbarStandardAction(getToolbar());

    }

}
