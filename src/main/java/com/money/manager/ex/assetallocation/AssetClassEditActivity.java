package com.money.manager.ex.assetallocation;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.money.manager.ex.Constants;
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

        if (savedInstanceState != null) {
            // todo: restore instance state
        } else {
            loadIntent();
        }
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
        // todo: validate

        boolean result = false;

        AssetClassEditFragment fragment = getFragment();
        AssetClass assetClass = fragment.assetClass;

        AssetClassRepository repo = new AssetClassRepository(this);
        switch (getIntent().getAction()) {
            case Intent.ACTION_INSERT:
                result = repo.insert(assetClass);
                break;
            case Intent.ACTION_EDIT:
                result = repo.update(assetClass);
                break;
            default:
                result = false;
        }
        return result;
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

    private void loadIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        int id = intent.getIntExtra(Intent.EXTRA_UID, Constants.NOT_SET);
        // load class
        AssetClassRepository repo = new AssetClassRepository(this);
        AssetClass assetClass = repo.load(id);
        // todo: show error message and return (close edit activity)
        if (assetClass == null) return;

        AssetClassEditFragment fragment = getFragment();
        fragment.assetClass = assetClass;
    }
}
