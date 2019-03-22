/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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
import android.view.Menu;
import android.view.MenuItem;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.datalayer.AssetClassRepository;
import com.money.manager.ex.domainmodel.AssetClass;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import timber.log.Timber;

/**
 * Editor for a single Asset Class.
 */
public class AssetClassEditActivity
    extends MmxBaseFragmentActivity {

    public static final String KEY_ASSET_CLASS_ID = "AssetClassEditActivity:AssetClassId";
    public static final String KEY_PARENT_ID = "AssetClassEditActivity:parentId";

    private Integer assetClassId;
    private String mAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_class_edit);

//        showStandardToolbarActions();
        setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else {
            loadIntent();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuHelper menuHelper = new MenuHelper(this, menu);
        menuHelper.addSaveToolbarIcon();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // cancel clicked. Prompt to confirm?
                Timber.d("going back");
                break;
            case MenuHelper.save:
                return onActionDoneClick();
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (this.assetClassId != null) {
            outState.putInt(KEY_ASSET_CLASS_ID, this.assetClassId);
        }
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        this.assetClassId = savedInstanceState.getInt(KEY_ASSET_CLASS_ID);
    }

    private boolean save() {
        // todo: validate

        boolean result;

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
        FragmentManager fm = getSupportFragmentManager();

        Fragment fragment = fm.findFragmentById(R.id.fragment);
        if (fragment == null) return null;

        return (AssetClassEditFragment) fragment;
    }

    private void loadIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        // Insert or Edit?
        mAction = intent.getAction();
        if (mAction == null) return;

        AssetClass assetClass = null;

        switch (mAction) {
            case Intent.ACTION_INSERT:
                assetClass = AssetClass.create("");
                int parentId = intent.getIntExtra(KEY_PARENT_ID, Constants.NOT_SET);
                assetClass.setParentId(parentId);
                break;

            case Intent.ACTION_EDIT:
                int id = intent.getIntExtra(Intent.EXTRA_UID, Constants.NOT_SET);
                this.assetClassId = id;

                // load class
                AssetClassRepository repo = new AssetClassRepository(this);
                assetClass = repo.load(id);
                if (assetClass == null) {
                    new UIHelper(this).showToast("No asset class found in the database!");
                    // todo: show error message and return (close edit activity)
                    return;
                }
                break;
        }

        AssetClassEditFragment fragment = getFragment();
        fragment.assetClass = assetClass;
    }
}
