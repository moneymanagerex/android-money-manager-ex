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

package com.money.manager.ex.dropbox;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.auth.DbxUserAuthRequests;

/**
 * Dropbox Helper for API v2.
 * Not used yet. Trying out CloudRail.
 */
public class DropboxHelper2 {
    public DropboxHelper2() {
        //DbxRequestConfig config = new DbxRequestConfig("");
        //DbxClientV2 client = new DbxClientV2(config, );
    }

    private DbxClientV2 mClient;

    public void authenticate() {
        DbxUserAuthRequests requests = mClient.auth();
    }

    public void upload() {

    }

    public void download() {

    }
}
