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

package com.money.manager.ex.database;

import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.datalayer.IEntity;
import com.money.manager.ex.domainmodel.Taglink;

import java.util.ArrayList;

import info.javaperformance.money.Money;

/**
 * Common interface for split transactions and recurring splits.
 */
public interface ISplitTransaction
    extends IEntity {

    Long getId();
    void setId(long splitTransId);
    boolean hasId();

    Long getAccountId();
    void setAccountId(long value);

    Money getAmount();
    void setAmount(Money splitTransAmount);

    String getNotes();
    void setNotes(String value);

    Long getCategoryId();
    void setCategoryId(long categoryId);

    TransactionTypes getTransactionType(TransactionTypes parentTransactionType);
    void setTransactionType(TransactionTypes value, TransactionTypes parentTransactionType);

    void setTags(ArrayList<Taglink> tags);
    ArrayList<Taglink> getTags();

    String getTransactionModel();
}
