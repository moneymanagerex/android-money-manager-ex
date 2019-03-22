/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.SparseArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import timber.log.Timber;

/**
 * File utilities
 */
public class MmxFileUtils {

    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    private static final int BUFFER_DIMENSION = 128;
    // https://developer.android.com/reference/android/util/SparseArray.html
    private static SparseArray<String> rawHashMap = new SparseArray<>();

    /**
     * Method that allows you to make a copy of file
     *
     * @param src Source file
     * @param dst Destination file
     * @throws IOException
     */
    public static void copy(File src, File dst) throws IOException {
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
     * @param context application
     * @param resId:  rawid resources
     * @return String: String file
     */
    public static String getRawAsString(Context context, int resId) {
        if (rawHashMap.indexOfKey(resId) >= 0) {
            return rawHashMap.get(resId);
        } else {
            String raw = loadRaw(context, resId);
            if (!TextUtils.isEmpty(raw))
                rawHashMap.put(resId, raw);
            return raw;
        }
    }

    /**
     * @param context application
     * @param resId:  rawid resources
     * @return String: String file
     */
    public static String loadRaw(Context context, int resId) {
        String result = null;
        // take input stream
        InputStream is = context.getResources().openRawResource(resId);
        if (is != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_DIMENSION];
            int numRead = 0;
            try {
                while ((numRead = is.read(buffer)) >= 0) {
                    outputStream.write(buffer, 0, numRead);
                }
                // convert to string
                result = new String(outputStream.toByteArray());
            } catch (IOException e) {
                Timber.e(e, "loadRaw");
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        Timber.e(e, "close byte array");
                    }
                }
            }
        }
        return result;
    }

    // Instance

    public MmxFileUtils(Context context) {
        this.context = context;
    }

    private Context context;

    public Context getContext() {
        return this.context;
    }

    /**
     * Dangerous permissions have to be requested at runtime as of API 23 (Android M, 6).
     * @return boolean indicating whether the request permission binaryDialog is displayed and should be
     * handled asynchronously.
     */
    public boolean requestExternalStoragePermissions(AppCompatActivity activity) {
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

    private boolean requestPermission(String permission, AppCompatActivity activity, int requestId) {
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
