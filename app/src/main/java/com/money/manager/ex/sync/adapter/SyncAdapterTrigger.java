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

package com.money.manager.ex.sync.adapter;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;

import static com.money.manager.ex.sync.adapter.SyncAdapterService.ACCOUNT;
import static com.money.manager.ex.sync.adapter.SyncAdapterService.ACCOUNT_TYPE;
import static com.money.manager.ex.sync.adapter.SyncAdapterService.AUTHORITY;

/**
 * Here is the code that can be used to invoke the sync adapter synchronization from within the app.
 */

public class SyncAdapterTrigger {
    // Sync interval constants
//    public static final long SECONDS_PER_MINUTE = 60L;
//    public static final long SYNC_INTERVAL_IN_MINUTES = 60L;
//    public static final long SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE;

    public void schedulePeriodicSync(long interval) {
        Account account = new Account(ACCOUNT, ACCOUNT_TYPE);

        /*
         * Turn on periodic syncing
         */
        ContentResolver.addPeriodicSync(
                account,
                AUTHORITY,
                Bundle.EMPTY,
                interval);

    }

    public void runSync() {
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        Account account = new Account(ACCOUNT, ACCOUNT_TYPE);

        /*
         * Request the sync for the default account, authority, and
         * manual sync settings
         */
        ContentResolver.requestSync(account, AUTHORITY, settingsBundle);

    }
}
