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

package com.money.manager.ex.core.ioc;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.database.MmxOpenHelper;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import dagger.Module;
import dagger.Provides;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Module for database access.
 */
@Module
public final class DbModule {

    /**
     * Keeping the open helper reference in the application instance.
     * @param app Instance of application object (context).
     * @return Open Helper (Database) instance.
     */
    @Provides
    MmxOpenHelper provideOpenHelper(MmexApplication app) {
        if (app.openHelperAtomicReference == null) {
            app.initDb(null);
        }
        return app.openHelperAtomicReference.get();
    }

    @Provides SqlBrite provideSqlBrite() {
        return new SqlBrite.Builder().logger(new SqlBrite.Logger() {
            @Override public void log(String message) {
                Timber.tag("Database").v(message);
            }
        }).build();
    }

    @Provides BriteDatabase provideDatabase(SqlBrite sqlBrite, MmxOpenHelper helper) {
        BriteDatabase db = sqlBrite.wrapDatabaseHelper(helper, Schedulers.io());
        db.setLoggingEnabled(true);
        return db;
    }
}
