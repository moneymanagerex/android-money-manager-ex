package com.money.manager.ex.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.TableCategory;

import java.util.HashMap;
import java.util.List;

public class CategoryExpandableListAdapter extends BaseExpandableListAdapter {
	private Context mContext;
	
	private int mLayout;
	
	private List<TableCategory> mCategories;
	private HashMap<TableCategory, List<QueryCategorySubCategory>> mSubCategories;
	
	private int mIdGroupChecked = ListView.INVALID_POSITION;
	private int mIdChildChecked = ListView.INVALID_POSITION;
	
	public static class ViewHolderGroup {
		TextView text1;
	}
	
	public static class ViewHolderChild {
		TextView text1;
		TextView text2;
	}
	
	public CategoryExpandableListAdapter(Context context, int layout, List<TableCategory> categories, HashMap<TableCategory, List<QueryCategorySubCategory>> subCategories) {
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
			holder.text1 = (TextView)convertView.findViewById(android.R.id.text1);
			holder.text2 = (TextView)convertView.findViewById(android.R.id.text2);
			
			convertView.setTag(holder);
		}
		holder = (ViewHolderChild)convertView.getTag();
		
		QueryCategorySubCategory categorySubCategory = (QueryCategorySubCategory)getChild(groupPosition, childPosition);
		
		if (categorySubCategory != null) {
			holder.text1.setText(categorySubCategory.getSubCategName());
			holder.text2.setText(categorySubCategory.getCategName());
			
			holder.text2.setTextColor(mContext.getResources().getColor(android.R.color.darker_gray));
			
			boolean isChildSelected = mIdChildChecked == ((QueryCategorySubCategory)getChild(groupPosition, childPosition)).getSubCategId();
			if (holder.text1 instanceof CheckedTextView) {
				((CheckedTextView)holder.text1).setChecked(isChildSelected);
			} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				convertView.setBackgroundColor(mContext.getResources().getColor(isChildSelected ? R.color.holo_blue_light : android.R.color.transparent));
			}
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
			holder.text1 = (TextView)convertView.findViewById(android.R.id.text1);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderGroup)convertView.getTag();
		}
		
		// check position and size of ArrayList
		if (groupPosition < mCategories.size()) {
			holder.text1.setText(((TableCategory)mCategories.get(groupPosition)).getCategName());
			
			boolean isGroupChecked = mIdGroupChecked == ((TableCategory)mCategories.get(groupPosition)).getCategId();
			if (holder.text1 instanceof CheckedTextView) {
				((CheckedTextView)holder.text1).setChecked(isGroupChecked && mIdChildChecked == ListView.INVALID_POSITION);
				if (isGroupChecked) {
					((ExpandableListView)parent).expandGroup(groupPosition, true);
				}
			} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				convertView.setBackgroundColor(mContext.getResources().getColor(isGroupChecked && mIdChildChecked == ExpandableListView.INVALID_POSITION ? R.color.holo_blue_light : android.R.color.transparent));
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
		mIdGroupChecked = idGroup;
		mIdChildChecked = ExpandableListView.INVALID_POSITION;
	}
	
	public void setIdChildChecked(int idGroup, int idChild) {
		mIdGroupChecked = idGroup;
		mIdChildChecked = idChild;
	}
	
}