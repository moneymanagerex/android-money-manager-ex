/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.money.manager.ex.dropbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.money.manager.ex.MoneyManagerApplication;

public class DropboxStartServiceReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		DropboxHelper dropboxHelper = DropboxHelper.getInstance(context);
		//create intent to launch sync
		Intent service = new Intent(context, DropboxServiceIntent.class);
		service.setAction(DropboxServiceIntent.INTENT_ACTION_SYNC);
		service.putExtra(DropboxServiceIntent.INTENT_EXTRA_LOCAL_FILE, MoneyManagerApplication.getDatabasePath(context));
		service.putExtra(DropboxServiceIntent.INTENT_EXTRA_REMOTE_FILE, dropboxHelper.getLinkedRemoteFile());
		//start service
		context.startService(service);
	}

}
