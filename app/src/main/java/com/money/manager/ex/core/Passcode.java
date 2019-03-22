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
package com.money.manager.ex.core;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.datalayer.InfoRepositorySql;
import com.money.manager.ex.domainmodel.Info;
import com.money.manager.ex.passcode.SimpleCrypto;
import com.money.manager.ex.servicelayer.InfoService;

import javax.inject.Inject;

import dagger.Lazy;
import timber.log.Timber;

public class Passcode {

    private static final String KEY = "6c2a6f30726b3447747559525162665768412370297c5573342324705b";

    public Passcode(Context context) {
        this.mContext = context.getApplicationContext();

        MmexApplication.getApp().iocComponent.inject(this);
    }

    private Context mContext;
    @Inject Lazy<InfoRepositorySql> infoRepositorySqlLazy;

    public Context getContext() {
        return mContext;
    }

    /**
     * Get decrypt pass-code
     * @return null if not set passcode else passcode
     */
    public String getPasscode() {
        String ret = loadPasscode();
        if (ret != null) {
            // decrypt passcode
            ret = decrypt(ret);
        }
        return ret;
    }

    /**
     * Return true if passcode has set otherwise false
     * @return indicator whether there is a passcode or not.
     */
    public boolean hasPasscode() {
        return !(TextUtils.isEmpty(loadPasscode()));
    }

    /**
     * Set a decrypt pass code
     * @param passcode new pass code
     */
    public boolean setPasscode(String passcode) {
        String encrypted = encrypt(passcode);
        return savePasscode(encrypted);
    }

    public boolean clearPasscode() {
        try {
            return clearPasscode_Internal();
        } catch (Exception ex) {
            Timber.e(ex, "Error clearing passcode");
        }
        return false;
    }

    /*
        Private
     */

    /**
     * Decrypt pass-code.
     * @param s encrypted pass-code
     * @return pass-code
     */
    private String decrypt(String s) {
        String ret = null;
        try {
            if (Build.VERSION.SDK_INT <= 23) {
                ret = SimpleCrypto.decrypt(KEY, s);
            } else {
                return s;
            }
        } catch (Exception e) {
            Timber.e(e, "encrypting passcode");
        }
        return ret;
    }

    /**
     * Encrypt clear pass-code
     * @param s clear pass-code
     * @return encrypted string
     */
    private String encrypt(String s) {
        String ret = null;
        try {
            if (Build.VERSION.SDK_INT <= 23) {
                ret = SimpleCrypto.encrypt(KEY, s);
            } else {
                // todo Encryptor.enc
                return s;
            }
        } catch (Exception e) {
            Timber.e(e, "encrypting passcode");
        }
        return ret;
    }

    private boolean clearPasscode_Internal() {
        InfoRepositorySql repo = infoRepositorySqlLazy.get();
        if (repo.delete(Info.INFONAME + "=?", InfoKeys.PASSCODE) <= 0) {
            Toast.makeText(mContext, R.string.db_delete_failed, Toast.LENGTH_LONG).show();
            return false;
        } else
            return true;
    }

    private String loadPasscode() {
        InfoService service = new InfoService(getContext());
        return service.getInfoValue(InfoKeys.PASSCODE);
    }

    /**
     * Set a passcode into database
     * @param passcode passcode to use
     */
    private boolean savePasscode(String passcode) {
        Info entity = new Info();
        entity.setName(InfoKeys.PASSCODE);
        entity.setValue(passcode);

        InfoRepositorySql repo = infoRepositorySqlLazy.get();

        if (hasPasscode()) {
            if (!repo.update(entity)) {
                Toast.makeText(mContext, R.string.db_update_failed, Toast.LENGTH_LONG).show();
                return false;
            }
        } else {
            // insert
            long id = repo.insert(entity);
            if (id <= 0) {
                Toast.makeText(mContext, R.string.db_insert_failed, Toast.LENGTH_LONG).show();
                return false;
            }
        }

        return true;
    }

}