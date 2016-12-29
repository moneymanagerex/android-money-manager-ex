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

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.TextView;

import com.shamanland.fonticon.CompoundDrawables;

/**
 * Implementation of a {@link TextView} with native support for all the Roboto fonts on all versions of Android.
 *
 * @author a.lazzari
 */
public class RobotoTextViewFontIcon extends AppCompatTextView {
    /**
     * Simple constructor to use when creating a widget from code.
     *
     * @param context The Context the widget is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public RobotoTextViewFontIcon(Context context) {
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
     * @see #RobotoTextView(Context, AttributeSet, int)
     */
    public RobotoTextViewFontIcon(Context context, AttributeSet attrs) {
        super(context, attrs);

        if(!this.isInEditMode()) {
            CompoundDrawables.init(context, attrs, this);
        }

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
     * @see #RobotoTextView(Context, AttributeSet)
     */
    public RobotoTextViewFontIcon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if(!this.isInEditMode()) {
            CompoundDrawables.init(context, attrs, this);
        }

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

    public void updateCompoundDrawables() {
        CompoundDrawables.update(this);
    }

    @TargetApi(17)
    public void updateCompoundDrawablesRelative() {
        CompoundDrawables.updateRelative(this);
    }
}
