package com.money.manager.ex.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RepeatingTransactionReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, RepeatingTransactionService.class);
		context.startService(service);
	}

}
