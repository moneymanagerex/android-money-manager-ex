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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.settings.AppSettings;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Main IoC module.
 * Modules provide functionality.
 */

@Module(
    includes = {
        DbModule.class,
        RepositoryModule.class
    }
)
public final class MmxModule {
    private final MmexApplication application;

    public MmxModule(MmexApplication application) {
        this.application = application;
    }

    @Provides @Singleton
    MmexApplication provideApplication() {
        return application;
    }

    @Provides @Singleton
    Context provideAppContext() {
        return application;
    }

    @Provides @Singleton
    SharedPreferences provideSharedPreferences() {
//        return application.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides @Singleton
    AppSettings provideAppSettings(MmexApplication application) {
        return new AppSettings(application);
    }
}
