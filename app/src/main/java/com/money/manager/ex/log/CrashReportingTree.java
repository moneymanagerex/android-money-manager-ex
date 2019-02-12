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

//import com.crashlytics.android.Crashlytics;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

/**
 * Logging tree for Production builds. Reports errors to Crashlytics.
 * https://github.com/JakeWharton/timber/blob/master/timber-sample/src/main/java/com/example/timber/ExampleApp.java
 */
@Deprecated
public class CrashReportingTree extends Timber.Tree {
    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return;
        }

//        Crashlytics.log(priority, tag, message);

        if (t != null) {
            if (priority == Log.ERROR) {
//                Crashlytics.logException(t);
            }
//            else if (priority == Log.WARN) {
//                Crashlytics.log.logWarning(t);
//            }
        }

        // also, raise an event for the UI to show the message.
        String uiMessage = message;
        if (t != null) {
            uiMessage = "Error: " + t.getMessage();
        }
        EventBus.getDefault().post(new ErrorRaisedEvent(uiMessage));
    }
}
