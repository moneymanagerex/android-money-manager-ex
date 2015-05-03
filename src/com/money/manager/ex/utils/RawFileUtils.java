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

package com.money.manager.ex.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by Alessandro on 08/09/2014.
 */
public class RawFileUtils {
    private static final String LOGCAT = RawFileUtils.class.getSimpleName();
    private static final int BUFFER_DIMENSION = 128;
    // hash map to optimize application
    private static HashMap<Integer, String> rawHashMap = new HashMap<Integer, String>();

    /**
     * @param context application
     * @param resId:  rawid resources
     * @return String: String file
     */
    public static String getRawAsString(Context context, int resId) {
        if (rawHashMap.containsKey(resId)) {
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
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_DIMENSION];
            int numRead = 0;
            try {
                while ((numRead = is.read(buffer)) >= 0) {
                    baos.write(buffer, 0, numRead);
                }
                // convert to string
                result = new String(baos.toByteArray());
            } catch (IOException e) {
                Log.e(LOGCAT, e.getMessage());
                e.printStackTrace();
            } finally {
                if (baos != null) {
                    try {
                        baos.close();
                    } catch (IOException e) {
                        Log.e(LOGCAT, e.getMessage());
                    }
                }
            }
        }
        return result;
    }
}
