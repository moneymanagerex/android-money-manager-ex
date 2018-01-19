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

package com.money.manager.ex.assetallocation.list;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.view.recycler.CursorRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the Asset Class list (picker)
 */
public class AssetClassListAdapter
    extends CursorRecyclerViewAdapter<AssetClassListItemViewHolder> {

    public AssetClassListAdapter(Cursor cursor) {
        super(cursor);

        this.assetClasses = new ArrayList<>();
    }

    public List<AssetClass> assetClasses;

    @Override
    public AssetClassListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        super.onCreateViewHolder(parent, viewType);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.item_asset_class_list, parent, false);

        AssetClassListItemViewHolder viewHolder = new AssetClassListItemViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(AssetClassListItemViewHolder viewHolder, Cursor cursor) {
        AssetClass assetClass = new AssetClass();

        assetClass.loadFromCursor(cursor);

        viewHolder.id = assetClass.getId();
        viewHolder.nameView.setText(assetClass.getName());
    }
}
