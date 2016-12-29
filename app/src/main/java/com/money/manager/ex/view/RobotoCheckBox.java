/*******************************************************************************
 * Copyright (C) 2013 The Android Money Manager Ex Project
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/
package com.money.manager.ex.view;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Implementation of a {@link TextView} with native support for all the Roboto fonts on all versions of Android.
 */
public class RobotoCheckBox
    extends AppCompatCheckBox {

    /**
     * Simple constructor to use when creating a widget from code.
     *
     * @param context The Context the widget is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public RobotoCheckBox(Context context) {
        super(context);
    }

    /**
     * Constructor that is called when inflating a widget from XML. This is called
     * when a widget is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     * <p/>
     * <p/>
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the widget is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the widget.
     * @see #RobotoCheckBox(Context, AttributeSet, int)
     */
    public RobotoCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context, attrs);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style. This
     * constructor of View allows subclasses to use their own base style when
     * they are inflating.
     *
     * @param context  The Context the widget is running in, through which it can
     *                 access the current theme, resources, etc.
     * @param attrs    The attributes of the XML tag that is inflating the widget.
     * @param defStyle The default style to apply to this widget. If 0, no style
     *                 will be applied (beyond what is included in the theme). This may
     *                 either be an attribute resource, whose value will be retrieved
     *                 from the current theme, or an explicit style resource.
     * @see #RobotoCheckBox(Context, AttributeSet)
     */
    public RobotoCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseAttributes(context, attrs);
    }

    /**
     * Parse the attributes.
     *
     * @param context The Context the widget is running in, through which it can access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the widget.
     */
    private void parseAttributes(Context context, AttributeSet attrs) {
    	RobotoView.parseAttributes(context, this, attrs);
    }

//    /**
//     * Adjust the margin between the checkbox and the text.
//     * A fix for displaying on JellyBean.
//     * Reference: http://stackoverflow.com/questions/4037795/android-spacing-between-checkbox-and-text
//     * Android version codes: http://developer.android.com/reference/android/os/Build.VERSION_CODES.html
//     * @return Left padding for the text component.
//     */
//    @Override
//    public int getCompoundPaddingLeft() {
//        int result = super.getCompoundPaddingLeft();
//
//        // fix for padding on Jelly Bean (4.2 and lower)
//        // Build.VERSION_CODES.KITKAT = 4.4
//        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            final float scale = this.getResources().getDisplayMetrics().density;
//            //result = (super.getCompoundPaddingLeft() + (int) (10.0f * scale + 0.5f));
//            result = (super.getCompoundPaddingLeft() + (int) (30.0f * scale + 0.5f));
//        }
//
//        return result;
//    }
}
