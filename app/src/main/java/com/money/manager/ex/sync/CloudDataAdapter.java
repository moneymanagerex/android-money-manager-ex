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

package com.money.manager.ex.sync;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cloudrail.si.types.CloudMetaData;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.mmex_icon_font_typeface_library.MMXIconFont;
import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;
import com.shamanland.fonticon.FontIconDrawable;

import java.util.List;

/**
 * Adapter for the items in the cloud storage. Used for db file picker.
 */
public class CloudDataAdapter
    extends RecyclerView.Adapter<CloudItemViewHolder> {

    public CloudDataAdapter(Context context, List<CloudMetaData> data) {
        mContext = context;
        mData = data;
    }

    private Context mContext;
    public List<CloudMetaData> mData;

    @Override
    public CloudItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_remote_storage_content, parent, false);

        return new CloudItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CloudItemViewHolder holder, int position) {
        // get the data
        CloudMetaData item = mData.get(position);

        holder.itemPosition = position;
        holder.itemPath = item.getPath();

        holder.nameTextView.setText(item.getName());
        // Icon: folder or file
        Drawable icon = null;
        if (item.getFolder()) {
            icon = new UIHelper(getContext()).getIcon(GoogleMaterial.Icon.gmd_folder_open).sizeDp(30);
        } else {
            //icon = FontIconDrawable.inflate(getContext(), R.xml.ic_);
        }
        holder.nameTextView.setCompoundDrawables(icon, null, null, null);
        holder.nameTextView.setCompoundDrawablePadding(16);
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        } else {
            return 0;
        }
    }

    public Context getContext() {
        return mContext;
    }
}
