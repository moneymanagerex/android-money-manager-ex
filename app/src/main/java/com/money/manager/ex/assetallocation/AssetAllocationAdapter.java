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

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;

import androidx.cursoradapter.widget.CursorAdapter;

/**
 * Adapter for the Asset Allocation list.
 */
public class AssetAllocationAdapter
    extends CursorAdapter {

    public AssetAllocationAdapter(Context context, Cursor cursor) {
        super(context, cursor, -1);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_asset_allocation, parent, false);

        // view holder pattern
        AssetClassViewHolder holder = AssetClassViewHolder.initialize(view);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Object tag = view.getTag();
        if (tag == null) return;

        AssetClassViewHolder holder = (AssetClassViewHolder) tag;

        MatrixCursorColumns values = MatrixCursorColumns.fromCursor(context, cursor);

        UIHelpers.populateAssetClassRow(holder, values);
    }
}
