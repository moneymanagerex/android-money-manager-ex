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
package com.money.manager.ex.home;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.money.manager.ex.R;
import com.shamanland.fonticon.FontIconView;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Adapter for the expandable drawer menu. Used to avoid displaying the dialogs when selecting the
 * 2nd-level selection and show them as menu children items instead.
 */
public class DrawerMenuGroupAdapter
        extends BaseExpandableListAdapter {

    public ArrayList<DrawerMenuItem> mGroupItems;
    public ArrayList<Object> mChildItems = new ArrayList<>();
    public AppCompatActivity activity;
    private final Context mContext;

    public DrawerMenuGroupAdapter(Context context, ArrayList<DrawerMenuItem> grList, ArrayList<Object> childItems) {
        this.mContext = context;
        this.mGroupItems = grList;
        this.mChildItems = childItems;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<DrawerMenuItem> tempChild = (ArrayList<DrawerMenuItem>) mChildItems.get(groupPosition);
        return tempChild.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        DrawerMenuItem item = (DrawerMenuItem) getChild(groupPosition, childPosition);

        DrawerViewHolder holder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_drawer_child, null);

            convertView.setTag(new DrawerViewHolder(convertView));
        }

        Object tag = convertView.getTag();
        if (tag instanceof DrawerViewHolder) {
            holder = (DrawerViewHolder) tag;
        }

        if (item != null && holder != null) {
            holder.textViewItem.setText(item.getText());
            holder.viewDivider.setVisibility(item.hasDivider() ? View.VISIBLE : View.GONE);
            if (item.getIcon() != null) {
                holder.imageViewIcon.setBackgroundResource(item.getIcon());
            }
            if (item.getIconDrawable() != null) {
                if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    holder.imageViewIcon.setBackground(item.getIconDrawable());
                } else {
                    holder.imageViewIcon.setBackgroundDrawable(item.getIconDrawable());
                }
            }
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<String> children = (ArrayList<String>) mChildItems.get(groupPosition);
        if (children == null) {
            return 0;
        } else {
            return children.size();
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroupItems.get(groupPosition);
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
        DrawerMenuItem item = mGroupItems.get(groupPosition);
        DrawerViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_drawer_group, null);

            convertView.setTag(new DrawerViewHolder(convertView));
        }

        if (convertView.getTag() instanceof DrawerViewHolder) {
            holder = (DrawerViewHolder)convertView.getTag();
        }

        if (item != null && holder != null) {
            holder.textViewItem.setText(item.getText());
            holder.viewDivider.setVisibility(item.hasDivider() ? View.VISIBLE : View.GONE);
            if (item.getIcon() != null) {
                holder.imageViewIcon.setBackgroundResource(item.getIcon());
            }
            if (item.getIconDrawable() != null) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    holder.imageViewIcon.setBackground(item.getIconDrawable());
                } else {
                    holder.imageViewIcon.setBackgroundDrawable(item.getIconDrawable());
                }
            }
        }

        // Show/hide chevron
        FontIconView chevronView = (FontIconView) convertView.findViewById(R.id.caretView);
        if (getChildrenCount(groupPosition) > 0) {
            if (isExpanded) {
                // the group is not expanded and has children.
                chevronView.setText(mContext.getString(R.string.ic_chevron_down));
            } else {
                chevronView.setText(mContext.getString(R.string.ic_chevron_right));
            }

            chevronView.setVisibility(View.VISIBLE);
        } else {
            chevronView.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // If this is false, the children are not selectable / clickable! They don't fire the
        // click event.
        return true;
    }

}
