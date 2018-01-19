/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;

import com.money.manager.ex.R;

/**
 * Assisting the creation of notifications on and after Android 8.
 */

public class NotificationUtils {

    public static final String CHANNEL_ID_DOWNLOADING = "Mmex Downloading";
    public static final String CHANNEL_ID_UPLOADING = "Mmex Uploading";
    public static final String CHANNEL_ID_UPLOAD_COMPLETE = "Mmex Upload Complete";
    public static final String CHANNEL_ID_CONFLICT = "Mmex Sync Conflict";

    public static final String NOTIFICATION_CHANNEL_NAME = "Mmex Notification Channel";

    public static void createNotificationChannel(Context context, String channelId) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return;
        }

//            CharSequence channelName = NOTIFICATION_CHANNEL_NAME;
        //int importance = NotificationManager.IMPORTANCE_LOW;
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        String channelName = context.getString(R.string.app_name);

        NotificationChannel channel = new NotificationChannel(
                channelId, channelName, importance);

        channel.setDescription(NOTIFICATION_CHANNEL_NAME);

        //channel.setSound();

        channel.enableLights(true);
        channel.setLightColor(Color.RED);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        //return channel;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }
}
