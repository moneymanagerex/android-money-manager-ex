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
package com.money.manager.ex.home;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.shamanland.fonticon.FontIconView;

import java.util.ArrayList;

/**
 * Adapter for the drawer menu with groups. Used to avoid displaying the dialogs for the
 * 2nd-stage selection.
 *
 * Created by Alen Siljak on 20/07/2015.
 */
public class DrawerMenuGroupAdapter
        extends BaseExpandableListAdapter {

    public ArrayList<DrawerMenuItem> mGroupItems;
    public ArrayList<String> tempChild;
    public ArrayList<Object> mChildItems = new ArrayList<Object>();
    public LayoutInflater mInflater;
    public Activity activity;
    private final Context mContext;

    public DrawerMenuGroupAdapter(Context context, ArrayList<DrawerMenuItem> grList, ArrayList<Object> childItem) {
        this.mContext = context;
        this.mGroupItems = grList;
        this.mChildItems = childItem;
    }

    public void setInflater(LayoutInflater mInflater, Activity act) {
        this.mInflater = mInflater;
        activity = act;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        tempChild = (ArrayList<String>) mChildItems.get(groupPosition);

        TextView text;
        if (convertView == null) {
            convertView = new TextView(mContext);
        }
        text = (TextView) convertView;
        text.setText(">" + tempChild.get(childPosition));

//		convertView.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Toast.makeText(activity, tempChild.get(childPosition),
//						Toast.LENGTH_SHORT).show();
//			}
//		});

        convertView.setTag(tempChild.get(childPosition));
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return ((ArrayList<String>) mChildItems.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public int getGroupCount() {
        return mGroupItems.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
//        if (convertView == null) {
//            convertView = new TextView(mContext);
//        }

//        DrawerMenuItem item = getItem(position);
        DrawerMenuItem item = mGroupItems.get(groupPosition);
        DrawerViewHolder holder = null;
        View view = convertView;
        if (convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_drawer_group, null);
            TextView textViewItem = (TextView)view.findViewById(R.id.textViewItem);
            ImageView imageViewIcon = (ImageView)view.findViewById(R.id.imageViewIcon);
            View viewDivider = view.findViewById(R.id.viewDivider);
            view.setTag(new DrawerViewHolder(textViewItem, imageViewIcon, viewDivider));
        }

//        TextView view = (TextView) convertView;
//        view.setText(mGroupItems.get(groupPosition).getText());

        if (view != null && holder == null) {
            if (view.getTag() instanceof DrawerViewHolder) {
                holder = (DrawerViewHolder)view.getTag();
            }
        }

        if (item != null && holder != null) {
            holder.textViewItem.setText(item.getText());
            holder.viewDivider.setVisibility(item.hasDivider() ? View.VISIBLE : View.GONE);
            if (item.getIcon() != null) {
                holder.imageViewIcon.setBackgroundResource(item.getIcon());
            }
            if (item.getIconDrawable() != null) {
                holder.imageViewIcon.setBackground(item.getIconDrawable());
            }
        }

        // Show caret
        if (isExpanded) {
            // todo: show indicator that the group is expanded.
            // id: caretView
            FontIconView caretView = (FontIconView) view.findViewById(R.id.caretView);
            caretView.setText("n");
        }

//        convertView.setTag(mGroupItems.get(groupPosition));
//        return convertView;

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

}
