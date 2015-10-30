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
package com.money.manager.ex.domainmodel;

import android.content.ContentValues;

import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ISplitTransactionsDataset;
import com.money.manager.ex.utils.DateUtils;

import java.util.Date;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Account Transaction entity. Table checkingaccount_v1.
 */
public class AccountTransaction
    extends EntityBase {

    public static final String TRANSID = "TRANSID";

    public static AccountTransaction create(int accountId, int payeeId, TransactionTypes type,
                                            int categoryId, int subCategoryId, Money amount) {
        AccountTransaction tx = new AccountTransaction();

        tx.setAccountId(accountId);
        tx.setPayeeId(payeeId);
        tx.setType(type);
        tx.setCategoryId(categoryId);
        tx.setSubcategoryId(subCategoryId);
        tx.setAmount(amount);

        return tx;
    }

    public AccountTransaction() {
        super();
    }

    public AccountTransaction(ContentValues contentValues) {
        super(contentValues);
    }

    public int getId() {
        return getInt(TRANSID);
    }

    public int getAccountId() {
        return getInt(ISplitTransactionsDataset.ACCOUNTID);
    }

    public void setAccountId(int value) {
        setInteger(ISplitTransactionsDataset.ACCOUNTID, value);
    }

    public Money getAmount() {
        Double amount = getDouble(ISplitTransactionsDataset.TRANSAMOUNT);
        Money result = MoneyFactory.fromDouble(amount);
        return result;
    }

    public void setAmount(Money value) {
        setMoney(ISplitTransactionsDataset.TRANSAMOUNT, value);
    }

    public Money getAmountTo() {
        Double amount = getDouble(ISplitTransactionsDataset.TOTRANSAMOUNT);
        Money result = MoneyFactory.fromDouble(amount);
        return result;
    }

    public int getCategoryId() {
        return getInt(ISplitTransactionsDataset.CATEGID);
    }

    public void setCategoryId(Integer value) {
        setInteger(ISplitTransactionsDataset.CATEGID, value);
    }

    public String getDateAsString() {
        return getString(ISplitTransactionsDataset.TRANSDATE);
    }

    public Date getDate() {
        String dateString = getDateAsString();
        return DateUtils.getDateFromIsoString(dateString);
    }

    public String getNotes() {
        return getString(ISplitTransactionsDataset.NOTES);
    }

    public int getPayeeId() {
        return getInt(ISplitTransactionsDataset.PAYEEID);
    }

    public void setPayeeId(int value) {
        setInteger(ISplitTransactionsDataset.PAYEEID, value);
    }

    public String getStatus() {
        return getString(ISplitTransactionsDataset.STATUS);
    }

    public int getSubcategoryId() {
        return getInt(ISplitTransactionsDataset.SUBCATEGID);
    }

    public void setSubcategoryId(Integer value) {
        setInteger(ISplitTransactionsDataset.SUBCATEGID, value);
    }

    public Integer getToAccountId() {
        return getInt(ISplitTransactionsDataset.TOACCOUNTID);
    }

    public String getTransCode() {
        return getString(ISplitTransactionsDataset.TRANSCODE);
    }

    public String getTransactionNumber() {
        return getString(ISplitTransactionsDataset.TRANSACTIONNUMBER);
    }

    public TransactionTypes getTransType() {
        String code = getTransCode();
        return TransactionTypes.valueOf(code);
    }

    public void setType(TransactionTypes value) {
        setString(ISplitTransactionsDataset.TRANSCODE, value.name());
    }

}
