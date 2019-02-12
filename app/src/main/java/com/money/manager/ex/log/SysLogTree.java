package com.money.manager.ex.log;

import android.util.Log;

import timber.log.Timber;

/**
 * Write the messages to the system log.
 */
public class SysLogTree
        extends Timber.DebugTree {
    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        Log.println(priority, tag, message);
    }
}
