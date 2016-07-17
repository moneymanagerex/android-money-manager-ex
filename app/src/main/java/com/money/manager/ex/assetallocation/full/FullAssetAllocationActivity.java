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

package com.money.manager.ex.assetallocation.full;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ObservableScrollView;
import com.money.manager.ex.R;
import com.money.manager.ex.assetallocation.AssetClassEditActivity;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;

import java.util.ArrayList;
import java.util.List;

public class FullAssetAllocationActivity
    extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Theme
        Core core = new Core(this);
        this.setTheme(core.getThemeId());

        setContentView(R.layout.activity_full_asset_allocation);

        // Toolbar
        setUpToolbar();

        // Floating action button.
        setUpFloatingButton();

        // List

        AssetAllocationService service = new AssetAllocationService(this);
        AssetClass assetAllocation = service.loadAssetAllocation();

        List<AssetClassViewModel> model = createViewModel(assetAllocation);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        FullAssetAllocationAdapter adapter = new FullAssetAllocationAdapter(model);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // home click is handled in the manifest by setting up the parent activity.
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // todo reload asset allocation?
    }

    private List<AssetClassViewModel> createViewModel(AssetClass assetAllocation) {
        if (assetAllocation == null) {
            // get asset allocation
            AssetAllocationService service = new AssetAllocationService(this);
            assetAllocation = service.loadAssetAllocation();
        }

        // linearize for display
        List<AssetClassViewModel> modelList = new ArrayList<>();
        for (AssetClass child : assetAllocation.getChildren()) {
            addModelToList(child, modelList, 0);
        }

        return modelList;
    }

    private void addModelToList(AssetClass assetClass, List<AssetClassViewModel> modelList, int level) {
        // add the asset class first.
        AssetClassViewModel model = new AssetClassViewModel(assetClass, level);
        modelList.add(model);

        List<AssetClass> children = assetClass.getChildren();
        if (children.size() == 0) return;

        // then add the children.
        for (AssetClass child : children) {
            addModelToList(child, modelList, level + 1);
        }
    }

    private void setUpFloatingButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab == null) return;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create new asset allocation.
                Intent intent = new Intent(FullAssetAllocationActivity.this, AssetClassEditActivity.class);
                intent.setAction(Intent.ACTION_INSERT);
//                intent.putExtra(AssetClassEditActivity.KEY_PARENT_ID, this.getAssetClassId());
                startActivity(intent);
            }
        });

        ObservableScrollView scrollView = (ObservableScrollView) findViewById(R.id.scrollView);
        if (scrollView != null) {
            fab.attachToScrollView(scrollView);
        }

        fab.setVisibility(View.VISIBLE);
    }

    private void setUpToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;

//        actionBar.hide();
        actionBar.setSubtitle(R.string.asset_allocation);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
