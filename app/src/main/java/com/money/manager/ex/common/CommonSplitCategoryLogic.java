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

package com.money.manager.ex.common;

import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ISplitTransaction;

import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

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

    /**
     * Adjusts the sign on the amount, required for storage.
     * @return Returns the amount that should be stored in the Split based on its Transaction Type
     * and parent transaction's Transaction Type.
     * Example: User enters 10 for Withdrawal on Withdrawal transaction. The sign will be +1.
     *          User enters 10 for Deposit on Withdrawal transaction. The sign will be -1.
     *          User enters 10 for Withdrawal on Deposit transaction. The sign will be -1.
     *          User enters 10 for Deposit on Deposit transaction. The sign will be +1.
     */
    public static Money getStorageAmount(TransactionTypes parentType, Money amount, ISplitTransaction split) {
        int splitSign = split.getTransactionType(parentType) == parentType
                ? +1
                : -1;

        return amount.multiply(splitSign);
    }

    public static void changeSign(List<ISplitTransaction> splits) {
        if (splits == null || splits.isEmpty()) return;

        for (ISplitTransaction tx : splits) {
            tx.setAmount(tx.getAmount().negate());
        }
    }

    public static boolean validateSumSign(List<ISplitTransaction> splits) {
        if (splits == null || splits.isEmpty()) return true;

        Money sum = MoneyFactory.fromDouble(0);

        for (ISplitTransaction tx : splits) {
            sum = sum.add(tx.getAmount());
        }

        return sum.toDouble() > 0;
    }
}
