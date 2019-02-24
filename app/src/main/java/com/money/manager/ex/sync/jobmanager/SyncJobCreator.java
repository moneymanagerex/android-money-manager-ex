/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.sync.jobmanager;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.money.manager.ex.sync.SyncConstants;

/**
 * Synchronization job creator for use with JobManager.
 */

public class SyncJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag) {
            case SyncConstants.INTENT_ACTION_SYNC:
                return new SyncJob();
            case SyncConstants.INTENT_ACTION_DOWNLOAD:
                return null;
            case SyncConstants.INTENT_ACTION_UPLOAD:
                return null;
            default:
                return null;
        }
    }
}
