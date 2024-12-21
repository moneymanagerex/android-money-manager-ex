package com.money.manager.ex.errorhandle;


import android.content.Context;
import android.content.Intent;
import android.os.Build;

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
        registerActivity.putExtra(Intent.EXTRA_TEXT, generateReport(thread,ex));
        registerActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        mContext.startActivity(registerActivity);

//        rootHandler.uncaughtException(thread, ex);

        // make sure we die, otherwise the app will hang ...
        android.os.Process.killProcess(android.os.Process.myPid());
        // sometimes on older android version killProcess wasn't enough -- strategy pattern should be considered here
        System.exit(0);

    }

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
        return  report.toString();
    }

}