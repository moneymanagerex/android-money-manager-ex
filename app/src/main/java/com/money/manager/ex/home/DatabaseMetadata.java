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
import android.net.Uri;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.core.database.DatabaseManager;
import com.money.manager.ex.core.docstorage.DocFileMetadata;
import com.money.manager.ex.utils.MmxDate;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import timber.log.Timber;

/**
 * An entry in the recent databases list.
 */
public class DatabaseMetadata {
    private static final String EXT_SYNC_TMP = ".synctmp";
    public static final String HTTPS_PREFIX = "https://";
    public static final String POCKETBASE = "pocketbase";

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
    // tmp path for sync (changed remote file)
    public String localTmpPath;

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
        if (isRemoteSyncServer()) {
            Uri uri = Uri.parse(remotePath);
            StringBuilder provider = new StringBuilder(uri.getScheme())
                    .append("://")
                    .append(uri.getHost());

            if (uri.getPort() != -1 && uri.getPort() != 443) {
                provider.append(':').append(uri.getPort());
            }
            return provider.toString();
        }

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
        if (isRemoteSyncServer()) {
            return new MmxDate();
        }
        DocFileMetadata remote = DocFileMetadata.fromDatabaseMetadata(context, this);
        // This is current dateModified at the remote file.
        return remote.lastModified;
    }

    /**
     * Checks if the local file has been changed since the snapshot.
     * @return true if the local file has changed, false otherwise
     */
    public boolean isLocalFileChanged() {
        Date localModified = this.getLocalFileModifiedDate().toDate();
        // The timestamp when the local file was downloaded.
        if (this.localSnapshotTimestamp == null ) {
            // issue #2615
            // no local snapshot
            Timber.d("Local file modified time: %s, snapshot time: unavailable", localModified.toString());
            return true;
        }
        Date localSnapshot = MmxDate.fromIso8601(this.localSnapshotTimestamp).toDate();

        Timber.d("Local file modified time: %s, snapshot time: %s", localModified.toString(), localSnapshot.toString());

        return localModified.after(localSnapshot);
    }

    /**
     * Checks if the remote file has been changed since the last snapshot.
     * @return true if the remote file has changed, false otherwise
     */
    public boolean isRemoteFileChanged(Context context) {
        if (isRemoteSyncServer()) {
            return true;
        }
        Date remoteModified = this.getRemoteFileModifiedDate(context).toDate();
        // Check if the remote file was modified since fetched.
        Date remoteSnapshot = MmxDate.fromIso8601(this.remoteLastChangedDate).toDate();

        Timber.d("Remote file modified time: %s, snapshot time: %s", remoteModified.toString(), remoteSnapshot.toString());

        return remoteModified.after(remoteSnapshot);
    }

    public boolean isRemoteSyncServer() {
        return !TextUtils.isEmpty(remotePath) && remotePath.startsWith(HTTPS_PREFIX);
    }


    /**
     * Sets the remote server details in the remotePath field.
     * The format is: https://serverType:encodedUser@server[:port]
     * @param serverType The type of sync server (e.g., "pocketbase")
     * @param user The username or email
     * @param server The server address (hostname)
     */
    public void setRemoteServer(String serverType, String user, String server) {
        setRemoteServer(serverType, user, server, null) ;
    }
    /**
     * Sets the remote server details in the remotePath field.
     * The format is: https://serverType:encodedUser@server[:port]
     * @param serverType The type of sync server (e.g., "pocketbase")
     * @param user The username or email
     * @param server The server address (hostname)
     * @param port The port number (optional, use null or -1 if not specified)
     */
    public void setRemoteServer(String serverType, String user, String server, Integer port) {
        StringBuilder remote = new StringBuilder(HTTPS_PREFIX)
                .append(serverType)
                .append(':')
                .append(Uri.encode(user))
                .append('@')
                .append(server);

        if (port != null && port != -1) {
            remote.append(':').append(port);
        }

        this.remotePath = remote.toString();
    }

    /**
     * Extracts the server type from the remote path.
     * @return The server type (e.g., "pocketbase") or an empty string if not applicable.
     */
    public String getRemoteServerType() {
        if (!isRemoteSyncServer()) return "";
        Uri uri = Uri.parse(remotePath);
        String userInfo = uri.getUserInfo();
        if (TextUtils.isEmpty(userInfo)) return "";
        int colonIndex = userInfo.indexOf(':');
        return colonIndex != -1 ? userInfo.substring(0, colonIndex) : userInfo;
    }

    /**
     * Extracts and decodes the username from the remote path.
     * @return The decoded username or an empty string if not applicable.
     */
    public String getRemoteUser() {
        if (!isRemoteSyncServer()) return "";
        Uri uri = Uri.parse(remotePath);
        String userInfo = uri.getUserInfo();
        if (TextUtils.isEmpty(userInfo)) return "";
        int colonIndex = userInfo.indexOf(':');
        return colonIndex != -1 ? Uri.decode(userInfo.substring(colonIndex + 1)) : "";
    }

    /**
     * Gets the full remote URL including port if available.
     * @return The server URL (host:port) or an empty string if not applicable.
     */
    public String getRemoteURL() {
        if (!isRemoteSyncServer()) return "";
        Uri uri = Uri.parse(remotePath);
        String url = uri.getHost();
        if (uri.getPort() != -1) {
            url += ":" + uri.getPort();
        }
        return url;
    }

    /**
     * Gets the remote server hostname.
     * @return The server host or an empty string if not applicable.
     */
    public String getRemoteServer() {
        if (!isRemoteSyncServer()) return "";
        Uri uri = Uri.parse(remotePath);
        return uri.getHost();
    }

    /**
     * Gets the remote server port.
     * @return The port number or -1 if not specified or not applicable.
     */
    public int getRemotePort() {
        if (!isRemoteSyncServer()) return -1;
        Uri uri = Uri.parse(remotePath);
        return uri.getPort();
    }

    public boolean isPocketBase() {
        return POCKETBASE.equals(getRemoteServerType());
    }

    public static DatabaseMetadata fromDocFileMetadata(Context context, DocFileMetadata docFileMetadata) {
        DatabaseMetadata metadata = new DatabaseMetadata();
        metadata.remotePath = docFileMetadata.Uri;
        metadata.remoteLastChangedDate = docFileMetadata.lastModified.toIsoString();

        // Local file will always be the same.
        String localPath = new DatabaseManager(context).getDefaultDatabaseDirectory();
        metadata.localPath = localPath + File.separator + docFileMetadata.Name;
        metadata.localTmpPath = metadata.getTmpFilePath();

        return metadata;
    }

    public String getTmpFilePath() {
        if (TextUtils.isEmpty(this.localTmpPath)) {
            this.localTmpPath = this.localPath + EXT_SYNC_TMP;
        }
        return this.localTmpPath;
    }
}
