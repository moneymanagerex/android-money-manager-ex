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
package com.money.manager.ex.domainmodel;

import android.database.Cursor;

import com.money.manager.ex.database.TableAccountList;

import java.math.BigDecimal;

/**
 * Account entity
 * Created by Alen on 5/09/2015.
 */
public class Account
    extends EntityBase {

    public static Account from(Cursor c) {
        Account account = new Account();
        account.loadFromCursor(c);
        return account;
    }

    public Account() {
        super();
    }

    public Integer getId() {
        return getInt(TableAccountList.ACCOUNTID);
    }

    public void setId(Integer value) {
        setInt(TableAccountList.ACCOUNTID, value);
    }

    public Integer getCurrencyId() {
        return getInt(TableAccountList.CURRENCYID);
    }

    public void setCurrencyId(Integer currencyId) {
        setInt(TableAccountList.CURRENCYID, currencyId);
    }

    public String getName() {
        return getString(TableAccountList.ACCOUNTNAME);
    }

    public void setName(String value) {
        setString(TableAccountList.ACCOUNTNAME, value);
    }

    public String getType() {
        return getString(TableAccountList.ACCOUNTTYPE);
    }

    public String getAccountNumber() {
        return getString(TableAccountList.ACCOUNTNUM);
    }

    public String getStatus() {
        return getString(TableAccountList.STATUS);
    }

    public String getNotes() {
        return getString(TableAccountList.NOTES);
    }

    public String getHeldAt() {
        return getString(TableAccountList.HELDAT);
    }

    public String getWebSite() {
        return getString(TableAccountList.WEBSITE);
    }

    public String getContactInfo() {
        return getString(TableAccountList.CONTACTINFO);
    }

    public String getAccessInfo() {
        return getString(TableAccountList.ACCESSINFO);
    }

    public BigDecimal getInitialBalance() {
        return getBigDecimal(TableAccountList.INITIALBAL);
    }

    public String getFavourite() {
        return getString(TableAccountList.FAVORITEACCT);
    }

}
