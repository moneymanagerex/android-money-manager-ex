package com.money.manager.ex.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.utils.MmxDatabaseUtils;
import com.nononsenseapps.filepicker.FilePickerActivity;

/**
 * Various methods that assist with the UI Android requirements.
 */
public class UIHelper {

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, int stringResourceId) {
        String message = context.getString(stringResourceId);
        showToast(context, message);
    }

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

//    /**
//     * Pick a database file using built-in file picker.
//     * @param locationPath ?
//     */
//    public void pickFileDialog(String locationPath, int requestCode) {
//        // root path should be the internal storage?
//        String root = Environment.getExternalStorageDirectory().getPath();
//
//        new MaterialFilePicker()
//                .withActivity((Activity) getContext())
//                .withRequestCode(requestCode)
//                .withRootPath(root)
//                .withPath(locationPath)
//                .withFilter(Pattern.compile(".*\\.mmb$"))
//                //.withFilterDirectories()
//                .withHiddenFiles(true)
//                .start();
//
//        // continues in onActivityResult in the parent activity
//    }

    public static void pickFileDialog(Activity activity, String location, int requestCode) {
// This always works
        Intent i = new Intent(activity, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, location);
        // Environment.getExternalStorageDirectory().getPath()

        activity.startActivityForResult(i, requestCode);
    }

    public static String getSelectedFile(Intent data) {
        if (data == null) return null;

//        ArrayList<String> files = data.getStringArrayListExtra(FilePickerActivity.EXTRA_PATHS);
//        if (files == null || files.size() == 0) return;
        // files.get(0)

//        return data.getStringExtra(FilePickerActivity.EXTRA_PATHS);
        String filePath = data.getData().getPath();

        // check if the db file is valid
        if (!MmxDatabaseUtils.isValidDbFile(filePath)) return null;

//        return data.getData().toString();
        return filePath;
    }
}
