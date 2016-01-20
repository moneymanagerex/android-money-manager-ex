package com.money.manager.ex.core;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import com.money.manager.ex.Constants;

/**
 * Various methods that assist with the UI Android requirements.
 */
public class UIHelper {

    /**
     * Finds the theme color from an attribute.
     * @param context   Context
     * @param attrId    Id of the attribute to parse. i.e. R.attr.some_color
     */
    public static int getColor(Context context, int attrId) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attrId, typedValue, true);
        int color = typedValue.data;
        return color;
    }

    /**
     * Resolve the id attribute into int value
     *
     * @param attr id attribute
     * @return resource id
     */
    public static int resolveIdAttribute(Context context, int attr) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(attr, tv, true))
            return tv.resourceId;
        else
            return Constants.NOT_SET;
    }

}
