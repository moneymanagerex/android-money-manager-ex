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

package com.money.manager.ex.sync;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.utils.NotificationUtils;

/**
 * Creates notifications for sync messages.
 */
public class SyncNotificationFactory {
    public SyncNotificationFactory(Context context) {
        this.context = context;
    }

    private Context context;

    public Context getContext() {
        return this.context;
    }

    /**
     * Get the builder of a notification for download
     * @return notification
     */
    public Notification getNotificationForDownload() {
        String channel_id = NotificationUtils.CHANNEL_ID_DOWNLOADING;
        NotificationUtils.createNotificationChannel(getContext(), channel_id);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getContext(), channel_id)
                .setContentTitle(getContext().getString(R.string.sync_notification_title))
                .setAutoCancel(false)
                .setDefaults(Notification.FLAG_FOREGROUND_SERVICE)
                .setContentText(getContext().getString(R.string.sync_downloading))
                //.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_dropbox_dark))
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setColor(getContext().getResources().getColor(R.color.md_primary));

        return notification.build();
    }

    /**
     * Get notification builder for download complete
     */
    public Notification getNotificationDownloadComplete(PendingIntent pendingIntent) {
        String channel_id = "Mmex Download Complete";
        NotificationUtils.createNotificationChannel(getContext(), channel_id);

        // compose notification big view
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getContext().getString(R.string.sync_notification_title));
        inboxStyle.addLine(getContext().getString(R.string.dropbox_file_ready_for_use));
        inboxStyle.addLine(getContext().getString(R.string.dropbox_open_database_downloaded));

        return new NotificationCompat.Builder(getContext(), channel_id)
            .addAction(R.drawable.ic_action_folder_open_dark, getContext().getString(R.string.open_database), pendingIntent)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setContentTitle(getContext().getString(R.string.sync_notification_title))
            .setContentText(getContext().getString(R.string.dropbox_open_database_downloaded))
            ////.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_dropbox_dark))
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setTicker(getContext().getString(R.string.dropbox_file_ready_for_use))
            .setStyle(inboxStyle)
            .setColor(getContext().getResources().getColor(R.color.md_primary))
            .build();
    }

    /**
     * Get the builder of a notification for upload
     */
    public Notification getNotificationUploading() {
        String channel_id = NotificationUtils.CHANNEL_ID_UPLOADING;
        NotificationUtils.createNotificationChannel(getContext(), channel_id);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getContext(), channel_id)
                .setContentTitle(getContext().getString(R.string.sync_notification_title))
                .setAutoCancel(false)
//                .setContentInfo(getContext().getString(R.string.sync_uploading))
                .setContentText(getContext().getString(R.string.sync_uploading))
                //.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_dropbox_dark))
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setColor(getContext().getResources().getColor(R.color.md_primary));

        return notification.build();
    }

    /**
     * Get notification builder for upload complete
     */
    public Notification getNotificationUploadComplete(PendingIntent pendingIntent) {
        // compose notification big view
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getContext().getString(R.string.sync_notification_title));
        inboxStyle.addLine(getContext().getString(R.string.upload_file_complete));

        String channel_id = NotificationUtils.CHANNEL_ID_UPLOAD_COMPLETE;
        NotificationUtils.createNotificationChannel(getContext(), channel_id);

        Notification notification = new NotificationCompat.Builder(getContext(), channel_id)
                //.addAction(R.drawable.ic_action_folder_open_dark, context.getString(R.string.open_database), pendingIntent)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentTitle(getContext().getString(R.string.sync_notification_title))
                .setContentText(getContext().getString(R.string.upload_file_complete))
                //.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_dropbox_dark))
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setStyle(inboxStyle)
                .setTicker(getContext().getString(R.string.upload_file_complete))
                .setColor(getContext().getResources().getColor(R.color.md_primary))
                .build();

        return notification;
    }

    /**
     * Get a notification builder with progress bar
     * @param notification existing builder
     * @param totalBytes   total bytes to transfer
     * @param bytes        bytes transfer
     * @return notification
     */
    public Notification getNotificationBuilderProgress(NotificationCompat.Builder notification, int totalBytes, int bytes) {
        notification.setProgress(totalBytes, bytes, false);
        notification.setContentInfo(String.format("%1dKB/%2dKB", bytes / 1024, totalBytes / 1024));

        return notification.build();
    }

    public Notification getNotificationForConflict() {
        UIHelper uiHelper = new UIHelper(getContext());
//        IconicsDrawable icon = new IconicsDrawable(getContext())
//                .icon(MMXIconFont.Icon.mmx_alert)
//                .color(uiHelper.getSecondaryColor())
//                .sizeDp(Constants.NotificationBigIconSize);

        String channel_id = NotificationUtils.CHANNEL_ID_CONFLICT;
        NotificationUtils.createNotificationChannel(getContext(), channel_id);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getContext(), channel_id)
            .setContentTitle(getContext().getString(R.string.sync_notification_title))
            .setAutoCancel(false)
            .setSubText(getContext().getString(R.string.sync_conflict))
            .setContentText(getContext().getString(R.string.both_files_modified))
//                .setLargeIcon(icon.toBitmap())
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setColor(uiHelper.getToolbarItemColor());

        return notification.build();
    }

}
