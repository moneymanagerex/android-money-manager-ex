/*
 * Copyright (C) 2025 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.crashreport;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.money.manager.ex.core.Core;

/**
 * Activity based Exception handler ...
 */
public class CrashReporter implements Thread.UncaughtExceptionHandler {

    private final Context mContext;
    private final Thread.UncaughtExceptionHandler rootHandler;

    public CrashReporter(Context context) {
        mContext = context;
        // we should store the current exception handler -- to invoke it for all not handled exceptions ...
        rootHandler = Thread.getDefaultUncaughtExceptionHandler();
        // we replace the exception handler now with us -- we will properly dispatch the exceptions ...
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        // note we can't just open in Android an dialog etc. we have to use Intents here
        // http://stackoverflow.com/questions/13416879/show-a-dialog-in-thread-setdefaultuncaughtexceptionhandler

        Intent registerActivity = new Intent(mContext, CrashReportActivity.class);
        registerActivity.setAction("HANDLE_ERROR");
        registerActivity.putExtra("ERROR", CrashReporter.class.getName());
        registerActivity.putExtra(Intent.EXTRA_TEXT, generateReport(mContext, thread, ex));
        registerActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        mContext.startActivity(registerActivity);

        rootHandler.uncaughtException(thread, ex);
//        // make sure we die, otherwise the app will hang ...
//        android.os.Process.killProcess(android.os.Process.myPid());
//        // sometimes on older android version killProcess wasn't enough -- strategy pattern should be considered here
//        System.exit(0);

    }

    public static String generateReport(final Context mContext, final Thread t, final Throwable e) {
        StackTraceElement[] arr = e.getStackTrace();
        final StringBuilder report = new StringBuilder(e.toString());
        final String lineSeparator = "-------------------------------\n\n";
        report.append("\n\n");
        report.append("--------- Stack trace ---------\n\n");
        for (StackTraceElement traceElement : arr) {
            report.append("    ");
            report.append(traceElement.toString());
            report.append("\n");
        }
        report.append(lineSeparator);
        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report.append("--------- Cause ---------\n\n");
        Throwable cause = e.getCause();
        if (cause != null) {
            report.append(cause);
            report.append("\n\n");
            arr = cause.getStackTrace();
            for (StackTraceElement stackTraceElement : arr) {
                report.append("    ");
                report.append(stackTraceElement.toString());
                report.append("\n");
            }
        }
        // Getting the App version
        report.append(lineSeparator);
        report.append("--------- APP VERSION ---------\n\n");
        try {
            Core core = new Core(mContext);
            String version = core.getAppVersionName();
            report.append("App version: ");
            report.append(version);
            report.append("\n");
            int build = core.getAppVersionCode();
            report.append("Build: ");
            report.append(build);
            report.append("\n");
        } catch ( Exception ex) {
            report.append("error while get App Version");
            report.append("\n");
        }

        // Getting the Device brand,model and sdk version details.
        report.append(lineSeparator);
        report.append("--------- Device ---------\n\n");
        report.append("Brand: ");
        report.append(Build.BRAND);
        report.append("\n");
        report.append("Device: ");
        report.append(Build.DEVICE);
        report.append("\n");
        report.append("Model: ");
        report.append(Build.MODEL);
        report.append("\n");
        report.append("Id: ");
        report.append(Build.ID);
        report.append("\n");
        report.append("Product: ");
        report.append(Build.PRODUCT);
        report.append("\n");
        report.append(lineSeparator);
        report.append("--------- Firmware ---------\n\n");
        report.append("SDK: ");
        report.append(Build.VERSION.SDK_INT);
        report.append("\n");
        report.append("Release: ");
        report.append(Build.VERSION.RELEASE);
        report.append("\n");
        report.append("Incremental: ");
        report.append(Build.VERSION.INCREMENTAL);
        report.append("\n");
        report.append(lineSeparator);
        return  report.toString();
    }
}