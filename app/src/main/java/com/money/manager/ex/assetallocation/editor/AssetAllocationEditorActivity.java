/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.assetallocation.editor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.money.manager.ex.R;
import com.money.manager.ex.assetallocation.AssetAllocationContentsFragment;
import com.money.manager.ex.assetallocation.AssetAllocationLoader;
import com.money.manager.ex.assetallocation.ItemType;
import com.money.manager.ex.assetallocation.UIHelpers;
import com.money.manager.ex.assetallocation.events.AssetAllocationReloadRequestedEvent;
import com.money.manager.ex.assetallocation.events.AssetClassSelectedEvent;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.currency.list.CurrencyListActivity;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

/**
 * Asset Allocation editor. Displays one level of asset classes and allows adding and removing
 * asset classes in the allocation.
 */
public class AssetAllocationEditorActivity
    extends MmxBaseFragmentActivity {

    public static final String KEY_ASSET_ALLOCATION = "assetAllocation";

    private static final int LOADER_ASSET_ALLOCATION = 1;

    private AssetClass assetAllocation;
    LoaderManager.LoaderCallbacks<AssetClass> mLoaderCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_toolbar_activity);

        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            showStandardToolbarActions();
            // change home icon to 'back'.
            setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null) {
            // use existing asset allocation
            this.assetAllocation = Parcels.unwrap(savedInstanceState.getParcelable(KEY_ASSET_ALLOCATION));
        } else {
            // Load asset allocation
            // Ref: http://developer.android.com/guide/components/loaders.html
            mLoaderCallbacks = setUpLoaderCallbacks();
            getSupportLoaderManager().initLoader(LOADER_ASSET_ALLOCATION, null, mLoaderCallbacks);
        }

//        Answers.getInstance().logCustom(new CustomEvent(AnswersEvents.AssetAllocation.name()));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_ASSET_ALLOCATION, Parcels.wrap(this.assetAllocation));
    }

    // Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_asset_allocation_editor, menu);

        // customize icons

        UIHelper ui = new UIHelper(this);

        // Currencies
        MenuItem currenciesMenu = menu.findItem(R.id.menu_currencies);
        if (currenciesMenu != null) {
            IconicsDrawable icon = ui.getIcon(GoogleMaterial.Icon.gmd_euro_symbol);
            currenciesMenu.setIcon(icon);
        }

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

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    // Events

    @Subscribe
    public void onEvent(AssetAllocationReloadRequestedEvent event) {
        // reload Asset Allocation
        getSupportLoaderManager().restartLoader(LOADER_ASSET_ALLOCATION, null, mLoaderCallbacks);
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
        setResult(AppCompatActivity.RESULT_OK);
        finish();
    }

    private void refreshCurrentFragment() {
        AssetAllocationContentsFragment fragment = (AssetAllocationContentsFragment) UIHelpers.getVisibleFragment(this);
        if (fragment == null) return;

        fragment.showData();
    }

    private void refreshDataInFragments(AssetClass assetAllocation) {
        // iterate through all the fragments and update the asset allocation reference.
        List<Fragment> allFragments = getSupportFragmentManager().getFragments();
        if (allFragments == null) return;

        for (Fragment fragment : allFragments) {
            AssetAllocationContentsFragment f = (AssetAllocationContentsFragment)fragment;
            if (f == null) continue;

            Bundle args = f.getArguments();
            if (args != null) {
                args.putParcelable(KEY_ASSET_ALLOCATION, Parcels.wrap(assetAllocation));
            }
        }

        refreshCurrentFragment();
    }

    private LoaderManager.LoaderCallbacks<AssetClass> setUpLoaderCallbacks() {
        return new LoaderManager.LoaderCallbacks<AssetClass>() {
            @Override
            public Loader<AssetClass> onCreateLoader(int id, Bundle args) {
                return new AssetAllocationLoader(AssetAllocationEditorActivity.this);
            }

            @Override
            public void onLoadFinished(Loader<AssetClass> loader, final AssetClass data) {
                AssetAllocationEditorActivity.this.assetAllocation = data;

                // Create handler to perform showing of fragment(s).
                Handler h = new Handler(Looper.getMainLooper());
                Runnable runnable = new Runnable() {
                    public void run() {
                        showAssetClass(data);
                    }
                };

                // show the data
                AssetAllocationContentsFragment fragment = (AssetAllocationContentsFragment) UIHelpers.getVisibleFragment(AssetAllocationEditorActivity.this);
                // If there are no other fragments, create the initial view.
                if (fragment == null) {
                    h.post(runnable);
                } else {
                    // Otherwise, find the fragment and update the data.
//                    refreshDataInFragment(data);
                    refreshDataInFragments(data);
                }
            }

            @Override
            public void onLoaderReset(Loader<AssetClass> loader) {
                // adapter swap cursor?
            }
        };
    }

    private void showAssetClass(AssetClass assetClass) {
        if (assetClass == null) return;

        // Round to decimals from the base currency.
        CurrencyService currencyService = new CurrencyService(this);
        int scale = currencyService.getBaseCurrency().getScale();
        int decimals = new NumericHelper(this).getNumberOfDecimals(scale);

        // show the fragment
        FragmentManager fm = getSupportFragmentManager();
        AssetAllocationContentsFragment fragment = AssetAllocationContentsFragment.create(assetClass.getId(), decimals, this.assetAllocation);

        String tag = assetClass.getId() != null
            ? assetClass.getId().toString()
            : "root";

        FragmentTransaction transaction = fm.beginTransaction();

        if (fm.findFragmentById(R.id.content) == null) {
            tag = AssetAllocationContentsFragment.class.getSimpleName();

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
