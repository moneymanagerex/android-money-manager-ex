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

package com.money.manager.ex.common.events;

import android.view.View;

/**
 * Event for clicking the items in recycler view.
 */
public class ListItemClickedEvent {
    public ListItemClickedEvent(int id, String name, View view) {
        this.id = id;
        this.name = name;
        this.view = view;
    }

    public int id;
    public String name;
    /**
     * Clicked view.
     */
    public View view;
}
