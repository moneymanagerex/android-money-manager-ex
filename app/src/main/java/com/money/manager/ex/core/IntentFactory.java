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

package com.money.manager.ex.core;

import android.content.Context;
import android.content.Intent;
import android.os.Messenger;

import com.money.manager.ex.sync.SyncConstants;
import com.money.manager.ex.sync.SyncService;

/**
 * Generates Intents for common app functionality.
 */

public class IntentFactory {
    public static Intent getSyncServiceIntent(Context context, String action, String localFile,
                                              String remoteFile, Messenger messenger) {
        Intent syncServiceIntent = new Intent(context, SyncService.class);

        syncServiceIntent.setAction(action);

        syncServiceIntent.putExtra(SyncConstants.INTENT_EXTRA_LOCAL_FILE, localFile);
        syncServiceIntent.putExtra(SyncConstants.INTENT_EXTRA_REMOTE_FILE, remoteFile);

        if (messenger != null) {
            syncServiceIntent.putExtra(SyncService.INTENT_EXTRA_MESSENGER, messenger);
        }

        return syncServiceIntent;
    }
}
