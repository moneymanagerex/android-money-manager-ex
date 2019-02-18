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

package org.moneymanagerex.android.testhelpers;

import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.test.espresso.ViewInteraction;
import androidx.appcompat.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import static org.hamcrest.CoreMatchers.allOf;

/**
 * Borrowed from:
 * https://github.com/designatednerd/Wino/blob/master/app/src/androidTest/java/com/designatednerd/wino/ui/EspressoHelpers.java
 * Created by Alen Siljak on 24/09/2015.
 */
public class EspressoHelpers {

    /**************
     * TEXT ENTRY *
     **************/

    public static void enterTextIntoViewWithHint(String aTextToEnter, @StringRes int aHintResID) {
        onView(withHint(aHintResID)).perform(typeText(aTextToEnter));
    }

    public static void enterTextIntoViewWithID(String aTextToEnter, @IdRes int aViewID) {
        onView(withId(aViewID)).perform(typeText(aTextToEnter));
    }

    /*************
     * SCROLLING *
     *************/

    public static void scrollToViewWithID(@IdRes int aViewIDRes) {
        onView(withId(aViewIDRes)).perform(scrollTo());
    }

    /***********
     * TAPPING *
     ***********/

    public static void tapViewWithText(String aText) {
        onView(withText(aText)).perform(click());
    }

    public static void tapViewWithText(@StringRes int aTextResID) {
        onView(withText(aTextResID)).perform(click());
    }

    public static void tapViewWithID(@IdRes int aViewResID) {
        onView(withId(aViewResID)).perform(click());
    }

    /**********************
     * RECYCLERVIEW STUFF *
     **********************/

    public static Matcher<View> withRecyclerView(@IdRes int viewId) {
        return allOf(isAssignableFrom(RecyclerView.class), withId(viewId));
    }


    //Modified from https://gist.github.com/tommyd3mdi/2622caecc1b2d498cd1a

    /**
     * Allows performing actions on a RecyclerView item with a given title. Mostly useful for
     * situations where all titles are guaranteed to be unique
     * @param aParentRecyclerViewID     The resource ID of the RecyclerView
     * @param aRecyclerViewTextViewID   The resource ID of the text view where the title should be displayed
     * @param title                     The title which should be displayed, as a string
     * @return A ViewInteraction where actions will be performed on a row matching the given parameters.
     */
    public static ViewInteraction onRecyclerItemViewWithTitle(@IdRes int aParentRecyclerViewID,
                                                              @IdRes int aRecyclerViewTextViewID,
                                                              String title) {

        Matcher<View> hasRecyclerViewAsParent = withParent(withRecyclerView(aParentRecyclerViewID));
        Matcher<View> hasChildWithTitleInTextView = withChild(allOf(withId(aRecyclerViewTextViewID), withText(title)));
        Matcher<View> hasChildWithChildWithTitleInTextView = withChild(hasChildWithTitleInTextView);

        return onView(allOf(hasRecyclerViewAsParent,
                hasChildWithChildWithTitleInTextView));

    }

    //Yoink: http://stackoverflow.com/a/30073528/681493
    private static Matcher<View> nthChildOf(final Matcher<View> parentMatcher, final int childPosition) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with " + childPosition + " child view of type parentMatcher");
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view.getParent() instanceof ViewGroup)) {
                    return parentMatcher.matches(view.getParent());
                }

                ViewGroup group = (ViewGroup) view.getParent();
                return parentMatcher.matches(view.getParent()) && group.getChildAt(childPosition).equals(view);
            }
        };
    }

    /**
     * Creates a view interaction on a RecylerView's child at a given position.
     * @param aParentRecyclerViewID The resource ID of the parent recycler view
     * @param aPosition             The index of the subview you wish to examine (ie, the row)
     * @return A ViewInteraction where actions will be performed on the row's parent view at the
     *         given index.
     */
    public static ViewInteraction onRecyclerItemViewAtPosition(@IdRes int aParentRecyclerViewID,
                                                               int aPosition) {


        return onView(nthChildOf(withRecyclerView(aParentRecyclerViewID), aPosition));
    }
}
