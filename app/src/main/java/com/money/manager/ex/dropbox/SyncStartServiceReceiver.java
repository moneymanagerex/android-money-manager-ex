///*
// * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
// *
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU General Public License
// * as published by the Free Software Foundation; either version 3
// * of the License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package com.money.manager.ex.dropbox;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//
//import com.money.manager.ex.MoneyManagerApplication;
//import com.money.manager.ex.sync.SyncConstants;
//import com.money.manager.ex.sync.SyncManager;
//import com.money.manager.ex.sync.SyncService;
//
///**
// * Receiver that gets the scheduled notification to run the synchronization task.
// */
//public class SyncStartServiceReceiver
//    extends BroadcastReceiver {
//
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		SyncManager sync = new SyncManager(context);
//
//        if (!sync.canAutoSync()) return;
//
//		//create intent to launch sync
//		Intent service = new Intent(context, SyncService.class);
//		service.setAction(SyncConstants.INTENT_ACTION_SYNC);
//		service.putExtra(SyncConstants.INTENT_EXTRA_LOCAL_FILE, MoneyManagerApplication.getDatabasePath(context));
//		service.putExtra(SyncConstants.INTENT_EXTRA_REMOTE_FILE, sync.getRemotePath());
//		//start service
//		context.startService(service);
//	}
//
//}
