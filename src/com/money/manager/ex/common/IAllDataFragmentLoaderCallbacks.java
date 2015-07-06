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
package com.money.manager.ex.common;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;

/**
 * Interface for callback fragment
 */
public interface IAllDataFragmentLoaderCallbacks {
    void onCallbackCreateLoader(int id, Bundle args);

    void onCallbackLoaderFinished(Loader<Cursor> loader, Cursor data);

    void onCallbackLoaderReset(Loader<Cursor> loader);
}
