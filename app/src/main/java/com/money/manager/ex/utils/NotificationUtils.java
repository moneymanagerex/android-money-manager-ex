/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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

import java.lang.reflect.Field;

import timber.log.Timber;

/**
 * Assisting the creation of notifications on and after Android 8.
 */

public class NotificationUtils {

    public static final String CHANNEL_ID_RECURRING = "notification_channel_recurring";

    public static final String CHANNEL_ID_DOWNLOADING = "notification_channel_fileoperation_downloadin";
    public static final String CHANNEL_ID_UPLOADING = "notification_channel_fileoperation_uploadin";
    public static final String CHANNEL_ID_UPLOAD_COMPLETE = "notification_channel_fileoperation_complete";
    public static final String CHANNEL_ID_CONFLICT = "notification_channel_fileoperation_conflict";

    public static void createNotificationChannel(Context context, String channelId) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return;
        }

//            CharSequence channelName = NOTIFICATION_CHANNEL_NAME;
        //int importance = NotificationManager.IMPORTANCE_LOW;
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        // Replace channelName with channelId or similar to resolve issue #1244
        // add language support
        // String channelName = context.getString(R.string.app_name);
        // Retrive dynamic cluster for notification
        String channelName;
        String channelDesc;
        Field resourceField;
        int resourceId;
        try {
            //Get the Name
            resourceField = R.string.class.getDeclaredField(channelId);
            resourceId = resourceField.getInt(resourceField);
            channelName = context.getString(resourceId);
        } catch (Exception e) {
            Timber.e(e, "Unable to found resourceId: ["+channelId+"]");
            channelName = channelId;
        }

        try {
            resourceField = R.string.class.getDeclaredField(channelId+"__description");
            resourceId = resourceField.getInt(resourceField);
            channelDesc = context.getString(resourceId);

        } catch (Exception e) {
            Timber.e(e, "Unable to found resourceId: ["+channelId+"__description]");
            channelDesc = channelName;
        }

        NotificationChannel channel = new NotificationChannel(
                channelId, channelName, importance);

        //  Set notification description based on channel & language
        // channel.setDescription(NOTIFICATION_CHANNEL_NAME);
        channel.setDescription(channelDesc);

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
