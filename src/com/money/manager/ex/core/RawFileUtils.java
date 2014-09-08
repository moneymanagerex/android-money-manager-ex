package com.money.manager.ex.core;

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
