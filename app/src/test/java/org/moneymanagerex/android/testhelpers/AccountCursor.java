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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.moneymanagerex.android.testhelpers;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

import org.robolectric.fakes.BaseCursor;

/**
 * Fake cursor
 */
public class AccountCursor
        extends BaseCursor {
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public boolean move(final int offset) {
        return false;
    }

    @Override
    public boolean moveToPosition(final int position) {
        return false;
    }

    @Override
    public boolean moveToFirst() {
        return false;
    }

    @Override
    public boolean moveToLast() {
        return false;
    }

    @Override
    public boolean moveToNext() {
        return false;
    }

    @Override
    public boolean moveToPrevious() {
        return false;
    }

    @Override
    public boolean isFirst() {
        return false;
    }

    @Override
    public boolean isLast() {
        return false;
    }

    @Override
    public boolean isBeforeFirst() {
        return false;
    }

    @Override
    public boolean isAfterLast() {
        return false;
    }

    @Override
    public int getColumnIndex(final String columnName) {
        return 0;
    }

    @Override
    public int getColumnIndexOrThrow(final String columnName) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return null;
    }

    @Override
    public String[] getColumnNames() {
        return new String[0];
    }

    @Override
    public int getColumnCount() {
        return 0;
    }

    @Override
    public byte[] getBlob(final int columnIndex) {
        return new byte[0];
    }

    @Override
    public String getString(final int columnIndex) {
        return null;
    }

    @Override
    public void copyStringToBuffer(final int columnIndex, final CharArrayBuffer buffer) {

    }

    @Override
    public short getShort(final int columnIndex) {
        return 0;
    }

    @Override
    public int getInt(final int columnIndex) {
        return 0;
    }

    @Override
    public long getLong(final int columnIndex) {
        return 0;
    }

    @Override
    public float getFloat(final int columnIndex) {
        return 0;
    }

    @Override
    public double getDouble(final int columnIndex) {
        return 0;
    }

    @Override
    public int getType(final int columnIndex) {
        return 0;
    }

    @Override
    public boolean isNull(final int columnIndex) {
        return false;
    }

    @Override
    public void deactivate() {

    }

    @Override
    public boolean requery() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void registerContentObserver(final ContentObserver observer) {

    }

    @Override
    public void unregisterContentObserver(final ContentObserver observer) {

    }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer) {

    }

    @Override
    public void setNotificationUri(final ContentResolver cr, final Uri uri) {

    }

    @Override
    public Uri getNotificationUri() {
        return null;
    }

    @Override
    public boolean getWantsAllOnMoveCalls() {
        return false;
    }

    @Override
    public Bundle getExtras() {
        return null;
    }

    @Override
    public void setExtras(final Bundle extras) {

    }

    @Override
    public Bundle respond(final Bundle extras) {
        return null;
    }
}
