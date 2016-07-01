/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
 * The background service that synchronizes the database file.
 */
public class SyncService {
    public static final String INTENT_EXTRA_MESSENGER = "com.money.manager.ex.dropbox.DropboxServiceIntent.MESSENGER";
    public static final Integer INTENT_EXTRA_MESSENGER_NOT_CHANGE = 0x000;
    public static final Integer INTENT_EXTRA_MESSENGER_DOWNLOAD = 0x000A;
    public static final Integer INTENT_EXTRA_MESSENGER_UPLOAD = 0x000B;
    public static final Integer INTENT_EXTRA_MESSENGER_START_DOWNLOAD = 0x000C;
    public static final Integer INTENT_EXTRA_MESSENGER_START_UPLOAD = 0x000D;
    public static final Integer INTENT_EXTRA_MESSENGER_NOT_ON_WIFI = 0x000E;

}
