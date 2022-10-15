package com.money.manager.ex.log;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

/**
 * Displays all messages above certain level to the screen.
 */
public class ScreenTree
        extends Timber.DebugTree {
    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        //int minLevel = Log.WARN;
        int minLevel = Log.INFO; // Show all messages

        if (priority >= minLevel) {
            String uiMessage = message.split("\\n")[0];
            if (t != null) {
                String exceptionMessage = t.getMessage();
                if (exceptionMessage != null) uiMessage = "Error: " + exceptionMessage;
            }
            // send to UI if there are any subscribers.
            if (EventBus.getDefault().hasSubscriberForEvent(ErrorRaisedEvent.class)) {
                EventBus.getDefault().post(new ErrorRaisedEvent(uiMessage));
            }
        }

        super.log(priority, tag, message, t);
    }
}
