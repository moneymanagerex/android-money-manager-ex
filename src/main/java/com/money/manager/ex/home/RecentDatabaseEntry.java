/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.home;

import java.io.File;

/**
 * An entry in the recent databases list.
 *
 * Created by Alen Siljak on 14/09/2015.
 */
public class RecentDatabaseEntry {

    public static RecentDatabaseEntry getInstance(String filePath, boolean linkedToDropbox, String dropboxFileName) {
        RecentDatabaseEntry entry = new RecentDatabaseEntry();
        entry.filePath = filePath;
        entry.linkedToDropbox = linkedToDropbox;
        entry.dropboxFileName = dropboxFileName;
        return entry;
    }

    public static RecentDatabaseEntry fromPath(String filePath) {
//        File file = new File(filePath);
//        String fileName = file.getName();

        return getInstance(filePath, false, "");
    }

    public String filePath;
    public String dropboxFileName;
    public boolean linkedToDropbox;

    public String getFileName() {
        File file = new File(this.filePath);
        return file.getName();
    }
}
