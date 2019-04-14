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

package com.money.manager.ex.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.common.CategoryListFragment;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.domainmodel.Category;

import java.util.HashMap;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class CategoryExpandableListAdapter
	extends BaseExpandableListAdapter {

    public CategoryExpandableListAdapter(Context context, int layout,
                                         List<Category> categories,
                                         HashMap<Category,
                                                 List<QueryCategorySubCategory>> subCategories,
                                         boolean showSelector) {
        mContext = context;
        mLayout = layout;
        mCategories = categories;
        mSubCategories = subCategories;
        mShowSelector = showSelector;
    }

	private Context mContext;
	private int mLayout;
	
	private List<Category> mCategories;
	private HashMap<Category, List<QueryCategorySubCategory>> mSubCategories;
	
	private int mIdGroupChecked = ListView.INVALID_POSITION;
	private int mIdChildChecked = ListView.INVALID_POSITION;

    private boolean mShowSelector;

    private final int[] expandedStateSet = {android.R.attr.state_expanded};
    private final int[] emptyStateSet = {};

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (groupPosition < mCategories.size()) {
			Category category = mCategories.get(groupPosition);
			List<QueryCategorySubCategory> categorySubCategories = mSubCategories.get(category);
			if (childPosition < categorySubCategories.size()) {
                return categorySubCategories.get(childPosition);
            }
		}
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
							 View convertView, ViewGroup parent) {
		CategoryListItemViewHolderChild holder;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(mLayout, null);
			
			holder = new CategoryListItemViewHolderChild(convertView);

			convertView.setTag(holder);
		} else {
            holder = (CategoryListItemViewHolderChild) convertView.getTag();
        }
		
		QueryCategorySubCategory entity = (QueryCategorySubCategory) getChild(groupPosition, childPosition);
		if (entity == null) return convertView;

        holder.text1.setText(entity.getSubcategoryName());

        holder.text2.setText(entity.getCategName());
        holder.text2.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));

        // Selector. Always hidden on subcategories.

//		if (mShowSelector) {
//			holder.selector.setVisibility(View.VISIBLE);
//			// set the tag to be the group position
//			holder.selector.setTag(entity.getCategId() + ":" + entity.getSubCategId());
//
//			holder.selector.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					String tag = v.getTag().toString();
//					String[] ids = tag.split(":");
//					Integer groupId = Integer.parseInt(ids[0]);
//					Integer childId = Integer.parseInt(ids[1]);
//					setIdChildChecked(groupId, childId);
//					// close
//					closeFragment();
//				}
//			});
//		} else {
			holder.selector.setVisibility(View.GONE);
//		}

        // indent subcategory
        holder.indent.setVisibility(View.VISIBLE);

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
		CategoryListItemViewHolderGroup holder;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(mLayout, null);
			
			holder = new CategoryListItemViewHolderGroup(convertView);

			convertView.setTag(holder);
		} else {
			holder = (CategoryListItemViewHolderGroup)convertView.getTag();
		}

		// prevent exceptions. todo: Find out how this happens in the first place.
		if (mCategories.size() == 0) return convertView;

        Category category = mCategories.get(groupPosition);

        holder.text1.setText(category.getName());

        // Selector

        if (mShowSelector) {
            holder.selector.setVisibility(View.VISIBLE);
            // set the tag to be the group position
            holder.selector.setTag(category.getId());

            holder.selector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tag = v.getTag().toString();
                    Integer groupId = Integer.parseInt(tag);
                    setIdGroupChecked(groupId);
                    // close
                    closeFragment();
                }
            });
        } else {
            holder.selector.setVisibility(View.GONE);
        }


        if (holder.collapseImageView != null) {
            TypedArray expandableListViewStyle = getContext().getTheme().obtainStyledAttributes(new int[]{android.R.attr.expandableListViewStyle});
            TypedArray groupIndicator = getContext().getTheme().obtainStyledAttributes(expandableListViewStyle.getResourceId(0, 0), new int[]{android.R.attr.groupIndicator});
            Drawable drawable = groupIndicator.getDrawable(0);
            holder.collapseImageView.setImageDrawable(drawable);
            expandableListViewStyle.recycle();
            groupIndicator.recycle();

            boolean hasChildren = getChildrenCount(groupPosition) != 0;
            if (!hasChildren) {
                holder.collapseImageView.setVisibility( View.INVISIBLE );
            } else {
                holder.collapseImageView.setVisibility( View.VISIBLE );

                if (drawable != null) {
                    drawable.setState(isExpanded ? expandedStateSet : emptyStateSet);
                }
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
//        if (mIdGroupChecked == idGroup) {
//            mIdGroupChecked = ListView.INVALID_POSITION;
//        } else {
		mIdGroupChecked = idGroup;
		mIdChildChecked = ExpandableListView.INVALID_POSITION;
//        }
	}
	
	public void setIdChildChecked(int idGroup, int idChild) {
		mIdGroupChecked = idGroup;
		mIdChildChecked = idChild;
	}

	private void closeFragment() {
        FragmentActivity activity = (FragmentActivity) getContext();
        CategoryListFragment fragment =
            (CategoryListFragment) activity
                .getSupportFragmentManager()
                .findFragmentByTag(CategoryListActivity.FRAGMENTTAG);
        fragment.setResultAndFinish();
    }

    private Context getContext() {
        return mContext;
    }
}