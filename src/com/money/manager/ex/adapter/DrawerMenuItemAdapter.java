/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.money.manager.ex.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.money.manager.ex.R;

public class DrawerMenuItemAdapter
		extends ArrayAdapter<DrawerMenuItem> {
	
	public DrawerMenuItemAdapter(Context context) {
		super(context, 0);
	}
	
	public static class ViewHolder {
		public TextView textViewItem;
		public ImageView imageViewIcon;
        public View viewDivider;

        public ViewHolder(TextView textViewItem, ImageView imageViewIcon, View viewDivider) {
            this.textViewItem = textViewItem;
			this.imageViewIcon = imageViewIcon;
            this.viewDivider = viewDivider;
        }
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DrawerMenuItem item = getItem(position);
		ViewHolder holder = null;
		View view = convertView;
		
		if (view == null) {
			view = LayoutInflater.from(getContext()).inflate(R.layout.item_drawer, null);
			TextView textViewItem = (TextView)view.findViewById(R.id.textViewItem);
			ImageView imageViewIcon = (ImageView)view.findViewById(R.id.imageViewIcon);
            View viewDivider = view.findViewById(R.id.viewDivider);
            view.setTag(new ViewHolder(textViewItem, imageViewIcon, viewDivider));
        }
		
		if (view != null && holder == null) {
			if (view.getTag() instanceof ViewHolder) {
				holder = (ViewHolder)view.getTag();
			}
		}

		if (item != null && holder != null) {
            holder.textViewItem.setText(item.getText());
            holder.viewDivider.setVisibility(item.hasDivider() ? View.VISIBLE : View.GONE);
            if (item.getIcon() != null)
				holder.imageViewIcon.setBackgroundResource(item.getIcon());
		}
		
		return view;
	}
}
