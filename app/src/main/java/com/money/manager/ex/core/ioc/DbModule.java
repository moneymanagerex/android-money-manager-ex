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

package com.money.manager.ex.core.ioc;

import android.app.Application;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;

import com.money.manager.ex.database.MmexOpenHelper;
import com.money.manager.ex.settings.AppSettings;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Module for database access.
 */
@Module
public final class DbModule {
    private MmexOpenHelper instance;

    @Provides SQLiteOpenHelper provideOpenHelper(Application application, AppSettings appSettings) {
//        return MmexOpenHelper.createNewInstance(application);
        // Use the existing singleton instance.
        //return MmexOpenHelper.getInstance(application);

        if (instance == null) {
            instance = MmexOpenHelper.createNewInstance(application);
        } else {
            // See whether to reinitialize
            String currentPath = instance.getDatabaseName();
            String newPath = appSettings.getDatabaseSettings().getDatabasePath();
            if (!currentPath.equals(newPath)) {
                instance.close();
                instance = MmexOpenHelper.createNewInstance(application);
            }
        }
        return instance;
    }

    @Provides SqlBrite provideSqlBrite() {
        return SqlBrite.create(new SqlBrite.Logger() {
            @Override public void log(String message) {
                Timber.tag("Database").v(message);
            }
        });
    }

    @Provides BriteDatabase provideDatabase(SqlBrite sqlBrite, SQLiteOpenHelper helper) {
        BriteDatabase db = sqlBrite.wrapDatabaseHelper(helper, Schedulers.io());
        db.setLoggingEnabled(true);
        return db;
    }
}
