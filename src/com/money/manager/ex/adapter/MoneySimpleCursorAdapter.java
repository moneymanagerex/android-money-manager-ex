package com.money.manager.ex.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.money.manager.ex.core.Core;
import com.money.manager.ex.view.RobotoView;

public class MoneySimpleCursorAdapter extends SimpleCursorAdapter {
	private static final String LOGCAT = MoneySimpleCursorAdapter.class.getSimpleName();
	private String mHighlight;
	private Core mCore;
	
	@SuppressWarnings("deprecation")
	public MoneySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
		mCore = new Core(context);
	}
	
	public MoneySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		mCore = new Core(context);
	}

	@Override
	public void setViewText(TextView v, String text) {
		if (v != null) {
			try {
				v.setTypeface(RobotoView.obtainTypeface(mContext, RobotoView.getUserFont()));
			} catch (Exception e){
				Log.e(LOGCAT, e.getMessage());
			}
		}
		super.setViewText(v, text);
		// check if highlight text
		if (!TextUtils.isEmpty(getHighlightFilter())) {
			v.setText(mCore.highlight(getHighlightFilter(), (String)v.getText()));
		}
	}

	public String getHighlightFilter() {
		return mHighlight;
	}

	public void setHighlightFilter(String mHighlight) {
		this.mHighlight = mHighlight;
	}
}
