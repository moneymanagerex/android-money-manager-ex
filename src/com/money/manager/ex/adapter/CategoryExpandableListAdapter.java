package com.money.manager.ex.adapter;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.TableCategory;

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
		return mSubCategories.get(mCategories.get(groupPosition)).get(childPosition);
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
		
		holder.text1.setText(((QueryCategorySubCategory)getChild(groupPosition, childPosition)).getSubCategName());
		holder.text2.setText(((QueryCategorySubCategory)getChild(groupPosition, childPosition)).getCategName());
		
		holder.text2.setTextColor(mContext.getResources().getColor(android.R.color.darker_gray));
		
		if (holder.text1 instanceof CheckedTextView) {
			((CheckedTextView)holder.text1).setChecked(mIdChildChecked == ((QueryCategorySubCategory)getChild(groupPosition, childPosition)).getSubCategId());
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
		holder.text1.setText(((TableCategory)mCategories.get(groupPosition)).getCategName());
		
		if (holder.text1 instanceof CheckedTextView) {
			boolean isGroupChecked = mIdGroupChecked == ((TableCategory)mCategories.get(groupPosition)).getCategId();
			((CheckedTextView)holder.text1).setChecked(isGroupChecked && mIdChildChecked == ListView.INVALID_POSITION);
			if (isGroupChecked) {
				((ExpandableListView)parent).expandGroup(groupPosition, true);
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