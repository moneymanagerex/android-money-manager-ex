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

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.File;

import timber.log.Timber;

/**
 * Helper class to securely store and retrieve database passwords for encrypted (.emb) database files.
 */
public class SecureDatabasePasswordManager {

    private static final String PREF_FILE_NAME = "secure_db_passwords";
    private static final String PREF_FILE_NAME_FALLBACK = "secure_db_passwords_fallback";
    private static SecureDatabasePasswordManager instance;

    private final Context context;
    private SharedPreferences sharedPreferences;

    private SecureDatabasePasswordManager(Context context) {
        this.context = context.getApplicationContext();
        initSharedPreferences();
    }

    public static synchronized SecureDatabasePasswordManager getInstance(Context context) {
        if (instance == null) {
            instance = new SecureDatabasePasswordManager(context);
        }
        return instance;
    }

    private void initSharedPreferences() {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    PREF_FILE_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Timber.e(e, "Error initializing EncryptedSharedPreferences for database passwords, falling back to standard");
            sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME_FALLBACK, Context.MODE_PRIVATE);
        }
    }

    public synchronized void savePassword(String dbPath, String password) {
        if (TextUtils.isEmpty(dbPath)) return;
        String key = getKeyForPath(dbPath);
        if (TextUtils.isEmpty(password)) {
            sharedPreferences.edit().remove(key).apply();
        } else {
            sharedPreferences.edit().putString(key, password).apply();
        }
    }

    public synchronized String getPassword(String dbPath) {
        if (TextUtils.isEmpty(dbPath)) return "";
        String key = getKeyForPath(dbPath);
        return sharedPreferences.getString(key, "");
    }

    public synchronized void clearPassword(String dbPath) {
        if (TextUtils.isEmpty(dbPath)) return;
        String key = getKeyForPath(dbPath);
        sharedPreferences.edit().remove(key).apply();
    }

    private String getKeyForPath(String dbPath) {
        String normalized = new File(dbPath).getName();
        return "pwd_" + Math.abs(normalized.hashCode());
    }
}
