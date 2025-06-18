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

import org.parceler.Parcel;

/**
 * Represents a Budget.
 * Table: budgetyear_v1
 */
@Parcel
public class Budget
    extends EntityBase {

    public static final String BUDGETYEARID = "BUDGETYEARID";
    public static final String BUDGETYEARNAME = "BUDGETYEARNAME";

    public Budget() { }

    @Override
    public String getPrimaryKeyColumn() {
        return BUDGETYEARID;  // This returns the column name
    }

    public String getName() {
        return getString(BUDGETYEARNAME);
    }

    public void setName(String value) {
        setString(BUDGETYEARNAME, value);
    }

    public boolean isMonthlyBudget() {
        return getName().contains("-");
    }

    public static boolean isMonthlyBudget(String budgetName) {
        return budgetName.contains("-");
    }

    /**
     * @return return year of budget
     */
    public int getYear() {
        String[] parts = getName().split("-");
        return Integer.parseInt(parts[0]);
    }

    /**
     * @return return month of budget if is monthly budget or 0 otherwise
     */
    public int getMonth() {
        if (isMonthlyBudget()) {
            String[] parts = getName().split("-");
            return Integer.parseInt(parts[1]);
        } else {
            return 0;
        }
    }

}
