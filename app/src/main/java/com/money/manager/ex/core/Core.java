/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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
 */

package com.money.manager.ex.core;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.settings.AppSettings;

import java.text.DateFormatSymbols;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.inject.Inject;

import dagger.Lazy;
import timber.log.Timber;

public class Core {

    @Inject
    public Core(Context context) {
        super();

        this.mContext = context;
        // .getApplicationContext() == null ? context.getApplicationContext() : context;

        MmexApplication.getApp().iocComponent.inject(this);
    }

    private final Context mContext;
    @Inject Lazy<MmxOpenHelper> openHelper;
    @Inject Lazy<AppSettings> appSettingsLazy;

    /**
     * Show alert dialog.
     * @param textResourceId id of string to display as a message
     */
    public void alert(int textResourceId) {
        alert(Constants.NOT_SET_INT, textResourceId);
    }

    public void alert(int title, int text) {
        if (title == Constants.NOT_SET) {
            title = R.string.attention;
        }

        UIHelper ui = new UIHelper(getContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setIcon(ui.getIcon(GoogleMaterial.Icon.gmd_warning))
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
    /**
     * Get a versioncode of the application.
     * @return application version name
     */
    public int getAppVersionCode() {
        try {
            PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(
                getContext().getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e, "getting app version build number");
            return 0;
        }
    }

    public String getAppVersionName() {
        try {
            return getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e, "getting app version name");
        }
        return "n/a";
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Method, which returns the last payee used
     * @return last payee used
     */
    public Payee getLastPayeeUsed() {
//        MmxOpenHelper helper = MmxOpenHelper.getInstance(getContext());
        Payee payee = null;

        String sql =
        "SELECT C.TransID, C.TransDate, C.PAYEEID, P.PAYEENAME, P.CATEGID " +
        "FROM CHECKINGACCOUNT_V1 C " +
        "INNER JOIN PAYEE_V1 P ON C.PAYEEID = P.PAYEEID " +
        "WHERE C.TransCode <> 'Transfer' AND (c.DELETEDTIME IS NULL OR c.DELETEDTIME = '') " +
        "ORDER BY C.TransDate DESC, C.TransId DESC " +
        "LIMIT 1";

        Cursor cursor = openHelper.get().getReadableDatabase().query(sql);

        // check if cursor can be open
        if (cursor != null && cursor.moveToFirst()) {
            payee = new Payee();
            payee.loadFromCursor(cursor);

            cursor.close();
        }
        //close database
        //helper.close();

        return payee;
    }

    /**
     * Return arrays of month formatted and localized
     * @return arrays of months
     */
    public String[] getListMonths() {
        return new DateFormatSymbols().getMonths();
    }

    public String getDefaultSystemDateFormat() {
        Locale locale = Locale.getDefault();
        SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, locale);
        String pattern = sdf.toLocalizedPattern();
        // replace date
        if (pattern.contains("dd")) {
            pattern = pattern.replace("dd", "%d");
        } else {
            pattern = pattern.replace("d", "%d");
        }
        // replace month
        if (pattern.contains("MM")) {
            pattern = pattern.replace("MM", "%m");
        } else {
            pattern = pattern.replace("M", "%m");
        }
        // replace year
        pattern = pattern.replace("yyyy", "%Y");
        pattern = pattern.replace("yy", "%y");
        // check if exists in format definition
        boolean found = false;
        String[] availableDateFormats = getContext().getResources().getStringArray(R.array.date_format_mask);
        for (int i = 0; i < availableDateFormats.length; i++) {
            if (pattern.equals(availableDateFormats[i])) {
                found = true;
                break;
            }
        }

        return found
                ? pattern
                : null;
    }

    public int getColourFromAttribute(int attribute) {
        TypedArray ta = getAttributeValue(attribute);
        int result = ta.getColor(0, Color.TRANSPARENT);

        ta.recycle();

        return result;
    }

    /**
     * This method allows to highlight in bold the content of a search string
     * @param search       string
     * @param originalText string where to find
     * @return CharSequence modified
     */
    public CharSequence highlight(String search, String originalText) {
        if (TextUtils.isEmpty(search))
            return originalText;
        // ignore case and accents
        // the same thing should have been done for the search text
        String normalizedText = Normalizer.normalize(originalText, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();

        int start = normalizedText.indexOf(search.toLowerCase());
        if (start < 0) {
            // not found, nothing to to
            return originalText;
        } else {
            // highlight each appearance in the original text
            // while searching in normalized text
            Spannable highlighted = new SpannableString(originalText);
            while (start >= 0) {
                int spanStart = Math.min(start, originalText.length());
                int spanEnd = Math.min(start + search.length(), originalText.length());

                highlighted.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), spanStart,
                        spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                start = normalizedText.indexOf(search, spanEnd);
            }
            return highlighted;
        }
    }

    /**
     * Function that determines if the application is running on tablet
     * @return true if running on the tablet, otherwise false
     */
    public boolean isTablet() {
        long layout = getContext().getResources().getConfiguration().screenLayout;
        return ((layout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) ||
                ((layout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
    }

    /**
     * Resolves the id attribute in color
     * @param attr id attribute
     * @return color
     */
    public int resolveColorAttribute(int attr) {
//        Resources.Theme currentTheme = mContext.getTheme();
//        return mContext.getResources().getColor(resolveAttribute(attr), currentTheme);
        //return mContext.getResources().getColor(resolveAttribute(attr));
        UIHelper uiHelper = new UIHelper(getContext());
        return ContextCompat.getColor(getContext(), uiHelper.resolveAttribute(attr));
    }

    public boolean isToDisplayChangelog() {
        int currentVersionCode = getAppVersionCode();
        int lastVersionCode = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt(getContext().getString(R.string.pref_last_version_key), Constants.NOT_SET_INT);

        return lastVersionCode != currentVersionCode;
    }

    /**
     * Method, which allows you to change the language of the application on the fly.
     * @param languageToLoad language to load for the locale
     * @return and indicator whether the operation was successful
     * Ref: http://stackoverflow.com/questions/22402491/android-change-and-set-default-locale-within-the-app
     */
    public boolean setAppLocale(String languageToLoad) {
        try {
            setAppLocale_Internal(languageToLoad);
        } catch (Exception e) {
            Timber.e(e, "changing app locale");

            return false;
        }
        return true;
    }

    private void setAppLocale_Internal(String languageToLoad) {
        Locale locale;

        if (!TextUtils.isEmpty(languageToLoad)) {
            locale = new Locale(languageToLoad);
//                locale = Locale.forLanguageTag(languageToLoad);
        } else {
            locale = Locale.getDefault();
        }
        // http://developer.android.com/reference/java/util/Locale.html#setDefault%28java.util.Locale%29
//            Locale.setDefault(locale);

        // change locale of the configuration
        Resources resources = getContext().getResources();
        Configuration config = resources.getConfiguration();

        config.setLocale(locale);

        // set new locale
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public boolean showChangelog() {
        // create changelog layout
        View view = LayoutInflater.from(getContext()).inflate(R.layout.changelog_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false)
                .setTitle(R.string.changelog)
                .setView(view)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

        // mark as seen
        int currentVersionCode = getAppVersionCode();
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                .putInt(getContext().getString(R.string.pref_last_version_key), currentVersionCode)
                .apply();

        return true;
    }

//    public long getColourFromStyledAttribute(long attribute) {
//        int[] attrs = { attribute };
//        TypedArray ta = getContext().obtainStyledAttributes(getContext().getTheme(), attrs);
//    }

//    public long getColourFromThemeAttribute(long attribute) {
//        TypedValue typedValue = new TypedValue();
//        getContext().getTheme().resolveAttribute(attribute, typedValue, true);
//        return typedValue.resourceId;
//    }

    // private

    private TypedArray getAttributeValue(int attribute) {
//        TypedValue typedValue = new TypedValue();
//        context.getTheme().resolveAttribute(attribute, typedValue, true);
//        return typedValue;

//        int[] arrayAttributes = new int[] { attribute };
//        TypedArray typedArray = context.obtainStyledAttributes(arrayAttributes);
//        long value = typedArray.getColor(0, context.getResources().getColor(R.color.abBackground));
//        typedArray.recycle();

        // Create an array of the attributes we want to resolve
        // using values from a theme
        int[] attrs = new int[] { attribute /* index 0 */};
        // Obtain the styled attributes. 'themedContext' is a context with a
        // theme, typically the current Activity (i.e. 'this')
        TypedArray ta = getContext().obtainStyledAttributes(attrs);
        // To get the value of the 'listItemBackground' attribute that was
        // set in the theme used in 'themedContext'. The parameter is the index
        // of the attribute in the 'attrs' array. The returned Drawable
        // is what you are after
//        Drawable drawableFromTheme = ta.getDrawable(0 /* index */);

        // Finally, free the resources used by TypedArray
//        ta.recycle();

        return ta;
    }
}
