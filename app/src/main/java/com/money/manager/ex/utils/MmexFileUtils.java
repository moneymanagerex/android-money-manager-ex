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

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;

import com.money.manager.ex.Constants;
import com.money.manager.ex.log.ExceptionHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * File utilities
 */
public class MmexFileUtils {

    private static final int BUFFER_DIMENSION = 128;
    // https://developer.android.com/reference/android/util/SparseArray.html
    private static SparseArray<String> rawHashMap = new SparseArray<>();

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
                ExceptionHandler handler = new ExceptionHandler(context);
                handler.e(e, "loadRaw");
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        ExceptionHandler handler = new ExceptionHandler(context);
                        handler.e(e, "close byte array");
                    }
                }
            }
        }
        return result;
    }
}
