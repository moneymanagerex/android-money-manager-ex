package com.money.manager.ex.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.money.manager.ex.R;

public class DrawerMenuItemAdapter extends ArrayAdapter<DrawerMenuItem> {
	
	public DrawerMenuItemAdapter(Context context) {
		super(context, 0);
	}
	
	public static class ViewHolder {
		public TextView textViewItem;
		
		public ViewHolder(TextView textViewItem) {
			this.textViewItem = textViewItem;
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
			view.setTag(new ViewHolder(textViewItem));
		}
		
		if (view != null && holder == null) {
			if (view.getTag() instanceof ViewHolder) {
				holder = (ViewHolder)view.getTag();
			}
		}

		if (item != null && holder != null) {
			holder.textViewItem.setText(item.getItemText());
		}
		
		return view;
	}
}
