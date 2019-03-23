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

package com.money.manager.ex.view.recycler;

import android.view.ContextMenu;

/**
 * Context menu for recycler view.
 */
public class RecyclerViewContextMenuInfo
    implements ContextMenu.ContextMenuInfo {

    public RecyclerViewContextMenuInfo(int position, long id) {
        this.position = position;
        this.id = id;
    }

    final public int position;
    final public long id;
}