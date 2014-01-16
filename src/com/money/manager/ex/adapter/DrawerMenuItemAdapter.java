package com.money.manager.ex.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.money.manager.ex.R;

public class DrawerMenuItemAdapter extends ArrayAdapter<DrawerMenuItem> {
	
	public DrawerMenuItemAdapter(Context context) {
		super(context, 0);
	}
	
	public static class ViewHolder {
		public TextView textViewItem;
		public ImageView imageViewIcon;
		
		public ViewHolder(TextView textViewItem, ImageView imageViewIcon) {
			this.textViewItem = textViewItem;
			this.imageViewIcon = imageViewIcon;
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
			view.setTag(new ViewHolder(textViewItem, imageViewIcon));
		}
		
		if (view != null && holder == null) {
			if (view.getTag() instanceof ViewHolder) {
				holder = (ViewHolder)view.getTag();
			}
		}

		if (item != null && holder != null) {
			holder.textViewItem.setText(item.getItemText());
			if (item.getIcon() != null)
				holder.imageViewIcon.setBackground(getContext().getResources().getDrawable(item.getIcon()));
		}
		
		return view;
	}
}
