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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.moneymanagerex.android.testhelpers;

import android.support.test.espresso.matcher.BoundedMatcher;

import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.TableSubCategory;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher for a Sub/Category row in Categories list.
 *
 * Created by Alen on 27/09/2015.
 */
public class MmexMatchers {
    public static Matcher<TableCategory> hasCategory(final String categoryName){
        return new TypeSafeMatcher<TableCategory>() {
            @Override
            public boolean matchesSafely(TableCategory category) {
                boolean result = category.getCategName().toString().equals(categoryName);
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected "+ categoryName);
            }
        };
    }

//    public static Matcher<TableCategory> hasCategory(final String categoryName){
//        return new BoundedMatcher<TableCategory>() {
//            @Override
//            public boolean matchesSafely(TableCategory category) {
//                return category.getCategName() == categoryName;
//            }
//
//            @Override
//            public void describeTo(Description description) {
//                description.appendText("expected "+ categoryName);
//            }
//        };
//    }

    public static Matcher<TableSubCategory> hasSubCategory(final String subcategoryName){
        return new TypeSafeMatcher<TableSubCategory>() {
            @Override
            public boolean matchesSafely(TableSubCategory item) {
                boolean result = item.getSubCategName().equals(subcategoryName);
                return result;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected "+ subcategoryName);
            }
        };
    }

}
