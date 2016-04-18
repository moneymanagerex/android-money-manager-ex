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
package org.moneymanagerex.android.testhelpers;

import com.money.manager.ex.database.TableCategory;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher for a Sub/Category row in Categories list.
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

}
