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

import android.app.IntentService;
import android.content.Intent;

import com.money.manager.ex.core.IntentFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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

//        android.os.Debug.isDebuggerConnected();
//        android.os.Debug.waitForDebugger();
    }

    private static Subscription delayedSubscription;

    @Override
    protected void onHandleIntent(Intent intent) {
        // Cancel any existing subscriptions.
        unsubscribe();

        String action = intent.getAction();
        if (action.equals(SyncSchedulerBroadcastReceiver.ACTION_STOP)) {
            // do not schedule a sync.
            return;
        }

        // schedule a delayed upload.
        delayedSubscription = Observable.timer(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
//                        Timber.d("complete");
                        unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "scheduled upload");
                    }

                    @Override
                    public void onNext(Long aLong) {
                        // Run sync.
                        upload();
                    }
                });
    }

    private void unsubscribe() {
        if (delayedSubscription == null) return;
        if (delayedSubscription.isUnsubscribed()) return;

        delayedSubscription.unsubscribe();
    }

    private void upload() {
        new SyncManager(getApplicationContext())
                .invokeSyncService(SyncConstants.INTENT_ACTION_UPLOAD);
    }
}
