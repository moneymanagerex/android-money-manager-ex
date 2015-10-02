package com.money.manager.ex.assetallocation;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.FragmentManager;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;

public class AssetClassEditActivity
    extends BaseFragmentActivity {

//    private static final String FRAGMENTTAG = AssetClassEditFragment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_class_edit);

        setToolbarStandardAction(getToolbar());

//        AssetClassEditFragment fragment = new AssetClassEditFragment();
//        FragmentManager fm = getSupportFragmentManager();
//        if (fm.findFragmentById(R.id.content) == null) {
//            fm.beginTransaction().add(R.id.content, fragment, FRAGMENTTAG).commit();
//        }

    }

}
