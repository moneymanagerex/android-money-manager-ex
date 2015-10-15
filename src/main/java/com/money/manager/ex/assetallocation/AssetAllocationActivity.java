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

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.datalayer.AssetClassRepository;
import com.money.manager.ex.datalayer.AssetClassStockRepository;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;

public class AssetAllocationActivity
    extends BaseFragmentActivity
    implements DetailFragmentCallbacks, LoaderManager.LoaderCallbacks<AssetClass> {

    private static final int LOADER_ASSET_ALLOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_toolbar_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            setToolbarStandardAction(toolbar);
            // change home icon to 'back'.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Ref: http://developer.android.com/guide/components/loaders.html
        getSupportLoaderManager().initLoader(LOADER_ASSET_ALLOCATION, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResultAndFinish();
                return true; // consumed here
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

//        unregisterObserver();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        registerObserver();
    }

    @Override
    protected void onDestroy() {
//        unregisterObserver();
        super.onDestroy();
    }

    @Override
    public void assetClassSelected(int assetClassId) {
        // Handling:
        // - asset group (has children), load children
        // - asset class (no children), show stocks
        // - stock (no children, no stocks) do nothing, or show context menu?

        AssetAllocationService service = new AssetAllocationService(this);
        // todo: AssetClass toShow = service.findChild(assetClassId, this.assetAllocation);

        // todo: showAssetClass(toShow);
    }

    @Override
    public void assetClassDeleted(int assetClassId) {
        // todo: redo this to use loaders and refresh the data.
        // reloadData();

//        // reload data.
//        loadAssetAllocation();
//        // find the currently displayed fragment
//        AssetAllocationFragment fragment = (AssetAllocationFragment) UIHelpers.getVisibleFragment(this);
//        // find which allocation is being displayed currently.
//        int id = fragment.assetClass.getId();
//        // find it again in the reloaded data
//        AssetAllocationService service = new AssetAllocationService(this);
//        AssetClass toShow = service.findChild(id, this.assetAllocation);
//        // reload data for the fragment
//        fragment.showData(toShow);
    }

    // Loader

    @Override
    public Loader<AssetClass> onCreateLoader(int id, Bundle args) {
        return new AssetAllocationLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<AssetClass> loader, final AssetClass data) {
//        registerObserver(loader);

        // Create handler to perform showing of fragment(s).
        Handler h = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            public void run() {
                showAssetClass(data);
            }
        };

        // show the data
        AssetAllocationFragment fragment = (AssetAllocationFragment) UIHelpers.getVisibleFragment(this);
        // If there are no other fragments, create the initial view.
        if (fragment == null) {
            h.post(runnable);
        }
        // todo: Otherwise, find the fragment and update the data.
        // showAssetClass();
    }

    @Override
    public void onLoaderReset(Loader<AssetClass> loader) {
        // Remove any references to the data.
//        Log.d("data", "loader reset");
    }

//    @Override
//    public void onLoadFinished(Loader<Object> loader, Object data) {
//        Log.d("data", "finished");
//        loadAssetAllocation();
//    }

//    @Override
//    public void onLoaderReset(Loader<Object> loader) {
//        Log.d("data", "reset");
//    }

    // Private

    private void reloadData() {
        getSupportLoaderManager().restartLoader(LOADER_ASSET_ALLOCATION, null, this);
    }

    private void setResultAndFinish() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void showAssetClass(AssetClass assetClass) {
        // show the fragment
        FragmentManager fm = getSupportFragmentManager();
        AssetAllocationFragment fragment = AssetAllocationFragment.create(assetClass);
        String tag = assetClass.getId() != null
            ? assetClass.getId().toString()
            : "root";

        FragmentTransaction transaction = fm.beginTransaction();

        if (fm.findFragmentById(R.id.content) == null) {
            tag = AssetAllocationFragment.class.getSimpleName();

            transaction.add(R.id.content, fragment, tag)
                .commit();
            // the initial fragment does not go onto back stack.
        } else {
            // Replace existing fragment. Always use replace instead of add?
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_right, R.anim.slide_out_left);
            transaction.replace(R.id.content, fragment, tag);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

//    private void registerObserver(Loader<AssetClass> loaderObject) {
//        AssetAllocationLoader loader = (AssetAllocationLoader) loaderObject;
//        AssetAllocationContentObserver observer = loader.getObserver();
//
//        Uri assetClassUri = new AssetClassRepository(this).getUri();
//        Uri linkUri = new AssetClassStockRepository(this).getUri();
//
//        getContentResolver().registerContentObserver(assetClassUri, true, observer);
//        getContentResolver().registerContentObserver(linkUri, true, observer);
//
//    }

//    private void unregisterObserver() {
//        AssetAllocationLoader loader = (AssetAllocationLoader) getLoader();
//        AssetAllocationContentObserver observer = loader.getObserver();
//
//        getContentResolver().unregisterContentObserver(observer);
//    }

//    private Loader<AssetClass> getLoader() {
//        Object supportLoader = getSupportLoaderManager().getLoader(LOADER_ASSET_ALLOCATION);
//        AssetAllocationLoader loader = (AssetAllocationLoader) supportLoader;
//
//        return loader;
//    }
}
