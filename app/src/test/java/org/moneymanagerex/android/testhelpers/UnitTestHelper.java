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

import android.app.Application;
import android.content.ContentProvider;
import android.content.Intent;

import com.money.manager.ex.MmxContentProvider;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.currency.CurrencyRepository;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.domainmodel.Currency;

import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowLog;

import java.lang.reflect.Field;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * Additionally simplify and standardize certain calls to assist when setting up and running
 * the unit tests.
 */
public class UnitTestHelper {
    public static <T extends AppCompatActivity> ActivityController<T> getController(Class<T> activityClass) {
        return Robolectric.buildActivity(activityClass);
    }

    public static <T extends AppCompatActivity> T getActivity(ActivityController<T> controller) {
        return controller.create().visible().start().get();
    }

    public static Application getContext() {
        return RuntimeEnvironment.application;
    }

//    public static <T extends Activity> T create(Class<T> activityClass) {
//        // standard set of calls until the activity is displayed.
//        return Robolectric.buildActivity(activityClass)
//                .create().visible().start().get();
//
//        // suggested:
//        //                .attach().create().visible().start().resume().get();
//
//        // available methods:
//        // .create().start().resume().visible() - .pause().stop().destroy()
//
//    }

    public static Fragment getFragment(MmxBaseFragmentActivity activity, String fragmentClassName) {
        Fragment fragment = activity.getSupportFragmentManager()
            .findFragmentByTag(fragmentClassName);
        return fragment;
    }

    public static Intent getSelectCategoryResult(int categoryId, String categoryName,
        int subCategId, String subCategoryName) {
        Intent result = new Intent();
        result.putExtra(CategoryListActivity.INTENT_RESULT_CATEGID, categoryId);
        result.putExtra(CategoryListActivity.INTENT_RESULT_CATEGNAME, categoryName);
        result.putExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGID, subCategId);
        result.putExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGNAME, subCategoryName);

        return result;
    }

    /**
     * Initialize the content provider explicitly as it is not executed automatically.
     * Also, call teardownDatabase after each test, in @After.
     * @return initialized Content Provider, if needed.
     */
    public static ContentProvider setupContentProvider() {
        ContentProvider contentProvider = new MmxContentProvider();
//        shadowOf(contentProvider).getContext();
        contentProvider.onCreate();
//        ShadowContentResolver.registerProvider(MmxContentProvider.getAuthority(), contentProvider);

        return contentProvider;
    }

    private static void resetSingleton(Class clazz, String fieldName) {
        Field instance;
        try {
            instance = clazz.getDeclaredField(fieldName);
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reset database helper instance. Use after every test on @After.
     * This may not be necessary any more as open helper is not a singleton.
     */
//    public static void teardownDatabase() {
//        resetSingleton(MmxOpenHelper.class, "mInstance");
//    }

    public static void setupLog() {
        ShadowLog.stream = System.out;
    }

    public static void setDefaultCurrency(String symbol) {
        CurrencyRepository repo = new CurrencyRepository(getContext());
        Currency currency = repo.loadCurrency(symbol);
        int id = currency.getCurrencyId();
        setDefaultCurrency(id);
    }

    public static void setDefaultCurrency(int currencyId) {
        CurrencyService currencyService = new CurrencyService(getContext());
        currencyService.setBaseCurrencyId(currencyId);
    }
}
