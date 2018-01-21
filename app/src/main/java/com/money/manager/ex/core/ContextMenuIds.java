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

package com.money.manager.ex.core;

/**
 * Ids for context menus.
 * For easier handling.
 */
public enum ContextMenuIds {
    EDIT(1),
    DELETE(2),
    COPY(3),
    VIEW_TRANSACTIONS(4),
    DownloadPrice(5),
    EditPrice(6),
    Print(7),
    SaveAsHtml(8),
    Portfolio(9);

    public static ContextMenuIds get(int id) {
        for(ContextMenuIds itemId : ContextMenuIds.values()) {
            if (itemId.getId() == id) return itemId;
        }
        return null;
    }

    ContextMenuIds(int id) {
        this.id = id;
    }

    private int id;

    public int getId() {
        return this.id;
    }
}
