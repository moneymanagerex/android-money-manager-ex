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

package com.money.manager.ex.notifications;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for sync notification
 */

public class SyncNotificationModel {
    public long number;
    // public List<String> inboxLine = new ArrayList<String>();;
    public List<SyncNotificationModelSingle> notifications = new ArrayList<SyncNotificationModelSingle>();;

    public static class SyncNotificationModelSingle  {
        String inboxLine ;
        String mode;
        long trxId;

        public SyncNotificationModelSingle (String inboxLine, String mode, long trxId) {
            this.inboxLine = inboxLine;
            this.mode = mode;
            this.trxId = trxId;
        }
    }

    public void addNotification(String inboxLine, String mode, Long trxId) {
        notifications.add(new SyncNotificationModelSingle(inboxLine, mode, trxId));
    }

}
