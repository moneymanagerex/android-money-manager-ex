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
package com.money.manager.ex.home;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.money.manager.ex.R;

public class DrawerMenuItemAdapter
    extends ArrayAdapter<DrawerMenuItem> {

	public DrawerMenuItemAdapter(Context context) {
		super(context, 0);

		this.context = context;
	}

	private Context context;

    public Context getContext() {
        return this.context;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DrawerMenuItem item = getItem(position);

		DrawerViewHolder holder = null;
		View view = convertView;
		
		if (view == null) {
			view = LayoutInflater.from(getContext()).inflate(R.layout.item_drawer, null);
            view.setTag(new DrawerViewHolder(view));
        }
		
		if (view != null && holder == null) {
			if (view.getTag() instanceof DrawerViewHolder) {
				holder = (DrawerViewHolder)view.getTag();
			}
		}

		if (item != null && holder != null) {
            holder.textViewItem.setText(item.getText());
            holder.viewDivider.setVisibility(item.hasDivider() ? View.VISIBLE : View.GONE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                if (item.getIcon() != null) {
                    holder.imageViewIcon.setBackgroundResource(item.getIcon());
                }
                if (item.getIconDrawable() != null) {
                    holder.imageViewIcon.setBackground(item.getIconDrawable());
                }
            } else {
                holder.imageViewIcon.setBackgroundDrawable(item.getIconDrawable());
            }
		}
		
		return view;
	}
}
