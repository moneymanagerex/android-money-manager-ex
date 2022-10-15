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
package com.money.manager.ex.core;

/**
 * Transaction Types
 */
public enum TransactionTypes {
    Withdrawal(0),
    Deposit(1),
    Transfer(2);

    TransactionTypes(int i) {
        this.code = i;
    }

    private int code;

    public int getCode() {
        return this.code;
    }

//    public TransactionTypes from(String name) {
//
//    }

    public boolean contains(String name) {
        boolean result = false;

        for (TransactionTypes type : TransactionTypes.values()) {
            if (type.toString().equalsIgnoreCase(name)) {
                result = true;
                break;
            }
        }

        return result;
    }
}
