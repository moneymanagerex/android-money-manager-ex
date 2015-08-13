package org.moneymanagerex.android.uitests;

import android.test.ActivityInstrumentationTestCase2;

import com.money.manager.ex.R;
import com.money.manager.ex.home.MainActivity;

public class FirstUiTest
    extends ActivityInstrumentationTestCase2<MainActivity>
{
    public FirstUiTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        getActivity();
    }

    public void testPhoneIconIsDisplayed() {
        // When the phone_icon view is available,
        // check that it is displayed.
//        onView(withId(R.id.icon))
//                .check(matches(isDisplayed()));
    }
}