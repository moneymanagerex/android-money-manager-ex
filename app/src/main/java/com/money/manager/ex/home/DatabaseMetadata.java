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

import android.content.Context;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.core.docstorage.DocFileMetadata;
import com.money.manager.ex.utils.MmxDate;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

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

    public void setRemoteLastChangedDate(MmxDate value) {
        if (value == null) {
            this.remoteLastChangedDate = null;
        } else {
            this.remoteLastChangedDate = value.toString(Constants.ISO_8601_FORMAT);
        }
    }

    public String getRemoteContentProvider() {
        URI uri;
        try {
            uri = new URI(remotePath);
        } catch (URISyntaxException e) {
            return "";
        }
        return uri.getHost();
    }

    /**
     * Reads the date/time when the local database file was last changed.
     * @return The date/time of the last change
     */
    public MmxDate getLocalFileModifiedDate() {
        File localFile = new File(this.localPath);
        long localFileTimestamp = localFile.lastModified();
        return new MmxDate(localFileTimestamp);
    }

    /**
     * Reads the date/time when the remote database file was last changed.
     * @return The date/time of the last change
     */
    public MmxDate getRemoteFileModifiedDate(Context context) {
        DocFileMetadata remote = DocFileMetadata.fromDatabaseMetadata(context, this);
        // This is current dateModified at the remote file.
        return remote.lastModified;
    }
}
