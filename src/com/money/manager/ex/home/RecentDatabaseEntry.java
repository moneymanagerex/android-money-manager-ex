/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.home;

/**
 * An entry in the recent databases list.
 *
 * Created by Alen Siljak on 14/09/2015.
 */
public class RecentDatabaseEntry {

    public static RecentDatabaseEntry getInstance(String fileName, boolean linkedToDropbox, String dropboxFileName) {
        RecentDatabaseEntry entry = new RecentDatabaseEntry();
        entry.fileName = fileName;
        entry.linkedToDropbox = linkedToDropbox;
        entry.dropboxFileName = dropboxFileName;
        return entry;
    }

    public String fileName;
    public String dropboxFileName;
    public boolean linkedToDropbox;
}
