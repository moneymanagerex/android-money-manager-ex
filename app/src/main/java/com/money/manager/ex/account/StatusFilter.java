/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

import com.money.manager.ex.R;
import com.money.manager.ex.core.TransactionStatuses;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the Status filter functionality in the account transactions list.
 */
@Parcel
public class StatusFilter {

    public StatusFilter() {
        this.filter = new ArrayList<>();

        loadAllStatuses();
    }

    // fields

    /**
     * The filter collection contains the Status Codes for SQL filter.
     */
    public ArrayList<String> filter;

    // methods

//    protected StatusFilter(Parcel in) {
//        filter = in.createStringArrayList();
//    }

//    public static final Creator<StatusFilter> CREATOR = new Creator<StatusFilter>() {
//        @Override
//        public StatusFilter createFromParcel(Parcel in) {
//            return new StatusFilter(in);
//        }
//
//        @Override
//        public StatusFilter[] newArray(int size) {
//            return new StatusFilter[size];
//        }
//    };

    public void add(String statusName) {
        TransactionStatuses status = TransactionStatuses.from(statusName);
        if (status == null) return;

        this.filter.add(status.getCode());
    }

    public void remove(String statusName) {
        TransactionStatuses status = TransactionStatuses.from(statusName);
        if (status == null) return;

        this.filter.remove(status.getCode());
    }

    public boolean contains(String statusName) {
        TransactionStatuses status = TransactionStatuses.from(statusName);
        if (status == null) return false;

        return this.filter.contains(status.getCode());
    }

    /**
     * This method checks the current statuses by their menu id instead of name, which is
     * localized when using different app language.
     * @param menuId Id of the menu item that represents the status.
     * @return Whether the status is selected.
     */
    public boolean contains(int menuId) {
        // get menu ids for all the statuses included.
        List<Integer> menuIds = new ArrayList<>();
        for(String statusCode : this.filter) {
            if (statusCode.equals(TransactionStatuses.NONE.getCode())) {
                menuIds.add(R.id.menu_none);
            }
            if (statusCode.equals(TransactionStatuses.RECONCILED.getCode())) {
                menuIds.add(R.id.menu_reconciled);
            }
            if (statusCode.equals(TransactionStatuses.VOID.getCode())) {
                menuIds.add(R.id.menu_void);
            }
            if (statusCode.equals(TransactionStatuses.FOLLOWUP.getCode())) {
                menuIds.add(R.id.menu_follow_up);
            }
            if (statusCode.equals(TransactionStatuses.DUPLICATE.getCode())) {
                menuIds.add(R.id.menu_duplicate);
            }
        }
        return menuIds.contains(menuId);
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

    // Private

    private void loadAllStatuses() {
        for (TransactionStatuses status : TransactionStatuses.values()) {
            this.filter.add(status.getCode());
        }
    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        String[] filterArray = new String[this.filter.size()];
//        this.filter.toArray(filterArray);
//        dest.writeStringArray(filterArray);
//    }
}
