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

package com.money.manager.ex.sync;

/**
 * Constants used in synchronization.
 */
public class SyncConstants {
    // intent action
    public static final String INTENT_ACTION_SYNC = "com.money.manager.ex.sync.action.SYNC";
    public static final String INTENT_ACTION_DOWNLOAD = "com.money.manager.ex.sync.action.DOWNLOAD";
    public static final String INTENT_ACTION_UPLOAD = "com.money.manager.ex.sync.action.UPLOAD";
    // intent extra
    public static final String INTENT_EXTRA_LOCAL_FILE = "SyncServiceIntent:LocalFile";
    public static final String INTENT_EXTRA_REMOTE_FILE = "SyncServiceIntent:RemoteFile";

    public static final int NOTIFICATION_SYNC_IN_PROGRESS = 0xCCCC;
    public static final int NOTIFICATION_SYNC_OPEN_FILE = 0xDDDD;
    public static final int NOTIFICATION_SYNC_ERROR = 3;

    public static final int REQUEST_PERIODIC_SYNC = 0;
    public static final int REQUEST_DELAYED_SYNC = 1;
}
