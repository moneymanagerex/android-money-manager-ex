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
package com.money.manager.ex.domainmodel;

import android.database.Cursor;
import android.database.DatabaseUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.account.AccountStatuses;
import com.money.manager.ex.account.AccountTypes;

import org.parceler.Parcel;

import info.javaperformance.money.Money;

/**
 * Account entity
 */
@Parcel
public class Account
    extends EntityBase {

    public static final String ACCOUNTID = "ACCOUNTID";
    public static final String ACCOUNTNAME = "ACCOUNTNAME";
    public static final String ACCOUNTTYPE = "ACCOUNTTYPE";
    public static final String ACCOUNTNUM = "ACCOUNTNUM";
    public static final String STATUS = "STATUS";
    public static final String NOTES = "NOTES";
    public static final String HELDAT = "HELDAT";
    public static final String WEBSITE = "WEBSITE";
    public static final String CONTACTINFO = "CONTACTINFO";
    public static final String ACCESSINFO = "ACCESSINFO";
    public static final String INITIALBAL = "INITIALBAL";
    public static final String FAVORITEACCT = "FAVORITEACCT";
    public static final String CURRENCYID = "CURRENCYID";

    public static Account from(Cursor c) {
        Account account = new Account();
        account.loadFromCursor(c);
        return account;
    }

    public static Account create(String name, AccountTypes type, AccountStatuses status,
                                 boolean favorite, int currencyId) {
        Account account = new Account();

        account.setName(name);
        account.setType(type);
        account.setStatus(status);
        account.setFavorite(favorite);
        account.setCurrencyId(currencyId);

        return account;
    }

    public Account() {
        super();
    }

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        // Reload all money values.
        DatabaseUtils.cursorDoubleToContentValuesIfPresent(c, this.contentValues, Account.INITIALBAL);
    }

    public Integer getId() {
        return getInt(Account.ACCOUNTID);
    }

    public void setId(Integer value) {
        setInt(Account.ACCOUNTID, value);
    }

    public Integer getCurrencyId() {
        return getInt(Account.CURRENCYID);
    }

    public void setCurrencyId(Integer currencyId) {
        setInt(Account.CURRENCYID, currencyId);
    }

    public String getName() {
        return getString(Account.ACCOUNTNAME);
    }

    public void setName(String value) {
        setString(Account.ACCOUNTNAME, value);
    }

    public AccountTypes getAccountType() {
        String type = getType();
        return AccountTypes.valueOf(type);
    }

    public String getType() {
        return getString(Account.ACCOUNTTYPE);
    }

    public void setType(AccountTypes value) {
        setString(Account.ACCOUNTTYPE, value.name());
    }

    public String getAccountNumber() {
        return getString(Account.ACCOUNTNUM);
    }

    public void setAccountNumber(String value) {
        setString(ACCOUNTNUM, value);
    }

    public String getStatus() {
        return getString(Account.STATUS);
    }

    public void setStatus(AccountStatuses value) {
        setString(Account.STATUS, value.name());
    }

    public String getNotes() {
        return getString(Account.NOTES);
    }

    public String getHeldAt() {
        return getString(Account.HELDAT);
    }

    public String getWebSite() {
        return getString(Account.WEBSITE);
    }

    public String getContactInfo() {
        return getString(Account.CONTACTINFO);
    }

    public String getAccessInfo() {
        return getString(Account.ACCESSINFO);
    }

    public Money getInitialBalance() {
        return getMoney(Account.INITIALBAL);
    }

    public boolean hasInitialBalance() {
        return this.getInitialBalance() != null && !getInitialBalance().equals(Constants.NOT_SET);
    }

    public void setInitialBalance(Money value) {
        setMoney(Account.INITIALBAL, value);
    }

    public Boolean getFavorite() {
        return getBoolean(Account.FAVORITEACCT);
    }

    public void setFavorite(boolean value) {
        setBoolean(Account.FAVORITEACCT, value);
    }
}
