/*
 * Copyright (C) 2012-2026 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteTransactionListener;
import android.os.CancellationSignal;import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Adapter that bridges androidx.sqlite.db.SupportSQLiteOpenHelper
 * to android.arch.persistence.db.SupportSQLiteOpenHelper required by SqlBrite 3.x
 * without requiring Jetifier.
 */
public class SupportSQLiteOpenHelperAdapter implements android.arch.persistence.db.SupportSQLiteOpenHelper {

    private final androidx.sqlite.db.SupportSQLiteOpenHelper delegate;

    public SupportSQLiteOpenHelperAdapter(@NonNull androidx.sqlite.db.SupportSQLiteOpenHelper delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getDatabaseName() {
        return delegate.getDatabaseName();
    }

    @RequiresApi(api = 16)
    @Override
    public void setWriteAheadLoggingEnabled(boolean enabled) {
        delegate.setWriteAheadLoggingEnabled(enabled);
    }

    @Override
    public android.arch.persistence.db.SupportSQLiteDatabase getWritableDatabase() {
        return new SupportSQLiteDatabaseAdapter(delegate.getWritableDatabase());
    }

    @Override
    public android.arch.persistence.db.SupportSQLiteDatabase getReadableDatabase() {
        return new SupportSQLiteDatabaseAdapter(delegate.getReadableDatabase());
    }

    @Override
    public void close() {
        delegate.close();
    }

    public static class SupportSQLiteDatabaseAdapter implements android.arch.persistence.db.SupportSQLiteDatabase {
        private final androidx.sqlite.db.SupportSQLiteDatabase delegate;

        public SupportSQLiteDatabaseAdapter(@NonNull androidx.sqlite.db.SupportSQLiteDatabase delegate) {
            this.delegate = delegate;
        }

        @Override
        public android.arch.persistence.db.SupportSQLiteStatement compileStatement(String sql) {
            final androidx.sqlite.db.SupportSQLiteStatement statement = delegate.compileStatement(sql);
            return new android.arch.persistence.db.SupportSQLiteStatement() {
                @Override public void execute() { statement.execute(); }
                @Override public int executeUpdateDelete() { return statement.executeUpdateDelete(); }
                @Override public long executeInsert() { return statement.executeInsert(); }
                @Override public long simpleQueryForLong() { return statement.simpleQueryForLong(); }
                @Override public String simpleQueryForString() { return statement.simpleQueryForString(); }
                @Override public void bindNull(int index) { statement.bindNull(index); }
                @Override public void bindLong(int index, long value) { statement.bindLong(index, value); }
                @Override public void bindDouble(int index, double value) { statement.bindDouble(index, value); }
                @Override public void bindString(int index, String value) { statement.bindString(index, value); }
                @Override public void bindBlob(int index, byte[] value) { statement.bindBlob(index, value); }
                @Override public void clearBindings() { statement.clearBindings(); }
                @Override public void close() throws IOException { statement.close(); }
            };
        }

        @Override
        public void beginTransaction() {
            delegate.beginTransaction();
        }

        @Override
        public void beginTransactionNonExclusive() {
            delegate.beginTransactionNonExclusive();
        }

        @Override
        public void beginTransactionWithListener(SQLiteTransactionListener listener) {
            delegate.beginTransactionWithListener(listener);
        }

        @Override
        public void beginTransactionWithListenerNonExclusive(SQLiteTransactionListener listener) {
            delegate.beginTransactionWithListenerNonExclusive(listener);
        }

        @Override
        public void endTransaction() {
            delegate.endTransaction();
        }

        @Override
        public void setTransactionSuccessful() {
            delegate.setTransactionSuccessful();
        }

        @Override
        public boolean inTransaction() {
            delegate.inTransaction();
            return delegate.inTransaction();
        }

        @Override
        public boolean isDbLockedByCurrentThread() {
            return delegate.isDbLockedByCurrentThread();
        }

        @Override
        public boolean yieldIfContendedSafely() {
            return delegate.yieldIfContendedSafely();
        }

        @Override
        public boolean yieldIfContendedSafely(long sleepAfterYieldDelay) {
            return delegate.yieldIfContendedSafely(sleepAfterYieldDelay);
        }

        @Override
        public int getVersion() {
            return delegate.getVersion();
        }

        @Override
        public void setVersion(int version) {
            delegate.setVersion(version);
        }

        @Override
        public long getMaximumSize() {
            return delegate.getMaximumSize();
        }

        @Override
        public long setMaximumSize(long numBytes) {
            return delegate.setMaximumSize(numBytes);
        }

        @Override
        public long getPageSize() {
            return delegate.getPageSize();
        }

        @Override
        public void setPageSize(long pageSize) {
            delegate.setPageSize(pageSize);
        }

        @Override
        public Cursor query(String query) {
            return delegate.query(query);
        }

        @Override
        public Cursor query(String query, Object[] bindArgs) {
            return delegate.query(query, bindArgs);
        }

        @Override
        public Cursor query(final android.arch.persistence.db.SupportSQLiteQuery query) {
            return delegate.query(new androidx.sqlite.db.SupportSQLiteQuery() {
                @Override public String getSql() { return query.getSql(); }
                @Override public void bindTo(androidx.sqlite.db.SupportSQLiteProgram program) {
                    query.bindTo(new android.arch.persistence.db.SupportSQLiteProgram() {
                        @Override public void bindNull(int index) { program.bindNull(index); }
                        @Override public void bindLong(int index, long value) { program.bindLong(index, value); }
                        @Override public void bindDouble(int index, double value) { program.bindDouble(index, value); }
                        @Override public void bindString(int index, String value) { program.bindString(index, value); }
                        @Override public void bindBlob(int index, byte[] value) { program.bindBlob(index, value); }
                        @Override public void clearBindings() { program.clearBindings(); }
                        @Override public void close() throws IOException { program.close(); }
                    });
                }
                @Override public int getArgCount() { return 0; }
            });
        }

        @Override
        public Cursor query(final android.arch.persistence.db.SupportSQLiteQuery query, CancellationSignal cancellationSignal) {
            return delegate.query(new androidx.sqlite.db.SupportSQLiteQuery() {
                @Override public String getSql() { return query.getSql(); }
                @Override public void bindTo(androidx.sqlite.db.SupportSQLiteProgram program) {
                    query.bindTo(new android.arch.persistence.db.SupportSQLiteProgram() {
                        @Override public void bindNull(int index) { program.bindNull(index); }
                        @Override public void bindLong(int index, long value) { program.bindLong(index, value); }
                        @Override public void bindDouble(int index, double value) { program.bindDouble(index, value); }
                        @Override public void bindString(int index, String value) { program.bindString(index, value); }
                        @Override public void bindBlob(int index, byte[] value) { program.bindBlob(index, value); }
                        @Override public void clearBindings() { program.clearBindings(); }
                        @Override public void close() throws IOException { program.close(); }
                    });
                }
                @Override public int getArgCount() { return 0; }
            }, cancellationSignal);
        }

        @Override
        public long insert(String table, int conflictAlgorithm, ContentValues values) throws SQLException {
            return delegate.insert(table, conflictAlgorithm, values);
        }

        @Override
        public int delete(String table, String whereClause, Object[] whereArgs) {
            return delegate.delete(table, whereClause, whereArgs);
        }

        @Override
        public int update(String table, int conflictAlgorithm, ContentValues values, String whereClause, Object[] whereArgs) {
            return delegate.update(table, conflictAlgorithm, values, whereClause, whereArgs);
        }

        @Override
        public void execSQL(String sql) throws SQLException {
            delegate.execSQL(sql);
        }

        @Override
        public void execSQL(String sql, Object[] bindArgs) throws SQLException {
            delegate.execSQL(sql, bindArgs);
        }

        @Override
        public boolean isReadOnly() {
            return delegate.isReadOnly();
        }

        @Override
        public boolean isOpen() {
            return delegate.isOpen();
        }

        @Override
        public boolean needUpgrade(int newVersion) {
            return delegate.needUpgrade(newVersion);
        }

        @Override
        public String getPath() {
            return delegate.getPath();
        }

        @Override
        public void setLocale(Locale locale) {
            delegate.setLocale(locale);
        }

        @Override
        public void setMaxSqlCacheSize(int cacheSize) {
            delegate.setMaxSqlCacheSize(cacheSize);
        }

        @Override
        public void setForeignKeyConstraintsEnabled(boolean enable) {
            delegate.setForeignKeyConstraintsEnabled(enable);
        }

        @Override
        public boolean enableWriteAheadLogging() {
            return delegate.enableWriteAheadLogging();
        }

        @Override
        public void disableWriteAheadLogging() {
            delegate.disableWriteAheadLogging();
        }

        @Override
        public boolean isWriteAheadLoggingEnabled() {
            return delegate.isWriteAheadLoggingEnabled();
        }

        @Override
        public List<Pair<String, String>> getAttachedDbs() {
            return delegate.getAttachedDbs();
        }

        @Override
        public boolean isDatabaseIntegrityOk() {
            return delegate.isDatabaseIntegrityOk();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
