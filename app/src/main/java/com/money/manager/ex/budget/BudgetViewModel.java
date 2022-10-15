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

package com.money.manager.ex.budget;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.money.manager.ex.BR;
import com.money.manager.ex.domainmodel.Budget;

/**
 * An observable view model, used for data binding in Budget Edit.
 */
public class BudgetViewModel
    extends BaseObservable {

    public static BudgetViewModel from(Budget budget) {
        if (budget == null) return null;

        BudgetViewModel model = new BudgetViewModel();

        model.setName(budget.getName());

        return model;
    }

    public String name;
    public int year;
    public int month;

    @Bindable
    public String getName() {
        return name;
    }

    @Bindable
    public String getYear() {
        if (this.year == 0) {
            return "";
        }

        return Integer.toString(this.year);
    }

    @Bindable
    public String getMonth() {
        if (this.month == 0) {
            return "";
        }

        return Integer.toString(this.month);
    }

    public void setName(String value) {
        this.name = value;

        BudgetNameParser parser = new BudgetNameParser();
        this.year = parser.getYear(value);
        this.month = parser.getMonth(value);

        notifyChange();
    }

    public void setYear(int value) {
        this.year = value;
        notifyPropertyChanged(BR.year);

        BudgetNameParser parser = new BudgetNameParser();
        this.name = parser.getName(this.year, this.month);
        notifyPropertyChanged(BR.name);
    }

    public void setMonth(int value) {
        this.month = value;
        notifyPropertyChanged(BR.month);

        BudgetNameParser parser = new BudgetNameParser();
        this.name = parser.getName(this.year, this.month);
        notifyPropertyChanged(BR.name);
    }

    public void saveTo(Budget budget) {
        budget.setName(this.name);
    }
}
