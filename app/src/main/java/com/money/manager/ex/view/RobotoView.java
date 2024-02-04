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
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.widget.TextView;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;

import timber.log.Timber;

/**
 * Implementation of a {@link TextView} with native support for all the Roboto fonts on all
 * versions of Android, with customization from users.
 */
public class RobotoView {
    private static final String LOGCAT = RobotoView.class.getSimpleName();
    /*
     * Permissible values ​​for the "typeface" attribute.
     */
    private final static int DEFAULT_FONT = -1;
    private final static int ROBOTO_THIN = 0;
    private final static int ROBOTO_THIN_ITALIC = 1;
    private final static int ROBOTO_LIGHT = 2;
    private final static int ROBOTO_LIGHT_ITALIC = 3;
    private final static int ROBOTO_REGULAR = 4;
    private final static int ROBOTO_ITALIC = 5;
    private final static int ROBOTO_MEDIUM = 6;
    private final static int ROBOTO_MEDIUM_ITALIC = 7;
    private final static int ROBOTO_BOLD = 8;
    private final static int ROBOTO_BOLD_ITALIC = 9;
    private final static int ROBOTO_BLACK = 10;
    private final static int ROBOTO_BLACK_ITALIC = 11;
    private final static int ROBOTO_CONDENSED = 12;
    private final static int ROBOTO_CONDENSED_ITALIC = 13;
    private final static int ROBOTO_CONDENSED_BOLD = 14;
    private final static int ROBOTO_CONDENSED_BOLD_ITALIC = 15;
    private final static int ROBOTOSLAB_THIN = 16;
    private final static int ROBOTOSLAB_LIGHT = 17;
    private final static int ROBOTOSLAB_REGULAR = 18;
    private final static int ROBOTOSLAB_BOLD = 19;
    private final static int ROBOTO_CONDENSED_LIGHT = 20;
    private final static int ROBOTO_CONDENSED_LIGHT_ITALIC = 21;

    /**
     * List of created typefaces for later reused.
     */
    private final static SparseArray<Typeface> mTypefaces = new SparseArray<>(16);

    /**
     * Font user
     */
    private static int mUserFont = ROBOTO_CONDENSED_LIGHT;
    private static float mUserFontSize;

    public static int getUserFont() {
        return mUserFont;
    }

    public static void setUserFont(int font) {
        mUserFont = font;
    }

    public static void setUserFontSize(Context context, String fontSize) {
        if (fontSize.equalsIgnoreCase("micro")) {
            mUserFontSize = context.getResources().getDimension(R.dimen.mmx_text_view_size_micro);
        } else if (fontSize.equalsIgnoreCase("small")) {
            mUserFontSize = context.getResources().getDimension(R.dimen.mmx_text_view_size_small);
        } else if (fontSize.equalsIgnoreCase("default")) {
            mUserFontSize = MmexApplication.getTextSize();
        } else if (fontSize.equalsIgnoreCase("medium")) {
            mUserFontSize = context.getResources().getDimension(R.dimen.mmx_text_view_size_medium);
        } else if (fontSize.equalsIgnoreCase("large")) {
            mUserFontSize = context.getResources().getDimension(R.dimen.mmx_text_view_size_large);
        } else {
            mUserFontSize = MmexApplication.getTextSize();
        }
    }

    public static float getUserFontSize() {
        return mUserFontSize;
    }

    /**
     * Parse the attributes.
     *
     * @param context The Context the widget is running in, through which it can access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the widget.
     */
    public static void parseAttributes(Context context, TextView view, AttributeSet attrs) {
        // Typeface.createFromAsset doesn't work in the layout editor, so skipping.
        if (view.isInEditMode()) {
            return;
        }
        // set type face
        setTypefaceView(context, view, attrs);
        // set text size
        setTextSizeView(context, view, attrs);
    }

    /**
     * Obtain typeface.
     *
     * @param context       The Context the widget is running in, through which it can
     *                      access the current theme, resources, etc.
     * @param typefaceValue values ​​for the "typeface" attribute
     * @return Roboto {@link Typeface}
     * @throws IllegalArgumentException if unknown `typeface` attribute value.
     */
    public static Typeface obtainTypeface(Context context, int typefaceValue) throws IllegalArgumentException {
        Typeface typeface = mTypefaces.get(typefaceValue);
        if (typeface == null) {
            typeface = createTypeface(context, typefaceValue);
            mTypefaces.put(typefaceValue, typeface);
        }
        return typeface;
    }

