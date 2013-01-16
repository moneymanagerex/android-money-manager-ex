package com.money.manager.ex.core;

import android.content.Context;
import android.util.TypedValue;

public class Core {
	private Context context;

	public Core(Context context) {
		super();
		this.context = context;
	}

	/**
	 * Resolves the id attribute in color
	 * 
	 * @param attr
	 *            id attribute
	 * @return color
	 */
	public int resolveColorAttribute(int attr) {
		return context.getResources().getColor(resolveColorIdAttribute(attr));
	}

	/**
	 * Resolves the id attribute in id color
	 * 
	 * @param attr
	 *            id attribute
	 * @return id color
	 */
	public int resolveColorIdAttribute(int attr) {
		TypedValue tv = new TypedValue();
		if (context.getTheme().resolveAttribute(attr, tv, true))
			return tv.resourceId;
		else 
			return -1;
	}
}
