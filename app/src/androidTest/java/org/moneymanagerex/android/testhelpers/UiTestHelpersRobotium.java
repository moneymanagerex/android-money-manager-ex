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

import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.settings.PreferenceConstants;
import com.robotium.solo.Solo;

import timber.log.Timber;

public class UiTestHelpersRobotium {

    public UiTestHelpersRobotium(Solo solo) {
        this.solo = solo;
//        this.host = host;

//        this.context =  host.getInstrumentation().getTargetContext();
//        this.context = solo.getCurrentActivity();
    }

//    private ActivityInstrumentationTestCase2 host;
    public Solo solo;

    public static Solo setUp(ActivityInstrumentationTestCase2 host) {
        host.injectInstrumentation(InstrumentationRegistry.getInstrumentation());

        return new Solo(host.getInstrumentation(), host.getActivity());
    }

    public static void tearDown(Solo solo) {
        if (solo != null) {
            solo.finishOpenedActivities();
        }
    }

    public void clearPreferences(Context context) {
        // clear default preferences
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit();

        clearDropboxPreferences(context);
    }

    public void clearDropboxPreferences(Context context) {
        context.getSharedPreferences(PreferenceConstants.PREF_DROPBOX_ACCOUNT_PREFS_NAME, Context.MODE_PRIVATE)
                .edit().clear().commit();
    }

//    public void clickOnNeutralDialogButton() {
//        // Positive = 1
//        // Negative = 2
//        // Neutral = 3
//        int buttonId = android.R.id.button3;
//        clickOnView(buttonId);
//    }
//
//    public void clickOnPositiveDialogButton() {
//        // Positive = 1
//        int buttonId = android.R.id.button1;
//        clickOnView(buttonId);
//    }

    public void clickOnView(int viewId) {
        solo.clickOnView(solo.getView(viewId));
    }

    public void clickOnMaterialDialogButton(DialogButtons button) {
        int viewId = Constants.NOT_SET;

        switch (button) {
            case POSITIVE:
                viewId = R.id.buttonDefaultPositive;
                break;
            case NEGATIVE:
                viewId = R.id.buttonDefaultNegative;
                break;
            case NEUTRAL:
                viewId = R.id.buttonDefaultNeutral;
                break;
        }
        clickOnView(viewId);
    }

//    public static void clickOnActionBarHomeButton(Solo solo) {
////        View homeView = solo.getView(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.id.home : R.id.home);
////        solo.clickOnView(homeView);
//
////        solo.setNavigationDrawer(Solo.OPENED);
//
////        solo.clickOnScreen(50, 50);
//
//        solo.clickOnActionBarHomeButton();
//    }

    public static void uninstallApp() {
        Runtime rt = Runtime.getRuntime();
        try {
            Process pr = rt.exec("adb uninstall your.package");
            pr.waitFor();
        } catch (Exception e) {
            Timber.e(e, "uninstalling app");
        }
    }

    public void clickOnFloatingButton() {
        solo.clickOnView(solo.getView(R.id.fab));
    }

    public void enterInNumericInput(String value) {
        for (Character character : value.toCharArray()) {
            switch (character) {
                case '.':
                    solo.clickOnView(solo.getView(R.id.buttonKeyNumDecimal));
                    break;
                case '1':
                    solo.clickOnView(solo.getView(R.id.buttonKeyNum1));
                    break;
                case '2':
                    solo.clickOnView(solo.getView(R.id.buttonKeyNum2));
                    break;
                case '3':
                    solo.clickOnView(solo.getView(R.id.buttonKeyNum3));
                    break;
                case '4':
                    solo.clickOnView(solo.getView(R.id.buttonKeyNum4));
                    break;
                case '5':
                    solo.clickOnView(solo.getView(R.id.buttonKeyNum5));
                    break;
                case '6':
                    solo.clickOnView(solo.getView(R.id.buttonKeyNum6));
                    break;
                case '7':
                    solo.clickOnView(solo.getView(R.id.buttonKeyNum7));
                    break;
                case '8':
                    solo.clickOnView(solo.getView(R.id.buttonKeyNum8));
                    break;
                case '9':
                    solo.clickOnView(solo.getView(R.id.buttonKeyNum9));
                    break;
                case '0':
                    solo.clickOnView(solo.getView(R.id.buttonKeyNum0));
                    break;

            }
        }
    }

    public void clickDone() {
        solo.clickOnView(solo.getView(R.id.action_done));
    }

    public void clickCancel() {
        solo.clickOnView(solo.getView(R.id.action_cancel));
    }

    public void openPayeesList() {
        solo.clickOnActionBarHomeButton();
        solo.clickOnText("Entities");
        solo.clickOnText("Payees");
    }
}