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
 * Message codes implemented as an enum to use in switch statements.
 */

public enum SyncServiceMessage {
    FILE_NOT_CHANGED(0x000),
    DOWNLOAD_COMPLETE(0x000A),
    UPLOAD_COMPLETE(0x000B),
    STARTING_DOWNLOAD(0x000C),
    STARTING_UPLOAD(0x000D),
    NOT_ON_WIFI(0x000E),
    ERROR(0x000F),
    SYNC_DISABLED(1),
    CONFLICT(2);

    SyncServiceMessage(int value) {
        code = value;
    }

    public int code;

    public static SyncServiceMessage parse(int code) {
        for (SyncServiceMessage item : SyncServiceMessage.values()) {
            if (item.code == code) return item;
        }
        return null;
    }
}