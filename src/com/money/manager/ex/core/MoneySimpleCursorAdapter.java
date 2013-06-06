package com.money.manager.ex.core;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.money.manager.ex.view.RobotoView;

public class MoneySimpleCursorAdapter extends SimpleCursorAdapter {

	@SuppressWarnings("deprecation")
	public MoneySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
	}
	
	public MoneySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
	}

	@Override
	public void setViewText(TextView v, String text) {
		if (v != null) {
			v.setTypeface(RobotoView.obtainTypeface(mContext, RobotoView.getUserFont()));
		}
		super.setViewText(v, text);
	}
}
