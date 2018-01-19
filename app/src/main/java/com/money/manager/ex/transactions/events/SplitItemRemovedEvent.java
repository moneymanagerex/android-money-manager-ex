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

package com.money.manager.ex.transactions.events;

import com.money.manager.ex.database.ISplitTransaction;

/**
 * Split item removed by the user.
 * Called from the individual split item fragment.
 */
public class SplitItemRemovedEvent {
    public SplitItemRemovedEvent(ISplitTransaction entity) {
        this.entity = entity;
    }

    public ISplitTransaction entity;
}
