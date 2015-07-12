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
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.money.manager.ex.CategorySubCategoryExpandableListActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.common.CategorySubCategoryExpandableLoaderListFragment;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.view.RobotoTextView;
import com.shamanland.fonticon.FontIconDrawable;

import java.util.HashMap;
import java.util.List;

public class CategoryExpandableListAdapter
		extends BaseExpandableListAdapter {
	private Context mContext;
	
	private int mLayout;
	
	private List<TableCategory> mCategories;
	private HashMap<TableCategory, List<QueryCategorySubCategory>> mSubCategories;
	
	private int mIdGroupChecked = ListView.INVALID_POSITION;
	private int mIdChildChecked = ListView.INVALID_POSITION;
	
	public static class ViewHolderGroup {
//		TextView text1;
        RobotoTextView text1;
	}
	
	public static class ViewHolderChild {
//		TextView text1;
        RobotoTextView text1;
		TextView text2;
	}
	
	public CategoryExpandableListAdapter(Context context, int layout,
                                         List<TableCategory> categories,
                                         HashMap<TableCategory, List<QueryCategorySubCategory>> subCategories) {
		mContext = context;
		mLayout = layout;
		mCategories = categories;
		mSubCategories = subCategories;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (groupPosition < mCategories.size()) {
			TableCategory category = mCategories.get(groupPosition);
			List<QueryCategorySubCategory> categorySubCategories = mSubCategories.get(category);
			if (childPosition < categorySubCategories.size())
				return categorySubCategories.get(childPosition);
		}
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolderChild holder;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(mLayout, null);
			
			holder = new ViewHolderChild();
//			holder.text1 = (TextView)convertView.findViewById(android.R.id.text1);
            holder.text1 = (RobotoTextView)convertView.findViewById(android.R.id.text1);
			holder.text2 = (TextView)convertView.findViewById(android.R.id.text2);
			
			convertView.setTag(holder);
		}
		holder = (ViewHolderChild)convertView.getTag();
		
		QueryCategorySubCategory entity = (QueryCategorySubCategory)getChild(groupPosition, childPosition);
		
		if (entity == null) return convertView;

        holder.text1.setText(entity.getSubcategoryName());
        holder.text2.setText(entity.getCategName());

        holder.text2.setTextColor(mContext.getResources().getColor(android.R.color.darker_gray));

        // Selector

        TextView selectorText = (TextView) convertView.findViewById(R.id.selectorText);
        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/mmex.ttf");
        selectorText.setTypeface(typeface);

        RelativeLayout selector = (RelativeLayout) convertView.findViewById(R.id.selector);
        if (selector != null) {
            // set the tag to be the group position
            selector.setTag(entity.getCategId() + ":" + entity.getSubCategId());

            selector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tag = v.getTag().toString();
                    String[] ids = tag.split(":");
                    Integer groupId = Integer.parseInt(ids[0]);
                    Integer childId = Integer.parseInt(ids[1]);
                    setIdChildChecked(groupId, childId);
                    // close
                    FragmentActivity activity = (FragmentActivity) mContext;
                    CategorySubCategoryExpandableLoaderListFragment fragment =
                            (CategorySubCategoryExpandableLoaderListFragment) activity
                                    .getSupportFragmentManager()
                                    .findFragmentByTag(CategorySubCategoryExpandableListActivity.FRAGMENTTAG);
                    fragment.setResultAndFinish();
                }
            });

        }

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mSubCategories.get(mCategories.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mCategories.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mCategories.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		ViewHolderGroup holder;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(mLayout, null);
			
			holder = new ViewHolderGroup();
            holder.text1 = (RobotoTextView) convertView.findViewById(android.R.id.text1);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderGroup)convertView.getTag();
		}
		
		// check position and size of ArrayList
		if (groupPosition < mCategories.size()) {
            TableCategory category = mCategories.get(groupPosition);

			holder.text1.setText(category.getCategName());

            // Selector

            TextView selectorText = (TextView) convertView.findViewById(R.id.selectorText);
            Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/mmex.ttf");
            selectorText.setTypeface(typeface);

            // set the selector image
//            ImageView selectorImageView = (ImageView) convertView.findViewById(R.id.selectorImage);
//            if (selectorImageView != null) {
//                selectorImageView.setImageDrawable(FontIconDrawable.inflate(mContext,
//                        R.xml.ic_right_arrow));
//            }

            RelativeLayout selector = (RelativeLayout) convertView.findViewById(R.id.selector);
            if (selector != null) {
                // set the tag to be the group position
                selector.setTag(category.getCategId());

                selector.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String tag = v.getTag().toString();
                        Integer groupId = Integer.parseInt(tag);
                        setIdGroupChecked(groupId);
                        // close
                        FragmentActivity activity = (FragmentActivity) mContext;
                        CategorySubCategoryExpandableLoaderListFragment fragment =
                                (CategorySubCategoryExpandableLoaderListFragment) activity
                                        .getSupportFragmentManager()
                                        .findFragmentByTag(CategorySubCategoryExpandableListActivity.FRAGMENTTAG);
                        fragment.setResultAndFinish();
                    }
                });
            }
        }
		
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	public int getIdGroupChecked() {
		return mIdGroupChecked;
	}
	
	public int getIdChildChecked() {
		return mIdChildChecked;
	}
	
	public void setIdGroupChecked(int idGroup) {
		// If an existing group is clicked, collapse it. Reset the expanded id.
        if (mIdGroupChecked == idGroup) {
            mIdGroupChecked = ListView.INVALID_POSITION;
        } else {
            mIdGroupChecked = idGroup;
            mIdChildChecked = ExpandableListView.INVALID_POSITION;
        }
	}
	
	public void setIdChildChecked(int idGroup, int idChild) {
		mIdGroupChecked = idGroup;
		mIdChildChecked = idChild;
	}
	
}