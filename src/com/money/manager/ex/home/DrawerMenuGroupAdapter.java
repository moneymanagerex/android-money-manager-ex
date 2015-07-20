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
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter for the drawer menu with groups. Used to avoid displaying the dialogs for the
 * 2nd-stage selection.
 *
 * Created by Alen Siljak on 20/07/2015.
 */
public class DrawerMenuGroupAdapter
        extends BaseExpandableListAdapter {

    public ArrayList<String> groupItem, tempChild;
    public ArrayList<Object> Childtem = new ArrayList<Object>();
    public LayoutInflater minflater;
    public Activity activity;
    private final Context context;

    public DrawerMenuGroupAdapter(Context context, ArrayList<String> grList, ArrayList<Object> childItem) {
        this.context = context;
        groupItem = grList;
        this.Childtem = childItem;
    }

    public void setInflater(LayoutInflater mInflater, Activity act) {
        this.minflater = mInflater;
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
        tempChild = (ArrayList<String>) Childtem.get(groupPosition);

        TextView text;
        if (convertView == null) {
            convertView = new TextView(context);
        }
        text = (TextView) convertView;
        text.setText(">"+tempChild.get(childPosition));

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
        return ((ArrayList<String>) Childtem.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public int getGroupCount() {
        return groupItem.size();
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
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new TextView(context);
        }
        ((TextView) convertView).setText(groupItem.get(groupPosition));
        convertView.setTag(groupItem.get(groupPosition));
        return convertView;
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
