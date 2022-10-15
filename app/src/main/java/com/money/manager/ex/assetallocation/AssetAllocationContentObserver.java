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

package com.money.manager.ex.assetallocation;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import com.money.manager.ex.domainmodel.AssetClass;

import androidx.loader.content.Loader;

/**
 * Content observer that glues data change notifications and Asset Allocation Loader.
 */
public class AssetAllocationContentObserver
    extends ContentObserver {

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public AssetAllocationContentObserver(Handler handler, Loader<AssetClass> loader) {
        super(handler);

        this.loader = loader;
    }

    private Loader<AssetClass> loader;

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        // do s.th.
        // depending on the handler you might be on the UI
        // thread, so be cautious!

//        Log.d("observer", "change detected");
        // notify Loader#onContentChanged() somehow...
        this.loader.onContentChanged();
    }
}
