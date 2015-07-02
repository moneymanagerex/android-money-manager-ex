package com.money.manager.ex.budget;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.money.manager.ex.R;
import com.money.manager.ex.account.AccountFragment;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.currency.CurrencyFormatsLoaderListFragment;
import com.money.manager.ex.home.HomeFragment;

public class BudgetsActivity
        extends BaseFragmentActivity {

    private boolean mIsDualPanel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // layout
        setContentView(R.layout.activity_budgets);
//        setContentView(R.layout.main_fragments_activity);

        setSupportActionBar(getToolbar());
        setToolbarStandardAction(getToolbar());
        // enable returning back from toolbar.
        setDisplayHomeAsUpEnabled(true);

        createFragments(savedInstanceState);
    }

    // Menu / toolbar

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_budgets, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createFragments(Bundle savedInstanceState) {
        LinearLayout fragmentDetail = (LinearLayout) findViewById(R.id.fragmentDetail);
        setDualPanel(fragmentDetail != null && fragmentDetail.getVisibility() == View.VISIBLE);

        Core core = new Core(getApplicationContext());

        // show navigation fragment
        BudgetsListFragment fragment = (BudgetsListFragment) getSupportFragmentManager()
                .findFragmentByTag(BudgetsListFragment.class.getSimpleName());
        if (fragment == null) {
            // fragment create
            fragment = new BudgetsListFragment();
            // add to stack
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContent, fragment, BudgetsListFragment.class.getSimpleName())
                    .commit();
        } else {
            if (core.isTablet()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContent, fragment, BudgetsListFragment.class.getSimpleName())
                        .commit();
            }
        }
    }

    public void setDualPanel(boolean mIsDualPanel) {
        this.mIsDualPanel = mIsDualPanel;
    }

}
