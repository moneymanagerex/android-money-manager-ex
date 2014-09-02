package com.money.manager.ex.dropbox;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.preferences.PreferencesConstant;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DropboxReceiver extends BroadcastReceiver {
	private static final String LOGCAT = DropboxReceiver.class.getSimpleName();
	// action intents
	public static final String ACTION_START = "com.money.manager.ex.custom.intent.action.START_SERVICE_DROPBOX";
	public static final String ACTION_CANCEL = "com.money.manager.ex.custom.intent.action.CANCEL_SERVICE_DROPBOX";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = "";
		//Log actions
		if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
			action = intent.getAction();
			if (BuildConfig.DEBUG) Log.d(LOGCAT, "Action request: " + action);
		}
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		// compose intent
		Intent i = new Intent(context, DropboxStartServiceReceiver.class);
		PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		// check if cancel pending intent
		if (ACTION_CANCEL.equals(action)) {
			alarmManager.cancel(pending); 
			return;
		}
		// check if connect 
		DropboxHelper dropboxHelper = DropboxHelper.getInstance(context);
		if (dropboxHelper == null || !dropboxHelper.isLinked()) return;
		// take repeat time
		SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(context);
		// get minute
		if (preferenceManager != null) {
			String preferenceMinute = preferenceManager.getString(PreferencesConstant.PREF_DROPBOX_TIMES_REPEAT, "30");
			if (Core.StringUtils.isNumeric(preferenceMinute)) {
				int minute = Integer.parseInt(preferenceMinute);
				if (minute > 0) {
					Calendar cal = Calendar.getInstance();
					// cal.add(Calendar.MINUTE, minute);
					// log
					if (BuildConfig.DEBUG) Log.d(LOGCAT, "Start at: " + new SimpleDateFormat().format(cal.getTime()) + " and repeats every: " + preferenceMinute + " minutes");
					// start service
					alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), minute * 60 * 1000, pending);
				}
			}
		}
	}
}