    /**
     * Create typeface from assets.
     *
     * @param context       The Context the widget is running in, through which it can
     *                      access the current theme, resources, etc.
     * @param typefaceValue values ​​for the "typeface" attribute
     * @return Roboto {@link Typeface}
     * @throws IllegalArgumentException if unknown `typeface` attribute value.
     */
    private static Typeface createTypeface(Context context, int typefaceValue) throws IllegalArgumentException {
        Typeface typeface;
        switch (typefaceValue) {
            case DEFAULT_FONT:
                typeface = null;
                break;
            case ROBOTO_THIN:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
                break;
            case ROBOTO_THIN_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-ThinItalic.ttf");
                break;
            case ROBOTO_LIGHT:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
                break;
            case ROBOTO_LIGHT_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-LightItalic.ttf");
                break;
            case ROBOTO_REGULAR:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
                break;
            case ROBOTO_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Italic.ttf");
                break;
            case ROBOTO_MEDIUM:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
                break;
            case ROBOTO_MEDIUM_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-MediumItalic.ttf");
                break;
            case ROBOTO_BOLD:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
                break;
            case ROBOTO_BOLD_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-BoldItalic.ttf");
                break;
            case ROBOTO_BLACK:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Black.ttf");
                break;
            case ROBOTO_BLACK_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-BlackItalic.ttf");
                break;
            case ROBOTO_CONDENSED:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Condensed.ttf");
                break;
            case ROBOTO_CONDENSED_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-CondensedItalic.ttf");
                break;
            case ROBOTO_CONDENSED_BOLD:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-BoldCondensed.ttf");
                break;
            case ROBOTO_CONDENSED_BOLD_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-BoldCondensedItalic.ttf");
                break;
            case ROBOTOSLAB_THIN:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoSlab-Thin.ttf");
                break;
            case ROBOTOSLAB_LIGHT:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoSlab-Light.ttf");
                break;
            case ROBOTOSLAB_REGULAR:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoSlab-Regular.ttf");
                break;
            case ROBOTOSLAB_BOLD:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoSlab-Bold.ttf");
                break;
            case ROBOTO_CONDENSED_LIGHT:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-CondensedLight.ttf");
                break;
            case ROBOTO_CONDENSED_LIGHT_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-CondensedLightItalic.ttf");
                break;
            default:
                throw new IllegalArgumentException("Unknown `typeface` attribute value " + typefaceValue);
        }
        return typeface;
    }

    /**
     * Sets the user typeface to view
     *
     * @param context
     * @param view
     * @param attrs
     */
    public static void setTypefaceView(Context context, TextView view, AttributeSet attrs) {
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.RobotoTextView);
        int typefaceValue = values.getInt(R.styleable.RobotoTextView_typeface, 0);
        values.recycle();

        // user-fonts
        if (typefaceValue == 0 && getUserFont() != DEFAULT_FONT) {
            typefaceValue = getUserFont();
            // manage bold or italic
            Typeface typeface = view.getTypeface();
            if (typeface != null) {
                switch (typefaceValue) {
                    case ROBOTO_LIGHT:
                        if (typeface.isItalic() && typeface.isBold()) {
                            typefaceValue = ROBOTO_BOLD_ITALIC;
                        } else if (typeface.isItalic()) {
                            typefaceValue = ROBOTO_LIGHT_ITALIC;
                        } else if (typeface.isBold()) {
                            typefaceValue = ROBOTO_BOLD;
                        }
                        break;
                    case ROBOTO_REGULAR:
                        if (typeface.isItalic() && typeface.isBold()) {
                            typefaceValue = ROBOTO_BOLD_ITALIC;
                        } else if (typeface.isItalic()) {
                            typefaceValue = ROBOTO_ITALIC;
                        } else if (typeface.isBold()) {
                            typefaceValue = ROBOTO_BOLD;
                        }
                        break;
                    case ROBOTO_MEDIUM:
                        if (typeface.isItalic()) {
                            typefaceValue = ROBOTO_MEDIUM_ITALIC;
                        }
                        break;
                    case ROBOTO_BLACK:
                        if (typeface.isItalic()) {
                            typefaceValue = ROBOTO_BLACK_ITALIC;
                        }
                        break;
                    case ROBOTO_CONDENSED:
                        if (typeface.isItalic() && typeface.isBold()) {
                            typefaceValue = ROBOTO_CONDENSED_BOLD_ITALIC;
                        } else if (typeface.isItalic()) {
                            typefaceValue = ROBOTO_CONDENSED_ITALIC;
                        } else if (typeface.isBold()) {
                            typefaceValue = ROBOTO_CONDENSED_BOLD;
                        }
                        break;
                    case ROBOTO_CONDENSED_LIGHT:
                        if (typeface.isItalic()) {
                            typefaceValue = ROBOTO_CONDENSED_ITALIC;
                        }
                        break;
                    default:
                        break;
                }
            }
            // if typefaceValue = -1 default font
            if (!(typefaceValue == DEFAULT_FONT)) {
                try {
                    view.setTypeface(RobotoView.obtainTypeface(context, typefaceValue));
                } catch (Exception e) {
                    Timber.e(e, "setting roboto typeface");
                }
            }
        }
    }

    /**
     * Sets the user text size to view
     *
     * @param context
     * @param view
     * @param attrs
     */
    public static void setTextSizeView(Context context, TextView view, AttributeSet attrs) {
        // set text size
        if (view.getTextSize() == MmexApplication.getTextSize()) {
            if (view.getTextSize() != getUserFontSize()) {
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, getUserFontSize());
            }
        }
    }
}
