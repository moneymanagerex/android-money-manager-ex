/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.database.MmexOpenHelper;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.log.ExceptionHandler;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.utils.MmexDatabaseUtils;
import com.shamanland.fonticon.FontIconDrawable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;

import dagger.Lazy;
import timber.log.Timber;

public class Core {

    /**
     * Shown alert dialog
     * @param resId id of string
     */
    public static void alertDialog(Context ctx, int resId) {
        alertDialog(ctx, ctx.getString(resId));
    }

    /**
     * Shown alert dialog
     * @param text to display
     */
    public static void alertDialog(Context context, String text) {
        new AlertDialogWrapper.Builder(context)
            // setting alert dialog
            .setIcon(FontIconDrawable.inflate(context, R.xml.ic_alert))
            .setTitle(R.string.attention)
            .setMessage(text)
            .setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    }
            })
            .show();
    }

    /**
     * Method, which allows you to change the language of the application on the fly.
     * @param context        Context
     * @param languageToLoad language to load for the locale
     * @return and indicator whether the operation was successful
     * Ref: http://stackoverflow.com/questions/22402491/android-change-and-set-default-locale-within-the-app
     */
    public static boolean setAppLocale(Context context, String languageToLoad) {
        try {
            setAppLocale_Internal(context, languageToLoad);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(context, null);
            handler.e(e, "changing app locale");

            return false;
        }
        return true;
    }

    private static void setAppLocale_Internal(Context context, String languageToLoad) {
        Locale locale;

        if (!TextUtils.isEmpty(languageToLoad)) {
            locale = new Locale(languageToLoad);
            // Below method is not available in emulator 4.1.1 (?!).
//                locale = Locale.forLanguageTag(languageToLoad);
        } else {
            locale = Locale.getDefault();
        }
        // http://developer.android.com/reference/java/util/Locale.html#setDefault%28java.util.Locale%29
//            Locale.setDefault(locale);

        // change locale of the configuration
        Resources resources = context.getResources();
//            Configuration config = new Configuration();
//            Configuration config = new Configuration(resources.getConfiguration());
        Configuration config = resources.getConfiguration();

        config.locale = locale;
        // set new locale
        resources.updateConfiguration(config, resources.getDisplayMetrics());
//            getBaseContext().getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    // Instance

    public Core(Context context) {
        super();

        this.mContext = context;
        // .getApplicationContext() == null ? context.getApplicationContext() : context;

        MoneyManagerApplication.getInstance().mainComponent.inject(this);
    }

    private Context mContext;
    @Inject Lazy<MmexOpenHelper> openHelper;

    /**
     * Change the database used by the app.
     *
     * @param path new database
     * @return indicator whether the operation was successful
     */
    public boolean changeDatabase(String path, String password)
        throws Exception {

        Timber.d("switching database to: %s", path);

        File file = new File(path);
        // check if database exists
        if (!file.exists()) {
            throw new Exception(getContext().getString(R.string.path_database_not_exists));
//            Toast.makeText(getContext(), R.string.path_database_not_exists, Toast.LENGTH_LONG).show();
//            return false;
        }
        // check if database can be open in write mode
        if (!file.canWrite()) {
            throw new Exception(getContext().getString(R.string.database_can_not_open_write));
//            Toast.makeText(getContext(), R.string.database_can_not_open_write, Toast.LENGTH_LONG).show();
//            return false;
        }

        // close existing connection.
//        MmexOpenHelper.closeDatabase();
        openHelper.get().close();

        // change database
        new AppSettings(getContext()).getDatabaseSettings().setDatabasePath(path);

        // todo: The components need to be restarted after this!

        // Reinitialize the provider.
//        MmexOpenHelper.reinitialize(getContext().getApplicationContext());

        // todo MmexOpenHelper.getInstance(getContext()).setPassword(password);

        return true;
    }

    /**
     * Method that allows you to make a copy of file
     *
     * @param src Source file
     * @param dst Destination file
     * @throws IOException
     */
    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * Backup current database
     *
     * @return new File database backup
     */
    public File backupDatabase() {
        File database = new File(MoneyManagerApplication.getDatabasePath(getContext()));
        if (!database.exists()) return null;

        //create folder to copy database
        MmexDatabaseUtils dbUtils = new MmexDatabaseUtils(getContext());
        File folderOutput = dbUtils.getDatabaseStorageDirectory();

        //take a folder of database
        ArrayList<File> filesFromCopy = new ArrayList<>();
        //add current database
        filesFromCopy.add(database);
        //get file journal
        File folder = database.getParentFile();
        if (folder != null) {
            for (File file : folder.listFiles()) {
                if (file.getName().startsWith(database.getName()) && !database.getName().equals(file.getName())) {
                    filesFromCopy.add(file);
                }
            }
        }
        //copy all files
        for (int i = 0; i < filesFromCopy.size(); i++) {
            try {
                copy(filesFromCopy.get(i), new File(folderOutput + "/" + filesFromCopy.get(i).getName()));
            } catch (Exception e) {
                Timber.e(e, "backing up the database");
                return null;
            }
        }

        return new File(folderOutput + "/" + filesFromCopy.get(0).getName());
    }

    public String getAppVersionBuild() {
        return Integer.toString(getAppVersionCode());
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
            ExceptionHandler handler = new ExceptionHandler(getContext().getApplicationContext(), this);
            handler.e(e, "getting app version build number");
            return 0;
        }
    }

    public String getAppVersionName() {
        try {
            return getContext().getPackageManager().getPackageInfo(
                getContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
            handler.e(e, "getting app version name");
        }
        return "n/a";
    }

    public String getFullAppVersion() {
        return getAppVersionName() + "." + getAppVersionBuild();
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Return application theme choice from user
     *
     * @return application theme id
     */
    public int getThemeId() {
        try {
            String darkTheme = Constants.THEME_DARK;
            String lightTheme = Constants.THEME_LIGHT;

            String key = mContext.getString(R.string.pref_theme);
            String currentTheme = PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getString(key, lightTheme);

            if (currentTheme.endsWith(darkTheme)) {
                // Dark theme
                return R.style.Theme_Money_Manager_Dark;
            } else {
                // Light theme
                return R.style.Theme_Money_Manager_Light;
            }
        } catch (Exception e) {
            Timber.e(e, "getting theme setting");

            return R.style.Theme_Money_Manager_Light;
        }
    }

    /**
     * Method, which returns the last payee used
     * @return last payee used
     */
    public Payee getLastPayeeUsed() {
//        MmexOpenHelper helper = MmexOpenHelper.getInstance(getContext());
        Payee payee = null;

        String sql =
        "SELECT C.TransID, C.TransDate, C.PAYEEID, P.PAYEENAME, P.CATEGID, P.SUBCATEGID " +
        "FROM CHECKINGACCOUNT_V1 C " +
        "INNER JOIN PAYEE_V1 P ON C.PAYEEID = P.PAYEEID " +
        "WHERE C.TransCode <> 'Transfer' " +
        "ORDER BY C.TransDate DESC, C.TransId DESC " +
        "LIMIT 1";

        Cursor cursor = openHelper.get().getReadableDatabase().rawQuery(sql, null);

        // check if cursor can be open
        if (cursor != null && cursor.moveToFirst()) {
            payee = new Payee();
//            payee.setPayeeId(cursor.getInt(cursor.getColumnIndex(Payee.PAYEEID)));
//            payee.setPayeeName(cursor.getString(cursor.getColumnIndex(Payee.PAYEENAME)));
//            payee.setCategId(cursor.getInt(cursor.getColumnIndex(Payee.CATEGID)));
//            payee.setSubCategId(cursor.getInt(cursor.getColumnIndex(Payee.SUBCATEGID)));
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
     *
     * @return true if running on the tablet, otherwise false
     */
    public boolean isTablet() {
        int layout = getContext().getResources().getConfiguration().screenLayout;
        return ((layout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) ||
                ((layout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
    }

    /**
     * Resolves the id attribute in color
     *
     * @param attr id attribute
     * @return color
     */
    public int resolveColorAttribute(int attr) {
//        Resources.Theme currentTheme = mContext.getTheme();
//        return mContext.getResources().getColor(resolveIdAttribute(attr), currentTheme);
        //return mContext.getResources().getColor(resolveIdAttribute(attr));
        UIHelper uiHelper = new UIHelper(getContext());
        return ContextCompat.getColor(getContext(), uiHelper.resolveIdAttribute(attr));
    }

    public boolean isToDisplayChangelog() {
        int currentVersionCode = getAppVersionCode();
        int lastVersionCode = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getInt(getContext().getString(PreferenceConstants.PREF_LAST_VERSION_KEY), Constants.NOT_SET);

        return lastVersionCode != currentVersionCode;
    }

    public boolean showChangelog() {
        int currentVersionCode = getAppVersionCode();
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                .putInt(getContext().getString(PreferenceConstants.PREF_LAST_VERSION_KEY), currentVersionCode)
                .commit();

        // create layout
        View view = LayoutInflater.from(getContext()).inflate(R.layout.changelog_layout, null);
        //create dialog
        new AlertDialogWrapper.Builder(getContext())
            .setCancelable(false)
            .setTitle(R.string.changelog)
            .setView(view)
            .setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
            )
            // show dialog
            .create().show();
        return true;
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
        boolean find = false;
        String[] availableDateFormats = getContext().getResources().getStringArray(R.array.date_format_mask);
        for (int i = 0; i < availableDateFormats.length; i++) {
            if (pattern.equals(availableDateFormats[i])) {
                find = true;
                break;
            }
        }

        String result = find
            ? pattern
            : null;
        return result;
    }

    public int getColourFromAttribute(int attribute) {
        TypedArray ta = getAttributeValue(attribute);
        int result = ta.getColor(0, Color.TRANSPARENT);

        ta.recycle();

        return result;
    }

//    public int getColourFromStyledAttribute(int attribute) {
//        int[] attrs = { attribute };
//        TypedArray ta = getContext().obtainStyledAttributes(getContext().getTheme(), attrs);
//    }

    public int getColourFromThemeAttribute(int attribute) {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(attribute, typedValue, true);
        return typedValue.resourceId;
    }

    public boolean usingDarkTheme(){
        int currentTheme = this.getThemeId();
        return currentTheme == R.style.Theme_Money_Manager_Dark;
    }

    // private

    private TypedArray getAttributeValue(int attribute) {
//        TypedValue typedValue = new TypedValue();
//        context.getTheme().resolveAttribute(attribute, typedValue, true);
//        return typedValue;

//        int[] arrayAttributes = new int[] { attribute };
//        TypedArray typedArray = context.obtainStyledAttributes(arrayAttributes);
//        int value = typedArray.getColor(0, context.getResources().getColor(R.color.abBackground));
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
