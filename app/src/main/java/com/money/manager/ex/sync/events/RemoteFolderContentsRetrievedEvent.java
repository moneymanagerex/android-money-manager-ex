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

package com.money.manager.ex.sync.events;

import com.cloudrail.si.types.CloudMetaData;

import java.util.List;

/**
 * Raised when the content of the remote storage folder have been retrieved.
 */
public class RemoteFolderContentsRetrievedEvent {
    public RemoteFolderContentsRetrievedEvent(List<CloudMetaData> items) {
        this.items = items;
    }

    public List<CloudMetaData> items;
}
