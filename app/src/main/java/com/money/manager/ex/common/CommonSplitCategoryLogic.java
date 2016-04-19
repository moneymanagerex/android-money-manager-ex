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

package com.money.manager.ex.common;

import com.money.manager.ex.core.TransactionTypes;

import info.javaperformance.money.Money;

/**
 * The logic used by Split Category records (transaction & recurring transaction).
 */
public class CommonSplitCategoryLogic {

    public static TransactionTypes getTransactionType(TransactionTypes parentTransactionType,
                                                      Money amount) {
        TransactionTypes oppositeType = parentTransactionType == TransactionTypes.Withdrawal
                ? TransactionTypes.Deposit
                : TransactionTypes.Withdrawal;

        if (amount.isZero()) {
            return parentTransactionType;
        }

        TransactionTypes splitType = amount.toDouble() < 0
                ? oppositeType
                : parentTransactionType;

        return splitType;
    }
}
