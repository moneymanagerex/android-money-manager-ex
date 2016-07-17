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

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.money.manager.ex.R;
import com.money.manager.ex.assetallocation.AssetAllocationActivity;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class FullAssetAllocationActivity
    extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_asset_allocation);

        AssetAllocationService service = new AssetAllocationService(this);
        AssetClass assetAllocation = service.loadAssetAllocation();
        // see if we have the allocation.
//        Parcelable parcel = getIntent().getParcelableExtra(AssetAllocationActivity.KEY_ASSET_ALLOCATION);
//        if (parcel != null) {
//            assetAllocation = Parcels.unwrap(parcel);
//        }

        List<AssetClassViewModel> model = createViewModel(assetAllocation);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        FullAssetAllocationAdapter adapter = new FullAssetAllocationAdapter(model);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
}
