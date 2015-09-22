package com.money.manager.ex.common;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import com.money.manager.ex.core.ExceptionHandler;

/**
 * The cursor loader with exception handling. It should be used instead of ordinary
 * CursorLoader.
 * Created by Alen Siljak on 06/07/2015.
 */
public class MmexCursorLoader
    extends CursorLoader {

    public MmexCursorLoader(Context context) {
        super(context);
    }

    public MmexCursorLoader(Context context, Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);

    }

    @Override
    public Cursor loadInBackground() {
        try {
            return super.loadInBackground();
        } catch (IllegalStateException | SQLiteDiskIOException ex) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
            handler.handle(ex, "loading data in cursor loader");
        } catch (SQLiteDatabaseCorruptException ex) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
            handler.handle(ex, "Your database file is corrupt!");
        }
        return null;
    }
}
