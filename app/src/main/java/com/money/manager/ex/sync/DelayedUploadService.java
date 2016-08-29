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

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import org.joda.time.DateTime;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * The service that executes a delayed db upload.
 */

public class DelayedUploadService
    extends IntentService {

    public DelayedUploadService() {
        super("com.money.manager.ex.sync.DelayedUploadService");

    }

//    private static Subscription delayedSubscription;

    @Override
    protected void onHandleIntent(Intent intent) {
        // validation.
        if (intent == null) return;

        // Cancel any existing subscriptions.
//        unsubscribe();

        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) return;

        if (action.equals(SyncSchedulerBroadcastReceiver.ACTION_STOP)) {
            // do not schedule a sync.
            return;
        }

        // schedule a delayed upload.
        scheduleUpload();
    }

    /*
        Private
     */

    private AlarmManager getAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

//    /**
//     * Schedules via Rx timer. The drawback is that, if the app is closed, this timer will
//     * never execute.
//     */
//    private void scheduleRx() {
//        delayedSubscription = Observable.timer(30, TimeUnit.SECONDS)
//                .subscribeOn(Schedulers.io())
////                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<Long>() {
//                    @Override
//                    public void onCompleted() {
//                        unsubscribe();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Timber.e(e, "scheduled upload");
//                    }
//
//                    @Override
//                    public void onNext(Long aLong) {
//                        // Run sync.
//                        upload();
//                    }
//                });
//    }

    /**
     * Schedules a system timer for the upload action.
     */
    private void scheduleUpload() {
        Context context = getApplicationContext();

        Intent syncIntent = new Intent(context, SyncBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, SyncConstants.REQUEST_DELAYED_UPLOAD,
                syncIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = getAlarmManager(context);

        // Trigger in 30 seconds from now.
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                DateTime.now().getMillis() + 30000,
                pendingIntent);
    }

//    private void unsubscribe() {
//        if (delayedSubscription == null) return;
//        if (delayedSubscription.isUnsubscribed()) return;
//
//        delayedSubscription.unsubscribe();
//    }

    private void upload() {
        new SyncManager(getApplicationContext())
                .invokeSyncService(SyncConstants.INTENT_ACTION_UPLOAD);
    }
}
