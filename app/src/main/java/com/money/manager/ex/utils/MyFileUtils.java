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

    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    public MyFileUtils(Context context) {
        this.context = context;
    }

    private Context context;

    public Context getContext() {
        return this.context;
    }

    /**
     * Dangerous permissions have to be requested at runtime as of API 23 (Android M, 6).
     * @return boolean indicating whether the request permission dialog is displayed and should be
     * handled asynchronously.
     */
    public boolean requestExternalStoragePermissions(Activity activity) {
        boolean requestingRead = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // Read external storage available only as of API 16.
            requestingRead = requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                activity, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
        }

        // Check write permission.
        boolean requestingWrite = requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            activity, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);

        return requestingRead && requestingWrite;
    }

    private boolean requestPermission(String permission, Activity activity, int requestId) {
        boolean requesting = false;
        int permissionResult = ContextCompat.checkSelfPermission(context, permission);

        if (permissionResult != PackageManager.PERMISSION_GRANTED) {
            requesting = true;
            ActivityCompat.requestPermissions(activity, new String[]{ permission }, requestId);
        }

        return requesting;

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
