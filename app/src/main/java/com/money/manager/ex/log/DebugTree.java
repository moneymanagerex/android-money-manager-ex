/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

/**
 * Debug logger that also posts an event for displaying the error to the UI.
 */

public class DebugTree
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
