package com.money.manager.ex.assetallocation;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.datalayer.AssetClassRepository;
import com.money.manager.ex.domainmodel.AssetClass;

public class AssetClassEditActivity
    extends BaseFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_class_edit);

        setToolbarStandardAction(getToolbar());
    }

    @Override
    public boolean onActionCancelClick() {
        setResult(Activity.RESULT_CANCELED);
        finish();

        return true;
    }

    @Override
    public boolean onActionDoneClick() {
        if (save()) {
            // set result ok and finish activity
            setResult(RESULT_OK);
            finish();
            return true;
        } else {
            return false;
        }
    }

    private boolean save() {
        AssetClassEditFragment fragment = getFragment();
        AssetClass assetClass = fragment.assetClass;

        AssetClassRepository repo = new AssetClassRepository(this);
        return repo.insert(assetClass);
    }

    private AssetClassEditFragment getFragment() {
//        String tag = AssetClassEditFragment.class.getSimpleName();

        //        AssetClassEditFragment fragment = new AssetClassEditFragment();
        FragmentManager fm = getSupportFragmentManager();
//        if (fm.findFragmentById(R.id.content) == null) {
//            fm.beginTransaction().add(R.id.content, fragment, FRAGMENTTAG).commit();
//        }

//        Fragment fragment = fm.findFragmentByTag(tag);
        Fragment fragment = fm.findFragmentById(R.id.fragment);
        if (fragment != null) {
            return (AssetClassEditFragment) fragment;
        } else {
            return null;
        }

    }
}
