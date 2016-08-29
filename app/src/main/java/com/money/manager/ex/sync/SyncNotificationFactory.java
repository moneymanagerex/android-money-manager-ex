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

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.mmex_icon_font_typeface_library.MMEXIconFont;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.home.MainActivity;

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
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getContext())
                .setContentTitle(getContext().getString(R.string.sync_notification_title))
                .setAutoCancel(false)
                .setDefaults(Notification.FLAG_FOREGROUND_SERVICE)
                .setContentText(getContext().getString(R.string.sync_downloading))
                //.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_dropbox_dark))
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setColor(getContext().getResources().getColor(R.color.md_primary));

        // only for previous version!
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);
            notification.setContentIntent(pendingIntent);
        }

        return notification.build();
    }

    /**
     * Get notification builder for download complete
     */
    public Notification getNotificationDownloadComplete(PendingIntent pendingIntent) {
        // compose notification big view
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getContext().getString(R.string.sync_notification_title));
        inboxStyle.addLine(getContext().getString(R.string.dropbox_file_ready_for_use));
        inboxStyle.addLine(getContext().getString(R.string.dropbox_open_database_downloaded));

        return new NotificationCompat.Builder(getContext())
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
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getContext())
                .setContentTitle(getContext().getString(R.string.sync_notification_title))
                .setAutoCancel(false)
//                .setContentInfo(getContext().getString(R.string.sync_uploading))
                .setContentText(getContext().getString(R.string.sync_uploading))
                //.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_dropbox_dark))
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setColor(getContext().getResources().getColor(R.color.md_primary));

        // only for previous version!
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);
            notification.setContentIntent(pendingIntent);
        }

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

        Notification notification = new NotificationCompat.Builder(getContext())
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
        IconicsDrawable icon = new IconicsDrawable(getContext())
                .icon(MMEXIconFont.Icon.mmx_alert)
                .color(uiHelper.getPrimaryColor())
                .sizeDp(Constants.NotificationBigIconSize);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getContext())
                .setContentTitle(getContext().getString(R.string.sync_notification_title))
                .setAutoCancel(false)
                .setSubText(getContext().getString(R.string.sync_conflict))
//                .setContentInfo(getContext().getString(R.string.sync_uploading))
                .setContentText(getContext().getString(R.string.both_files_modified))
//                .setLargeIcon(icon.toBitmap())
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setColor(uiHelper.getColor(R.color.md_primary));

        return notification.build();
    }

}
