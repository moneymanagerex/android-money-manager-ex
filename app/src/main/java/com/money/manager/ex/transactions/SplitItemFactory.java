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
package com.money.manager.ex.transactions;

import com.money.manager.ex.Constants;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.domainmodel.SplitCategory;
import com.money.manager.ex.domainmodel.SplitRecurringCategory;

import info.javaperformance.money.MoneyFactory;

/**
 * The factory that creates the Split Category entities
 */
public class SplitItemFactory {

    public static ISplitTransaction create(String entityClassName, TransactionTypes parentTransactionType) {
        ISplitTransaction entity;
        String recurringSplitName = SplitRecurringCategory.class.getSimpleName();

        if (entityClassName != null && entityClassName.contains(recurringSplitName)) {
            entity = SplitRecurringCategory.create(Constants.NOT_SET, Constants.NOT_SET,
                    Constants.NOT_SET, parentTransactionType, MoneyFactory.fromDouble(0));
        } else {
            entity = SplitCategory.create(Constants.NOT_SET, Constants.NOT_SET,
                    Constants.NOT_SET, parentTransactionType, MoneyFactory.fromDouble(0));
        }

        return entity;
    }
}
