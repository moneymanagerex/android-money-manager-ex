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
package com.money.manager.ex.database;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.datalayer.AssetClassStockRepository;

/**
 * Implementation of the simple cursor loader that does not require content provider.
 */
public class MmexSimpleCursorLoader
    extends SimpleCursorLoader {

    public MmexSimpleCursorLoader(Context context, int assetClassId) {
        super(context);

        this.assetClassId = assetClassId;
    }

    private int assetClassId;

    @Override
    public Cursor loadInBackground() {
        AssetClassStockRepository repo = new AssetClassStockRepository(getContext());
        return repo.fetchCursorAssignedSecurities(this.assetClassId);
    }
}
