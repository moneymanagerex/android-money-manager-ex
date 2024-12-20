package com.money.manager.ex.errorhandle;

import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class LoggingExceptionHandlerDetail {

    public static String generateReport(final Thread t, final Throwable e) {
        StackTraceElement[] arr = e.getStackTrace();
        final StringBuffer report = new StringBuffer(e.toString());
        final String lineSeperator = "-------------------------------\n\n";
        report.append("\n\n");
        report.append("--------- Stack trace ---------\n\n");
        for (int i = 0; i < arr.length; i++) {
            report.append( "    ");
            report.append(arr[i].toString());
            report.append("\n");
        }
        report.append(lineSeperator);
        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause
        report.append("--------- Cause ---------\n\n");
        Throwable cause = e.getCause();
        if (cause != null) {
            report.append(cause.toString());
            report.append("\n\n");
            arr = cause.getStackTrace();
            for (int i = 0; i < arr.length; i++) {
                report.append("    ");
                report.append(arr[i].toString());
                report.append("\n");
            }
        }
        // Getting the Device brand,model and sdk verion details.
        report.append(lineSeperator);
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
        report.append(lineSeperator);
        report.append("--------- Firmware ---------\n\n");
        report.append("SDK: ");
        report.append(Build.VERSION.SDK);
        report.append("\n");
        report.append("Release: ");
        report.append(Build.VERSION.RELEASE);
        report.append("\n");
        report.append("Incremental: ");
        report.append(Build.VERSION.INCREMENTAL);
        report.append("\n");
        report.append(lineSeperator);

//        Log.e("Report ::", report.toString());

        return  report.toString();
    }
}