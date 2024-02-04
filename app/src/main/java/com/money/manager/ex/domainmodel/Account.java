/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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
import com.money.manager.ex.utils.MmxDate;

import org.parceler.Parcel;

import java.util.Date;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

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
    public static final String INITIALDATE = "INITIALDATE";

    public Account() {
    }

    public static Account from(final Cursor c) {
        final Account account = new Account();
        account.loadFromCursor(c);
        return account;
    }

    public static Account create(final String name, final AccountTypes type, final AccountStatuses status,
                                 final boolean favorite, final int currencyId) {
        final Account account = new Account();

        account.setName(name);
        account.setType(type);
        account.setStatus(status);
        account.setFavorite(favorite);
        account.setCurrencyId(currencyId);
        // defaults
        account.setId(Constants.NOT_SET);
        account.setInitialBalance(MoneyFactory.fromDouble(0));
        account.setInitialDate(new MmxDate().toDate());

        return account;
    }

    @Override
    public void loadFromCursor(final Cursor c) {
        super.loadFromCursor(c);

        // Reload all money values.
        DatabaseUtils.cursorDoubleToContentValuesIfPresent(c, contentValues, INITIALBAL);
    }

    public Integer getId() {
        return getInt(ACCOUNTID);
    }

    public void setId(final Integer value) {
        setInt(ACCOUNTID, value);
    }

    public Integer getCurrencyId() {
        return getInt(CURRENCYID);
    }

    public void setCurrencyId(final Integer currencyId) {
        setInt(CURRENCYID, currencyId);
    }

    public String getName() {
        return getString(ACCOUNTNAME);
    }

    public void setName(final String value) {
        setString(ACCOUNTNAME, value);
    }

    public String getTypeName() {
        return getString(ACCOUNTTYPE);
    }

    public AccountTypes getType() {
        final String typeName = getTypeName();
        return AccountTypes.get(typeName);
    }

    public void setType(final AccountTypes value) {
        setString(ACCOUNTTYPE, value.title);
    }

    public String getAccountNumber() {
        return getString(ACCOUNTNUM);
    }

    public void setAccountNumber(final String value) {
        setString(ACCOUNTNUM, value);
    }

    public String getStatus() {
        return getString(STATUS);
    }

    public void setStatus(final AccountStatuses value) {
        setString(STATUS, value.title);
    }

    public String getNotes() {
        return getString(NOTES);
    }

    public void setNotes(final String value) {
        setString(NOTES, value);
    }

    public String getHeldAt() {
        return getString(HELDAT);
    }

    public void setHeldAt(final String value) {
        setString(HELDAT, value);
    }

    public String getContactInfo() {
        return getString(CONTACTINFO);
    }

    public void setContactInfo(final String value) {
        setString(CONTACTINFO, value);
    }

    public String getAccessInfo() {
        return getString(ACCESSINFO);
    }

    public void setAccessInfo(final String value) {
        setString(ACCESSINFO, value);
    }

    public Money getInitialBalance() {
        return getMoney(INITIALBAL);
    }

    public void setInitialBalance(final Money value) {
        setMoney(INITIALBAL, value);
    }

    public boolean hasInitialBalance() {
        return null != this.getInitialBalance() && !getInitialBalance().equals(Constants.NOT_SET);
    }

    public Boolean getFavorite() {
        return getBoolean(FAVORITEACCT);
    }

    public void setFavorite(final boolean value) {
        setBoolean(FAVORITEACCT, value);
    }

    public String getWebSite() {
        return getString(WEBSITE);
    }

    public void setWebSite(final String value) {
        setString(WEBSITE, value);
    }

    public Date getInitialDate() {
        final String dateString = getString(INITIALDATE);
        return null != dateString
                ? new MmxDate(dateString).toDate()
                : null;
    }

    public void setInitialDate(final Date date) {
        final String dateString = new MmxDate(date).toIsoDateString();
        setString(INITIALDATE, dateString);
    }
}
