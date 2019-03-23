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
package com.money.manager.ex.log;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Deprecated. Replace with calls to Timber, which uses the new production logger.
 * Default exception handler.
 * This class was used for reporting uncaught exceptions before using Crashlytics.
 */
public class ExceptionHandler
    implements Thread.UncaughtExceptionHandler
{
//    public static void warn(String message) {
//        Log.w("manual", message);
//        Crashlytics.log(message);
//    }

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
//        this.originalHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    private final String LINE_SEPARATOR = "\n";
    private Context mContext;
    private Object mHost;

    public Context getContext() {
        return mContext;
    }

    public void e(Exception ex, String errorMessage) {
        this.e((Throwable) ex, errorMessage);
    }

    public void e(Throwable t, String errorMessage) {
        errorMessage = String.format("Error %s:\n%s", errorMessage, t.getLocalizedMessage());

        String version = getAppVersion() + "." + getAppBuildNumber();
        Log.e(getLogcat(), "version: " + version + ": " + errorMessage + ": " + t.getLocalizedMessage());
        t.printStackTrace();
        showMessage(errorMessage);

        //Crashlytics.getInstance().crash();
//        Crashlytics.logException(t);
    }

    private String getLogcat() {
        if (mHost != null) {
            return mHost.getClass().getSimpleName();
        } else {
            return "unknown";
        }
    }

//    public void showMessage(int resourceId) {
//        showMessage(getContext().getString(resourceId));
//    }

    public void showMessage(final String message) {
        showMessage(message, Toast.LENGTH_SHORT);
    }

    /**
     * Display a toast message.
     * @param message Message text to display.
     * @param length Length of display. See Toast.Long and Toast.Short.
     * reference: http://stackoverflow.com/questions/18705945/android-cant-create-handler-inside-thread-that-has-not-called-looper-prepare
     */
    public void showMessage(final String message, final int length) {
        if (this.mContext == null) return;

        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            public void run() {
                try {
                    Toast.makeText(mContext, message, length).show();
                } catch (Exception e) {
                    Log.e(getLogcat(), "Error showing toast: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        StringWriter stackTrace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTrace));

        StringBuilder errorReport = new StringBuilder();
        errorReport.append("************ FEEDBACK ************\n");
        errorReport.append("Please tell us what happened in the space below. Thank you!");
        errorReport.append(LINE_SEPARATOR);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append(LINE_SEPARATOR);

        errorReport.append("************ APP DETAILS ************\n");
        String version = getAppVersionInformation();
        errorReport.append(version);
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
        errorReport.append(LINE_SEPARATOR);

        errorReport.append("************ CAUSE OF ERROR ************\n");
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

//        Log.e(getLogcat(), errorReport.toString());
//        showMessage(errorReport.toString());

//        Intent intent = new Intent ();
//        intent.setAction ("com.mydomain.SEND_LOG"); // see step 5.
//        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
//        context.startActivity(intent);

        sendEmail(errorReport.toString());

//        this.originalHandler.uncaughtException(thread, throwable);

//        android.os.Process.killProcess(android.os.Process.myPid());
//        System.exit(10);
        System.exit(1);
    }

    // private

    private void sendEmail(String text) {
        Intent intent = new Intent (Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { Constants.EMAIL });
        intent.putExtra(Intent.EXTRA_SUBJECT, "Unexpected Exception Log");
//        intent.putExtra (Intent.EXTRA_STREAM, Uri.parse("file://" + fullName));
        intent.putExtra(Intent.EXTRA_TEXT, text); // do this so some email clients don't complain about empty body.
        // Title for the app selector
//        intent.putExtra(Intent.EXTRA_TITLE, "The app has crashed");
//        context.startActivity(intent);

        Intent chooser = Intent.createChooser(intent, mContext.getString(R.string.unhandled_crash));
        mContext.startActivity(chooser);
    }

    private String getAppVersionInformation() {
        if (getContext() == null) return "";

        String result = "";
        try {
            String version = getAppVersion();
            result += "Version: " + version;
            result += LINE_SEPARATOR;

            String build = getAppBuildNumber();
            result += "Build: " + build;
        } catch (Exception ex) {
            result = "Could not retrieve version information.";
        }
        return result;
    }

    private String getAppVersion() {
        String version;
        try {
            version = getContext().getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (Exception e) {
            version = "can't fetch";
        }
        return version;
    }

    private String getAppBuildNumber() {
        try {
            return Integer.toString(getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            String message = "could not retrieve build number";
//            Crashlytics.log(message);
            return message;
        }
    }
}
