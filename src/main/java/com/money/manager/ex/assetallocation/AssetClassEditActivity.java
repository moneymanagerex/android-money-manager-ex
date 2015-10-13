/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.money.manager.ex.assetallocation;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.datalayer.AssetClassRepository;
import com.money.manager.ex.datalayer.AssetClassStockRepository;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.domainmodel.AssetClassStock;
import com.money.manager.ex.servicelayer.AssetAllocationService;

public class AssetClassEditActivity
    extends BaseFragmentActivity {

    public static final int REQUEST_STOCK_ID = 1;
    public static final String INTENT_RESULT_STOCK_ID = "AssetClassEditActivity:StockId";
    public static final String KEY_ASSET_CLASS_ID = "AssetClassEditActivity:AssetClassId";

    private Integer assetClassId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_class_edit);

        setToolbarStandardAction(getToolbar());

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case REQUEST_STOCK_ID:
                Integer stockId = data.getIntExtra(INTENT_RESULT_STOCK_ID, Constants.NOT_SET);
                assignStockToAssetClass(stockId);
                break;
        }
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_ASSET_CLASS_ID, this.assetClassId);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        this.assetClassId = savedInstanceState.getInt(KEY_ASSET_CLASS_ID);
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
        this.assetClassId = id;

        // load class
        AssetClassRepository repo = new AssetClassRepository(this);
        AssetClass assetClass = repo.load(id);
        // todo: show error message and return (close edit activity)
        if (assetClass == null) return;

        AssetClassEditFragment fragment = getFragment();
        fragment.assetClass = assetClass;
    }

    private void assignStockToAssetClass(int stockId) {
        AssetAllocationService service = new AssetAllocationService(this);
        service.assignStockToAssetClass(stockId, this.assetClassId);
        // todo: refresh data?
    }
}
