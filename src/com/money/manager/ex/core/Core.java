/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.money.manager.ex.core;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.database.TableInfoTable;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.database.TableSubCategory;
import com.money.manager.ex.dropbox.SimpleCrypto;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.utils.CurrencyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

public class Core {
    public static final int INVALID_ATTRIBUTE = -1;
    private static final String LOGCAT = Core.class.getSimpleName();
    private static final String base64 = "A346EA3CFF9C3F679946C4294AAF737D1E2708694B66A13D1A615F7C0752F4770664F8407DDCE84AB1DE2770DA62760D2ACF6F45F9DCDDB7E5018D9F5FCD561544C6F4BDFF7801BC1DF65A97C40AC293A3C097C4E3F3CDD5366EAB72B29CA78D234B69C30047201323D7070BCC42EA02A9903955783CC701FB3B3CCB9855B16D70EAC0931668FD33E1C7634EAEECAC93D1980D3B81DB10A50905815A54FFEAE951DDA6862561B971D1F439FE49A146F33FF661E79533F574F187377DE7229433ABBFF7ACEC1C05BC38520BB537D1418239077A696AA00E415980922AD575B39E83B70B04C836D2E9BD9398884469A27E5826DAF1FF77D6DE687BE17ACA2243F25901168FD37E14717D38737A963175E4131B819B475A700A3532F10CA5A3028ACC4061217B7CD0EF070FB9B0354BD8D0F95719E5C1F27BB227CA62EBE1B6C04BFB31A5592701F263023A14A2D27231D44B60A5D0B5394154C786C87D0911C566640C1F67A223A302BF19E47137CEBF2DDFC3594E1AE0962A135BBEF9355AADB28AD494AEA033CB9C877254676844D0F246AAE95F7D95932929A01675E8AEC76097E46702514A60FF6A3A7C72C848D002CD1EEEA424FACE609126134C2E24A8D93B2777F3DE24CE8C557ABA1B293DEC5119A62DAE6249EAD0EB88A9B4E1D8729968A8098AF61A2BD67446F692F665F891F1909B1CA66AA6872524E7F7C6EB7EF79D87D6FD6665149F5FC94362533D9AB7CB51BCB6B4599BA217F82220C5217F6AC6D138A3B8E5D8DAC4DE6618B435F006E8426065593347411CD6BE9CECA39A23D693C7A072F75937F5BE454119D780E9623E1424F14631EF693ECAD21438473E5813DF6D7B5984693441AD8C15EA4543BC958AEE8F8C0AF587D18893AA3584268091B606A04B0B099B5AD3D97D5347BFB09F64B3CA7F3836D11EDF2C56C436877658A54C86ABEDE5BB86E0C3324E52CCEDD1DD1F41D347ECEE8E8566FCF481EA4FD5841F3102AB25228AD93A7A89C9728E18A1315EA46CCD94BA84A62EDFB65A992DDC8BC87983AE9F11AA606DCE191E209073B94B40760820C024EBD717D2D4D66F459EF7A7C577371CD88864B8E94E48D4883D623EB1EB1CDB8109157C6B2444F79869F2FE56C0A4CCA491DBE6A69F";
    @SuppressWarnings("unused")
    private static final String token = "1E1380731503B92242AB2A5D8A41F8AE761A9ECD8BC7BBC9C229C5B49F8A032C59467A21872756BF85B7CB781DDB1DE6C1D1D77C9D5E7EBCA07CD01E2525CE4B186128463FAC8C99956BF102DC28A56647F15248624DF792B7D437699024102D24B48B5FAFDE18E690F29224A6C233493D60696A1CF018F002C9CC4A057548665829B552B2F42F2CAACC5F47F5EC6A01B4F525AC2E0E07AEF2D18B871E1145066D5AF20F32D69B0CEDE9D0B0CEC313E34DF0B55F2A7D66A7CEBF481F877BDF04C9E7CEF1BC1F230342EF07C952D8C9A584B2D4D572D470075F2F2306D3F07785340BB49D6FED77E7A1F0CC30F79F21C774B0C298637E23C2A9FD4ECAB9E74A53B074A25B6566D3995DF2D25998A818B439273761E9A911EF73A118C3FE22CCBA4E5FA461501EE9E0E5A6E99942758BC9D9A9D12672A70E388437F2374C48314D13E9205973B7A452EA9FE97CFFCF3A966F02931EFBB5CE3AB70D5AEF2E9670A8B1C3CFF91889D4CAA4A176495954F0F95D4B71562527AB9E596D1F8A7147E221FD6E6C434740CED20D9422AC6ED96A48";
    private Context mContext;

