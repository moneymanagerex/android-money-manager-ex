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
 * Class used to manage the database synchronization process.
 * Currently forwards the calls to the Dropbox Helper.
 */
public class SyncManager {
    public static void disableAutoUpload() {
        // todo: DropboxHelper.setAutoUploadDisabled(false);
    }

    public static void enableAutoUpload() {
        // todo: DropboxHelper.setAutoUploadDisabled(true);
    }

    public static void dataChanged() {
        // todo: DropboxHelper.notifyDataChanged();
    }

    public static void synchronize() {
        // todo:
//        DropboxManager dropbox = new DropboxManager(mContext, mDropboxHelper);
//        dropbox.synchronizeDropbox();

    }

    public static boolean isInSync() {
        // todo: return mDropboxHelper.checkIfFileIsSync();
        return false;
    }
}
