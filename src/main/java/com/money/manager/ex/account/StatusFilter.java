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
package com.money.manager.ex.account;

import com.money.manager.ex.core.TransactionStatuses;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the Status filter functionality in the account transactions list.
 */
public class StatusFilter {
    public StatusFilter() {
        this.filter = new ArrayList<>();

        loadAllStatuses();
    }

    public ArrayList<String> filter;

    /**
     * Set the filter to the given status.
     * @param status The status to use as a filter.
     */
    public void setFilter(TransactionStatuses status) {
        setFilter(status.getCode());
    }

    public boolean isEmpty() {
        // If there's nothing, or we have only None status in the filter, consider empty.
        return this.filter.isEmpty() ||
            this.filter.size() == 1 && this.filter.contains(TransactionStatuses.NONE.getCode());
    }

    /**
     * Used when assembling SQL statements.
     * i.e. ('R', 'F')
     * @return statuses as the parameters for the SQL query
     */
    public String getSqlParameters() {
        String result = "(";
        for (String status : this.filter) {
            // append comma if not the first element
            if (this.filter.indexOf(status) > 0) {
                result += ", ";
            }

            result += "'";
            result += status;
            result += "'";
        }
        result += ")";

        return result;
    }

//    /**
//     * Used to indicate which filter is selected in the UI.
//     * @return the name of the currently active filter (if found).
//     */
//    public String getCurrentFilterName() {
//        String none = TransactionStatuses.NONE.name();
//
//        if (this.filter.size() == 0) return none;
//
//        // check single values
//        if (this.filter.size() == 1) {
//            return TransactionStatuses.get(this.filter.get(0)).name();
//        }
//
//        // Check the not-statuses.
//        for (StatusFilterEnum notStatus : StatusFilterEnum.values()) {
//            boolean found = new EqualsBuilder()
//                .append(TransactionStatuses.values(), notStatus.values)
//                .isEquals();
//            if (found) {
//                return notStatus.name();
//            }
//        }
//
//        return none;
//    }

    // Private

    private void loadAllStatuses() {
        for (TransactionStatuses status : TransactionStatuses.values()) {
            this.filter.add(status.getCode());
        }
    }

    private void setFilter(String statusCode) {
        // clear the collection
        this.filter.clear();

        this.filter.add(statusCode);
    }

//    private String[] getAllAvailableFilterNames() {
//        List<String> names = new ArrayList<>();
//
//        for (TransactionStatuses status : TransactionStatuses.values()) {
//            names.add(status.name());
//        }
//        for (StatusFilterEnum notstatus : StatusFilterEnum.values()) {
//            names.add(notstatus.name());
//        }
//
//        String[] result = new String[names.size()];
//        names.toArray(result);
//        return result;
//    }
}
