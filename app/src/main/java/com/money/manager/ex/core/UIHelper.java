package com.money.manager.ex.core;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.money.manager.ex.Constants;

/**
 * Various methods that assist with the UI Android requirements.
 */
public class UIHelper {
    public UIHelper(Context context) {
        this.context = context;
    }

    private Context context;

    public Context getContext() {
        return this.context;
    }

    /**
     * Finds the theme color from an attribute.
     * @param attrId    Id of the attribute to parse. i.e. R.attr.some_color
     */
    public int getColor(int attrId) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(attrId, typedValue, true);
        return typedValue.data;
    }

    /**
     * Resolve the id attribute into int value
     * @param attr id attribute
     * @return resource id
     */
    public int resolveIdAttribute(int attr) {
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(attr, tv, true))
            return tv.resourceId;
        else
            return Constants.NOT_SET;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }
}