    public Core(Context context) {
        super();
        this.mContext = context;
    }

    /**
     * Get a versioncode of the application.
     *
     * @param context Executing context.
     * @return application version name
     */
    public static int getCurrentVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

//    /**
//     * Take a versioncode of this application
//     *
//     * @param context executing context
//     * @return application version name
//     */
//    public static String getCurrentVersionName(Context context) {
//        try {
//            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//            return packageInfo.versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            return "";
//        }
//    }

    public static String getAppBase64() {
        try {
            return SimpleCrypto.decrypt(MoneyManagerApplication.KEY, getTokenApp());
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
        }
        return null;
    }

    private static String getTokenApp() {
        try {
            return SimpleCrypto.decrypt(MoneyManagerApplication.DROPBOX_APP_KEY, base64);
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
        }
        return null;
    }

    /**
     * Shown alert dialog
     *
     * @param resId id of string
     * @return alert dialog
     */
    public static void alertDialog(Context ctx, int resId) {
        alertDialog(ctx, ctx.getString(resId));
    }

    /**
     * Shown alert dialog
     *
     * @param text to display
     * @return alert dialog
     */
    public static void alertDialog(Context ctx, String text) {
        new AlertDialogWrapper.Builder(ctx)
            // setting alert dialog
            .setIcon(R.drawable.ic_action_warning_light)
            .setTitle(R.string.attention)
            .setMessage(text)
//            .setNeutralButton()
            .setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    }
            })
                .show();
    }

    /**
     * Backup current database
     *
     * @return new File database backup
     */
    public File backupDatabase() {
        File database = new File(MoneyManagerApplication.getDatabasePath(mContext));
        if (!database.exists()) return null;
        //create folder to copy database
        File folderOutput = getExternalStorageDirectoryApplication();
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
                Log.e(LOGCAT, e.getMessage());
                return null;
            }
        }

        return new File(folderOutput + "/" + filesFromCopy.get(0).getName());
    }

    /**
     * Method, which allows you to change the language of the application
     *
     * @param context        Context
     * @param languageToLoad language to load for the locale
     * @return and indicator whether the operation was successful
     */
    public static boolean changeLocaleApp(Context context, String languageToLoad) {
        try {
            // load locale
            Locale locale;
            if (!TextUtils.isEmpty(languageToLoad)) {
                locale = new Locale(languageToLoad);
            } else {
                locale = Locale.getDefault();
            }
            Locale.setDefault(locale);
            // change locale to configuration
            Configuration config = new Configuration();
            config.locale = locale;
            // set new locale
            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
            return false;
        }
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
     * Method, which formats the default currency amount in TextView
     *
     * @param view   TextView to set the amount
     * @param amount to be formatted
     */
    public void formatAmountTextView(TextView view, double amount) {
        formatAmountTextView(view, amount, null);
    }

    /**
     * Method, which formats the default currency amount in TextView
     *
     * @param view       TextView to set the amount
     * @param amount     to be formatted
     * @param currencyId Id currency to be formatted
     */
    public void formatAmountTextView(TextView view, double amount, Integer currencyId) {
        CurrencyUtils currencyUtils = new CurrencyUtils(mContext);

        if (currencyId == null) {
            view.setText(currencyUtils.getBaseCurrencyFormatted(amount));
        } else {
            view.setText(currencyUtils.getCurrencyFormatted(currencyId, amount));
        }

        view.setTag(amount);
    }

    /**
     * Return application theme choice from user
     *
     * @return application theme id
     */
    public int getThemeApplication() {
        try {
            String darkTheme = Constants.THEME_DARK;
            String lightTheme = Constants.THEME_LIGHT;

            String currentTheme = PreferenceManager.getDefaultSharedPreferences(mContext)
                    .getString(mContext.getString(PreferenceConstants.PREF_THEME), lightTheme);

            if (currentTheme.endsWith(darkTheme)) {
                // Dark theme
                return R.style.Theme_Money_Manager;
            } else {
                // Light theme
                return R.style.Theme_Money_Manager_Light_DarkActionBar;
            }
        } catch (Exception e) {
            Log.e("", e.getMessage());
            return R.style.Theme_Money_Manager_Light_DarkActionBar;
        }
    }

    public boolean usingDarkTheme(){
        int currentTheme = this.getThemeApplication();
        return currentTheme == R.style.Theme_Money_Manager;
    }

    /**
     * Method, which returns the last payee used
     *
     * @return last payee used
     */
    public TablePayee getLastPayeeUsed() {
        MoneyManagerOpenHelper helper = MoneyManagerOpenHelper.getInstance(mContext);
        SQLiteDatabase database = helper.getReadableDatabase();
        TablePayee payee = null;

        Cursor cursor = database.rawQuery(
                "SELECT C.PAYEEID, P.PAYEENAME, P.CATEGID, P.SUBCATEGID " +
                        "FROM CHECKINGACCOUNT_V1 C, PAYEE_V1 P WHERE C.PAYEEID = P.PAYEEID AND " +
                        "C.TRANSID = (select max(T2.TRANSID) from checkingaccount_v1 T2 where T2.TRANSDATE = C.TRANSDATE)",
                null);

        // check if cursor can be open
        if (cursor != null && cursor.moveToFirst()) {
            payee = new TablePayee();
            payee.setPayeeId(cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID)));
            payee.setPayeeName(cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME)));
            payee.setCategId(cursor.getInt(cursor.getColumnIndex(TablePayee.CATEGID)));
            payee.setSubCategId(cursor.getInt(cursor.getColumnIndex(TablePayee.SUBCATEGID)));

            cursor.close();
        }
        //close database
        //helper.close();

        return payee;
    }

    /**
     * Get application directory on external storage
     *
     * @return directory created if not exists
     */
    public File getExternalStorageDirectoryApplication() {
        //get external storage
        File externalStorage;
        File folderOutput;
        externalStorage = Environment.getExternalStorageDirectory();
        if (externalStorage != null && externalStorage.exists() && externalStorage.isDirectory() && externalStorage.canWrite()) {
            //create folder to copy database
            folderOutput = new File(externalStorage + File.separator + mContext.getPackageName());
            //make a directory
            if (!folderOutput.exists()) {
                folderOutput = new File(externalStorage + "/MoneyManagerEx");
                if (!folderOutput.exists()) {
                    if (!folderOutput.mkdirs()) return mContext.getFilesDir();
                }
            }
            //folder create
            return folderOutput;
        } else {
            return mContext.getFilesDir();
        }
    }

    /**
     * Get dropbox application directory on external storage
     *
     * @return directory created if not exists
     */
    public File getExternalStorageDirectoryDropboxApplication() {
        File folder = getExternalStorageDirectoryApplication();
        // manage folder
        if (folder != null && folder.exists() && folder.isDirectory() && folder.canWrite()) {
            // create a folder for dropbox
            File folderDropbox = new File(folder + "/dropbox");
            // check if folder exists otherwise create
            if (!folderDropbox.exists()) {
                if (!folderDropbox.mkdirs()) return mContext.getFilesDir();
            }
            return folderDropbox;
        } else {
            return mContext.getFilesDir();
        }
    }

    /**
     * Returns category and sub-category formatted
     *
     * @param categoryId
     * @param subCategoryId
     * @return category : sub-category
     */
    public String getCategSubName(int categoryId, int subCategoryId) {
        String categoryName, subCategoryName, ret;
        TableCategory category = new TableCategory();
        TableSubCategory subCategory = new TableSubCategory();
        Cursor cursor;
        MoneyManagerOpenHelper helper = MoneyManagerOpenHelper.getInstance(mContext);
//        SQLiteDatabase db = helper.getReadableDatabase();
        // category
//        cursor = db.query(category.getSource(), null,
//                TableCategory.CATEGID + "=?", new String[]{Integer.toString(categoryId)}, null, null, null);
        cursor = mContext.getContentResolver().query(category.getUri(),
                null,
                TableCategory.CATEGID + "=?",
                new String[]{Integer.toString(categoryId)},
                null);

        if ((cursor != null) && (cursor.moveToFirst())) {
            // set category name and sub category name
            categoryName = cursor.getString(cursor.getColumnIndex(TableCategory.CATEGNAME));
        } else {
            categoryName = null;
        }
        if (cursor != null) {
            cursor.close();
        }
        // sub-category
//        cursor = db.query(subCategory.getSource(), null,
//                TableSubCategory.SUBCATEGID + "=?", new String[]{Integer.toString(subCategoryId)}, null, null, null);
        cursor = mContext.getContentResolver().query(subCategory.getUri(),
                null,
                TableSubCategory.SUBCATEGID + "=?",
                new String[]{Integer.toString(subCategoryId)},
                null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            // set category name and sub category name
            subCategoryName = cursor.getString(cursor.getColumnIndex(TableSubCategory.SUBCATEGNAME));
        } else {
            subCategoryName = null;
        }
        if (cursor != null) {
            cursor.close();
        }
        ////helper.close();

        ret = (!TextUtils.isEmpty(categoryName) ? categoryName : "") +
                (!TextUtils.isEmpty(subCategoryName) ? ":" + subCategoryName : "");

//        db.close();

        return ret;
    }

