package com.money.manager.ex.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Various file utilities.
 */
public class MyFileUtils {

    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    public static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    public MyFileUtils(Context context) {
        this.context = context;
    }

    private Context context;

    public Context getContext() {
        return this.context;
    }

    /**
     * Dangerous permissions have to be requested at runtime as of API 23 (Android M, 6).
     */
    public void requestExternalStoragePermissions(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // Read external storage available only as of API 16.
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, activity,
                PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }

        // Check write permission.
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, activity,
            PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    private void requestPermission(String permission, Activity activity, int requestId) {
        int allowed = ContextCompat.checkSelfPermission(context, permission);

        if (allowed != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{ permission }, requestId);
        }
        /*
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            // todo: show explanation?
        } else {
            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(activity, new String[]{ permission },
                PERMISSION_REQUEST_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
        */
    }
}
