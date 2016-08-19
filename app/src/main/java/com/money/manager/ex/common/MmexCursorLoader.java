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

package com.money.manager.ex.common;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import com.money.manager.ex.log.ExceptionHandler;
import com.money.manager.ex.datalayer.Query;

import timber.log.Timber;

/**
 * The cursor loader with exception handling. It should be used instead of ordinary CursorLoader.
 */
public class MmexCursorLoader
    extends CursorLoader {

    public MmexCursorLoader(Context context) {
        super(context);
    }

    public MmexCursorLoader(Context context, Uri uri, Query query) {
        // String[] projection, String selection, String[] selectionArgs, String sortOrder
        super(context, uri, query.projection, query.selection, query.selectionArgs, query.sort);

    }

    @Override
    public Cursor loadInBackground() {
        try {
            return super.loadInBackground();
        } catch (Exception e) {
            Timber.e(e, "loading data");
        }
        return null;
    }
}
