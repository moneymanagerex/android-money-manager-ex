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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.Objects;

/**
 * Standard exception handler.
 */
public class ExceptionHandler {
    /**
     * Basic constructor
     * @param context Context / activity.
     * @param host The class where the exception originates. Used to get the class name
     *             for Logcat.
     */
    public ExceptionHandler(Context context, Object host) {
        mContext = context;
        mHost = host;
    }

    private Context mContext;
    private Object mHost;

    public void handle(Exception ex, String errorMessage) {
        if (TextUtils.isEmpty(errorMessage)) {
            errorMessage = "Error";
        }

        Log.e(getLogcat(), errorMessage + ": " + ex.getLocalizedMessage());
        ex.printStackTrace();
        showMessage(errorMessage);
    }

    private String getLogcat() {
        return mHost.getClass().getSimpleName();
    }

    private void showMessage(final String message) {
        // http://stackoverflow.com/questions/18705945/android-cant-create-handler-inside-thread-that-has-not-called-looper-prepare

        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            public void run() {
//                Toast.makeText(context, "Your message to main thread", Toast.LENGTH_SHORT).show();
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
