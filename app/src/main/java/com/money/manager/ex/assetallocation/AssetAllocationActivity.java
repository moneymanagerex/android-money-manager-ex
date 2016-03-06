/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.money.manager.ex.R;
import com.money.manager.ex.assetallocation.events.AssetAllocationReloadRequested;
import com.money.manager.ex.assetallocation.events.AssetClassSelectedEvent;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyListActivity;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;
import com.shamanland.fonticon.FontIconDrawable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

public class AssetAllocationActivity
    extends BaseFragmentActivity
    implements DetailFragmentCallbacks, LoaderManager.LoaderCallbacks<AssetClass> {

    private static final int LOADER_ASSET_ALLOCATION = 1;
    private static final String KEY_ASSET_ALLOCATION = "assetAllocation";

    private AssetClass assetAllocation;

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

        if (savedInstanceState != null) {
            // use existing asset allocation
            this.assetAllocation = savedInstanceState.getParcelable(KEY_ASSET_ALLOCATION);
        } else {
            // Load asset allocation
            // Ref: http://developer.android.com/guide/components/loaders.html
            getSupportLoaderManager().initLoader(LOADER_ASSET_ALLOCATION, null, this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save allocation?
        outState.putParcelable(KEY_ASSET_ALLOCATION, Parcels.wrap(this.assetAllocation));
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // reload data
        //getSupportLoaderManager().restartLoader(LOADER_ASSET_ALLOCATION, null, this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    // Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_asset_allocation, menu);

        // customize icons

        // Currencies
        MenuItem currenciesMenu = menu.findItem(R.id.menu_currencies);
        if (currenciesMenu != null) {
            FontIconDrawable icon = FontIconDrawable.inflate(this, R.xml.ic_euro);
            icon.setTextColor(UIHelper.getColor(this, R.attr.toolbarItemColor));
            currenciesMenu.setIcon(icon);
        }

        // Overview
        MenuItem overview = menu.findItem(R.id.menu_asset_allocation_overview);
        FontIconDrawable icon = FontIconDrawable.inflate(this, R.xml.ic_report_page);
        icon.setTextColor(UIHelper.getColor(this, R.attr.toolbarItemColor));
        overview.setIcon(icon);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
            case android.R.id.home:
                setResultAndFinish();
                break;
            case R.id.menu_currencies:
                // open the Currencies activity.
                intent = new Intent(this, CurrencyListActivity.class);
                intent.setAction(Intent.ACTION_EDIT);
                startActivity(intent);
                break;
            case R.id.menu_asset_allocation_overview:
                // show the overview
                intent = new Intent(this, AssetAllocationOverviewActivity.class);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    // Asset Class display fragment

    @Override
    public AssetClass getAssetClass(int id) {
        AssetAllocationService service = new AssetAllocationService(this);
        AssetClass result = service.findChild(id, this.assetAllocation);
        return result;
    }

    // Loader

    @Override
    public Loader<AssetClass> onCreateLoader(int id, Bundle args) {
        return new AssetAllocationLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<AssetClass> loader, final AssetClass data) {
        this.assetAllocation = data;

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
        } else {
            // Otherwise, find the fragment and update the data.
            refreshDataInFragment(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<AssetClass> loader) {
        // Remove any references to the data.
    }

    // Events

    @Subscribe
    public void onEvent(AssetAllocationReloadRequested event) {
        // reload Asset Allocation
        getSupportLoaderManager().restartLoader(LOADER_ASSET_ALLOCATION, null, this);
    }

    @Subscribe
    public void onEvent(AssetClassSelectedEvent event) {
        AssetAllocationService service = new AssetAllocationService(this);
        AssetClass toShow = service.findChild(event.assetClassId, this.assetAllocation);

        ItemType selectedType = toShow.getType();
        switch (selectedType) {
            case Cash:
                // ignore
                break;
            default:
                showAssetClass(toShow);
                break;
        }
    }

    // Private

    private void setResultAndFinish() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void refreshDataInFragment(AssetClass assetAllocation) {
        // find the currently displayed fragment
        AssetAllocationFragment fragment = (AssetAllocationFragment) UIHelpers.getVisibleFragment(this);
        if (fragment == null) return;

        // find which allocation is being displayed currently.
        Integer id = fragment.getArguments().getInt(AssetAllocationFragment.PARAM_ASSET_CLASS_ID);

        AssetClass toShow;
        if (id != null) {
            // find it again in the reloaded data
            AssetAllocationService service = new AssetAllocationService(this);
            toShow = service.findChild(id, assetAllocation);
        } else {
            // assume root asset allocation.
            toShow = assetAllocation;
        }

        // reload data for the fragment
        fragment.showData(toShow);
    }

    private void showAssetClass(AssetClass assetClass) {
        // Round to decimals from the base currency.
        CurrencyService currencyService = new CurrencyService(this);
        int scale = currencyService.getBaseCurrency().getScale();
        int decimals = new NumericHelper(this).getNumberOfDecimals(scale);

        // show the fragment
        FragmentManager fm = getSupportFragmentManager();
        AssetAllocationFragment fragment = AssetAllocationFragment.create(assetClass.getId(), decimals);
        String tag = assetClass.getId() != null
            ? assetClass.getId().toString()
            : "root";

        FragmentTransaction transaction = fm.beginTransaction();

        if (fm.findFragmentById(R.id.content) == null) {
            tag = AssetAllocationFragment.class.getSimpleName();

//            transaction.add(R.id.content, fragment, tag)
            transaction.replace(R.id.content, fragment, tag)
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
}
