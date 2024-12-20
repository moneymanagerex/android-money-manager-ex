package com.money.manager.ex.errorhandle;


import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Application based Exception Handler, basically a Singleton.
 *
 * As this exception handler is attached in the Android Application,
 * we can't start activities here but we could easily log the errors
 * and send them later ... .
 */
public class LoggingExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final static String TAG = LoggingExceptionHandler.class.getSimpleName();

    private final Context context;
    private final Thread.UncaughtExceptionHandler rootHandler;

    public LoggingExceptionHandler(Context context) {
        this.context = context;
        // we should store the current exception handler -- to invoke it for all not handled exceptions ...
        rootHandler = Thread.getDefaultUncaughtExceptionHandler();
        // we replace the exception handler now with us -- we will properly dispatch the exceptions ...
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        rootHandler.uncaughtException(thread, ex);
    }

}