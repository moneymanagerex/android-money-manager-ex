package com.money.manager.ex.core;

import android.content.Context;
import android.util.TypedValue;

public class Core {
	public static final int INVALID_ATTRIBUTE = -1;
	private Context context;

	public Core(Context context) {
		super();
		this.context = context;
	}

	/**
	 * Resolves the id attribute in color
	 * 
	 * @param attr id attribute
	 * @return color
	 */
	public int resolveColorAttribute(int attr) {
		return context.getResources().getColor(resolveIdAttribute(attr));
	}
	
	/**
	 * Resolve the id attribute into int value
	 * @param attr id attribute
	 * @return
	 */
	public int resolveIdAttribute(int attr) {
		TypedValue tv = new TypedValue();
		if (context.getTheme().resolveAttribute(attr, tv, true))
			return tv.resourceId;
		else 
			return INVALID_ATTRIBUTE;
	}
}
