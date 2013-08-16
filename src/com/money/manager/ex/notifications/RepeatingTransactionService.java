package com.money.manager.ex.notifications;

import android.app.IntentService;
import android.content.Intent;

public class RepeatingTransactionService extends IntentService {

	public RepeatingTransactionService() {
		super("com.money.manager.ex.notifications.RepeatingTransactionService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// start repeating transaction
		RepeatingTransactionNotifications notifications = new RepeatingTransactionNotifications(getApplicationContext());
		notifications.notifyRepeatingTransaction();
	}
}
