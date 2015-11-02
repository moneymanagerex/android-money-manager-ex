package org.moneymanagerex.android.tests;

import android.content.Context;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.R;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.DatabaseSettings;
import com.money.manager.ex.transactions.EditCheckingTransactionActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.UnitTestHelper;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for edit transaction activity.
 *
 * Created by Alen Siljak on 25/09/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class EditTransactionTests {

    private Context context;
    private ActivityController<EditCheckingTransactionActivity> controller;
//    private EditCheckingTransactionActivity activity;

    @Before
    public void setUp() {
        this.context = UnitTestHelper.getContext();
        this.controller = UnitTestHelper.getController(EditCheckingTransactionActivity.class);
//        this.activity = UnitTestHelper.getActivity(this.controller);
    }

    @Test
    public void setDbPath() {
        DatabaseSettings dbSettings = new AppSettings(this.context).getDatabaseSettings();

        String dbPath = dbSettings.getDatabasePath();
        assertThat(dbPath).isEmpty();

        // set the db path in settings
        dbPath = "Z:\\Dropbox\\Apps\\MoneyManagerEx Mobile\\dev-db.mmb";
        dbSettings.setDatabasePath(dbPath);

        dbPath = dbSettings.getDatabasePath();
        assertThat(dbPath).isNotEmpty();
    }

    //@Test
    public void useExistingDatabase() {
        setDbPath();

        // FAILS, because FontIcon can't be instantiated by Robolectric.

        EditCheckingTransactionActivity activity = UnitTestHelper.getActivity(this.controller);

        // now create a transaction
        activity.findViewById(R.id.textViewAmount).performClick();
        // ent
    }
}