//    /**
//     * Returns category and sub-category formatted
//     *
//     * @param queryCategorySubCategory object
//     * @return category : sub-category
//     */
//    public String getCategSubName(QueryCategorySubCategory queryCategorySubCategory) {
//        return getCategSubName(queryCategorySubCategory.getCategId(), queryCategorySubCategory.getSubCategId());
//    }

//    /**
//     * Returns category and sub-category formatted
//     *
//     * @param category object
//     * @return category : sub-category
//     */
//    public String getCategSubName(TableCategory category) {
//        return getCategSubName(category.getCategId(), -1);
//    }

//    /**
//     * Returns category and sub-category formatted
//     *
//     * @param subCategory object
//     * @return category : sub-category
//     */
//    public String getCategSubName(TableSubCategory subCategory) {
//        return getCategSubName(subCategory.getCategId(), subCategory.getSubCategId());
//    }

    /**
     * Retrieve value of info
     *
     * @param info to be retrieve
     * @return value
     */
    public String getInfoValue(String info) {
        TableInfoTable infoTable = new TableInfoTable();
//        MoneyManagerOpenHelper helper;
        Cursor data;
        String ret = null;

        try {
            data = MoneyManagerOpenHelper.getInstance(mContext)
                .getReadableDatabase().query(infoTable.getSource(), null,
                    TableInfoTable.INFONAME + "=?", new String[]{info}, null, null, null);
            if (data != null && data.moveToFirst()) {
                ret = data.getString(data.getColumnIndex(TableInfoTable.INFOVALUE));
            }
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
        } finally {
//            // close data
//            if (data != null)
//                data.close();
            //if (helper != null) helper.close();
        }

        return ret;
    }

    /**
     * Return arrays of month formatted and localizated
     *
     * @return arrays of months
     */
    public String[] getListMonths() {
        return new DateFormatSymbols().getMonths();
    }

    public HashMap<String, String> getCurrenciesCodeAndSymbol() {
        HashMap<String, String> map = new HashMap<>();
        // compose map
        String[] codes = mContext.getResources().getStringArray(R.array.currencies_code);
        String[] symbols = mContext.getResources().getStringArray(R.array.currencies_symbol);

        for (int i = 0; i < codes.length; i++) {
            map.put(codes[i], symbols[i]);
        }

        return map;
    }

    /**
     * This method allows to highlight in bold the content of a search string
     *
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

    public boolean importCurrenciesFromLocaleAvaible() {
        Locale[] locales = Locale.getAvailableLocales();
        TableCurrencyFormats tableCurrencyFormats = new TableCurrencyFormats();
        // get map codes and symbols
        HashMap<String, String> symbols = getCurrenciesCodeAndSymbol();

        for (Locale locale : locales) {
            Currency currency;
            try {
                currency = Currency.getInstance(locale);

                // check if already exists currency symbol
                Cursor cursor = mContext.getContentResolver().query(tableCurrencyFormats.getUri(), null,
                        TableCurrencyFormats.CURRENCY_SYMBOL + "=?", new String[]{currency.getCurrencyCode()}, null);

                if (cursor != null && cursor.getCount() <= 0) {
                    ContentValues values = new ContentValues();

                    values.put(TableCurrencyFormats.CURRENCYNAME, currency.getDisplayName());
                    values.put(TableCurrencyFormats.CURRENCY_SYMBOL, currency.getCurrencyCode());

                    if (symbols != null && symbols.containsKey(currency.getCurrencyCode())) {
                        values.put(TableCurrencyFormats.PFX_SYMBOL, symbols.get(currency.getCurrencyCode()));
                    }

                    values.put(TableCurrencyFormats.DECIMAL_POINT, ".");
                    values.put(TableCurrencyFormats.GROUP_SEPARATOR, ",");
                    values.put(TableCurrencyFormats.SCALE, 100);
                    values.put(TableCurrencyFormats.BASECONVRATE, 1);

                    cursor.close();

                    // insert and check error
                    if (mContext.getContentResolver().insert(tableCurrencyFormats.getUri(), values) == null)
                        return false;
                }
            } catch (Exception e) {
                Log.e(LOGCAT, e.getMessage());
            }
        }

        return true;
    }

    /**
     * Check if device has connection
     *
     * @return true if is online otherwise false
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Function that determines if the application is running on tablet
     *
     * @return true if running on the tablet, otherwise false
     */
    @SuppressLint("InlinedApi")
    public boolean isTablet() {
        int layout = mContext.getResources().getConfiguration().screenLayout;
        return ((layout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) ||
                ((layout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
    }

    /**
     * Change the application database
     *
     * @param path new database
     * @return indicator whether the operation was successful
     */
    public boolean changeDatabase(String path) {
        File file = new File(path);
        // check if database exists
        if (!file.exists()) {
            Toast.makeText(mContext, R.string.path_database_not_exists, Toast.LENGTH_LONG).show();
            return false;
        }
        // check if database can be open in write mode
        if (!file.canWrite()) {
            Toast.makeText(mContext, R.string.database_can_not_open_write, Toast.LENGTH_LONG).show();
            return false;
        }
        // close connection
        MoneyManagerOpenHelper.getInstance(mContext).close();
        // change database
        MoneyManagerApplication.setDatabasePath(mContext, path);

        return true;
    }

    /**
     * Resolves the id attribute in color
     *
     * @param attr id attribute
     * @return color
     */
    public int resolveColorAttribute(int attr) {
        return mContext.getResources().getColor(resolveIdAttribute(attr));
    }

    /**
     * Resolve the id attribute into int value
     *
     * @param attr id attribute
     * @return resource id
     */
    public int resolveIdAttribute(int attr) {
        TypedValue tv = new TypedValue();
        if (mContext.getTheme().resolveAttribute(attr, tv, true))
            return tv.resourceId;
        else
            return INVALID_ATTRIBUTE;
    }

    /**
     * Update value of info
     *
     * @param info  to be updated
     * @param value value to be used
     * @return true if update success otherwise false
     */
    public boolean setInfoValue(String info, String value) {
        boolean ret;
        TableInfoTable infoTable = new TableInfoTable();
        MoneyManagerOpenHelper helper;
        boolean exists;
        // check if exists info
        exists = !TextUtils.isEmpty(getInfoValue(info));
        // content values
        ContentValues values = new ContentValues();
        values.put(TableInfoTable.INFOVALUE, value);

        try {
            helper = MoneyManagerOpenHelper.getInstance(mContext);
            if (exists) {
                ret = helper.getWritableDatabase().update(infoTable.getSource(), values,
                        TableInfoTable.INFONAME + "=?", new String[]{info}) >= 0;
            } else {
                values.put(TableInfoTable.INFONAME, info);
                ret = helper.getWritableDatabase().insert(infoTable.getSource(), null, values) >= 0;
            }
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
            ret = false;
        }

        return ret;
    }

    public boolean isToDisplayChangelog() {
        int currentVersionCode = getCurrentVersionCode(mContext);
        int lastVersionCode = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getInt(mContext.getString(PreferenceConstants.PREF_LAST_VERSION_KEY), -1);

        return lastVersionCode != currentVersionCode;
    }

    public boolean showChangelog() {
        int currentVersionCode = getCurrentVersionCode(mContext);
        PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                .putInt(mContext.getString(PreferenceConstants.PREF_LAST_VERSION_KEY), currentVersionCode).commit();

        // create layout
        View view = LayoutInflater.from(mContext).inflate(R.layout.changelog_layout, null);
        //create dialog
        AlertDialogWrapper.Builder showDialog = new AlertDialogWrapper.Builder(mContext);
        showDialog.setCancelable(false);
        showDialog.setTitle(R.string.changelog);

        showDialog.setView(view);
        showDialog.setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        // show dialog
        showDialog.create().show();
        return true;
    }

    /**
     * @return preferences account fav visible
     */
    public boolean getAccountFavoriteVisible() {
        return PreferenceManager.getDefaultSharedPreferences(mContext)
                .getBoolean(mContext.getString(PreferenceConstants.PREF_ACCOUNT_FAV_VISIBLE), false);
    }

    /**
     * @return preferences accounts visible
     */
    public boolean getAccountsOpenVisible() {
        return PreferenceManager.getDefaultSharedPreferences(mContext)
                .getBoolean(mContext.getString(PreferenceConstants.PREF_ACCOUNT_OPEN_VISIBLE), false);
    }
}
