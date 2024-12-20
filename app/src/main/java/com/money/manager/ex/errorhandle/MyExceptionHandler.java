package com.money.manager.ex.errorhandle;


import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.Constants;

import timber.log.Timber;

/**
 * Activity based Exception handler ...
 */
public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {

    public static final String EXTRA_MY_EXCEPTION_HANDLER = "EXTRA_MY_EXCEPTION_HANDLER";
    private final Activity context;
    private final Thread.UncaughtExceptionHandler rootHandler;

    public MyExceptionHandler(Activity context) {
        this.context = context;
        // we should store the current exception handler -- to invoke it for all not handled exceptions ...
        rootHandler = Thread.getDefaultUncaughtExceptionHandler();
        // we replace the exception handler now with us -- we will properly dispatch the exceptions ...
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        // note we can't just open in Android an dialog etc. we have to use Intents here
        // http://stackoverflow.com/questions/13416879/show-a-dialog-in-thread-setdefaultuncaughtexceptionhandler

        Intent registerActivity = new Intent(context, AuthActivity.class);
        registerActivity.setAction("HANDLE_ERROR");
        registerActivity.putExtra("ERROR", MyExceptionHandler.class.getName());
        registerActivity.putExtra(Intent.EXTRA_TEXT, LoggingExceptionHandlerDetail.generateReport(thread,ex));
        registerActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        registerActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(registerActivity);

        // make sure we die, otherwise the app will hang ...
        android.os.Process.killProcess(android.os.Process.myPid());
        // sometimes on older android version killProcess wasn't enough -- strategy pattern should be considered here
        System.exit(0);

    }
}