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
