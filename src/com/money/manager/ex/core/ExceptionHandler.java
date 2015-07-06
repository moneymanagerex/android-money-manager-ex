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
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

/**
 * Standard exception handler.
 */
public class ExceptionHandler
        implements Thread.UncaughtExceptionHandler {

    public ExceptionHandler(Context context) {
        mContext = context;
        mHost = context;
    }

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

    private final String LINE_SEPARATOR = "\n";
    private Context mContext;
    private Object mHost;

    public void handle(Exception ex, String errorMessage) {
        errorMessage = "Error " + errorMessage;

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

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        StringWriter stackTrace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTrace));

        StringBuilder errorReport = new StringBuilder();
        errorReport.append("************ APP DETAILS ************\n\n");
        String version = getAppVersionInformation();
        errorReport.append(version);
        errorReport.append(LINE_SEPARATOR);

        errorReport.append("************ CAUSE OF ERROR ************\n\n");
        errorReport.append(stackTrace.toString());

        errorReport.append("\n************ DEVICE INFORMATION ***********\n");
        errorReport.append("Brand: ");
        errorReport.append(Build.BRAND);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Device: ");
        errorReport.append(Build.DEVICE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Model: ");
        errorReport.append(Build.MODEL);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Id: ");
        errorReport.append(Build.ID);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Product: ");
        errorReport.append(Build.PRODUCT);
        errorReport.append(LINE_SEPARATOR);

        errorReport.append("\n************ FIRMWARE ************\n");
        errorReport.append("SDK: ");
        errorReport.append(Build.VERSION.SDK);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Release: ");
        errorReport.append(Build.VERSION.RELEASE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Incremental: ");
        errorReport.append(Build.VERSION.INCREMENTAL);
        errorReport.append(LINE_SEPARATOR);

//        Intent intent = new Intent(mContext, ExceptionHandlerActivity.class);
//        intent.putExtra("error", errorReport.toString());
//        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
//        mContext.startActivity(intent);

//        Log.e(getLogcat(), errorReport.toString());
//        showMessage(errorReport.toString());

//        Intent intent = new Intent ();
//        intent.setAction ("com.mydomain.SEND_LOG"); // see step 5.
//        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
//        mContext.startActivity(intent);

        sendEmail(errorReport.toString());

//        android.os.Process.killProcess(android.os.Process.myPid());
//        System.exit(10);
        System.exit(1);
    }

    private void sendEmail(String text) {
        Intent intent = new Intent (Intent.ACTION_SEND);
        intent.setType ("plain/text");
        intent.putExtra (Intent.EXTRA_EMAIL, new String[] {"android.money.manager.ex@gmail.com"});
        intent.putExtra (Intent.EXTRA_SUBJECT, "Unexpected Exception Log");
//        intent.putExtra (Intent.EXTRA_STREAM, Uri.parse("file://" + fullName));
        intent.putExtra (Intent.EXTRA_TEXT, text); // do this so some email clients don't complain about empty body.
        // Title for the app selector
//        intent.putExtra(Intent.EXTRA_TITLE, "The app has crashed");
//        mContext.startActivity(intent);

        Intent chooser = Intent.createChooser(intent, mContext.getString(R.string.unhandled_crash));
        mContext.startActivity(chooser);
    }

    private String getAppVersionInformation() {
        String result = "";
        try {
            String version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            result += "Version: " + version;
            result += LINE_SEPARATOR;

            String build = Integer.toString(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode);
            result += "Build: " + build;
        } catch (Exception ex) {
            result = "Could not retrieve version information.";
        }
        return result;
    }
}
