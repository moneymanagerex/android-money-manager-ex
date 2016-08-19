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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Messenger;

/**
 * Creates a Messenger
 */
public class SyncMessengerFactory {
    public SyncMessengerFactory(Context context) {
        this.context = context;
    }

    private Context context;

    public Context getContext() {
        return this.context;
    }

    public Messenger createMessenger(final ProgressDialog progressDialog, final String remoteFile) {
        // Handler can be used only when running in a Looper.
        if (!(getContext() instanceof Activity)) return null;

        // Messenger handles received messages from the sync service.
        return new Messenger(new SyncServiceMessageHandler(getContext(), progressDialog, remoteFile));
    }
}
