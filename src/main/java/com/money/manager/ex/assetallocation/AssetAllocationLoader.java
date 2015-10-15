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

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;

/**
 * Custom loader for Asset Allocation. Used to plug into the infrastructue and get
 * notifications on updated/deleted/created objects.
 */
public class AssetAllocationLoader
    extends AsyncTaskLoader<AssetClass> {

    public AssetAllocationLoader(Context context) {
        super(context);
    }

    @Override
    public AssetClass loadInBackground() {
        AssetAllocationService service = new AssetAllocationService(getContext());
        AssetClass result = service.loadAssetAllocation();
        return result;
//        return null;
    }

}
