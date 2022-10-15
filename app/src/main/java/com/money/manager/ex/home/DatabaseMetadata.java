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
package com.money.manager.ex.home;

import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.utils.MmxDate;

import java.io.File;
import java.util.Date;

/**
 * An entry in the recent databases list.
 */
public class DatabaseMetadata {

    public String localPath;
    public boolean isLocalFileChanged;
    /**
     * The timestamp (ISO date/time) when the file was downloaded.
     */
    public String localSnapshotTimestamp;
    // todo sync provider
    // SyncAdapterType
    public String remotePath;
    public String remoteLastChangedDate;

    public String getFileName() {
        if (TextUtils.isEmpty(this.localPath)) return "";

        File file = new File(this.localPath);
        return file.getName();
    }

    public boolean isSynchronised() {
        return !TextUtils.isEmpty(remotePath);
    }

    public void setRemoteLastChangedDate(MmxDate value) {
        if (value == null) {
            this.remoteLastChangedDate = null;
        } else {
            this.remoteLastChangedDate = value.toString(Constants.ISO_8601_FORMAT);
        }
    }
}
