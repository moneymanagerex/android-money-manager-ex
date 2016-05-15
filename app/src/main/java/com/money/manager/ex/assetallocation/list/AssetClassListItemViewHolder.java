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

package com.money.manager.ex.assetallocation.list;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoTextViewFontIcon;
import com.shamanland.fonticon.FontIconTextView;

import org.greenrobot.eventbus.EventBus;

/**
 * View holder for the list item.
 */
public class AssetClassListItemViewHolder
    extends RecyclerView.ViewHolder {

    public RobotoTextViewFontIcon nameView;

    public AssetClassListItemViewHolder(View itemView) {
        super(itemView);

        nameView = (RobotoTextViewFontIcon) itemView.findViewById(R.id.nameView);
        nameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ListItemClickedEvent());
            }
        });
    }
}
