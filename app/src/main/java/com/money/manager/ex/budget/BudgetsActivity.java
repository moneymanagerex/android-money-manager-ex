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
package com.money.manager.ex.budget;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.money.manager.ex.R;
import com.money.manager.ex.budget.events.BudgetSelectedEvent;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.Core;

import org.greenrobot.eventbus.Subscribe;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class BudgetsActivity
    extends MmxBaseFragmentActivity {

    private boolean mIsDualPanel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // layout
        setContentView(R.layout.activity_budgets);

        setSupportActionBar(getToolbar());
        showStandardToolbarActions(getToolbar());
        // enable returning back from toolbar.
        setDisplayHomeAsUpEnabled(true);

        createFragments();

//        Answers.getInstance().logCustom(new CustomEvent(AnswersEvents.Budget.name()));
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
        // automatically e clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Events

    @Subscribe
    public void onEvent(BudgetSelectedEvent event) {
        showBudgetDetails(event.yearId, event.name);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        outState.putInt(KEY_TRANS_ID, mTransId);
    }

    // Public methods

    public void setDualPanel(boolean mIsDualPanel) {
        this.mIsDualPanel = mIsDualPanel;
    }

    public boolean isDualPanel() {
        return mIsDualPanel;
    }

    // Private methods

    private void createFragments() {
        LinearLayout fragmentDetail = (LinearLayout) findViewById(R.id.fragmentDetail);
        setDualPanel(fragmentDetail != null && fragmentDetail.getVisibility() == View.VISIBLE);

        Core core = new Core(getApplicationContext());

        // show navigation fragment
        BudgetListFragment fragment = (BudgetListFragment) getSupportFragmentManager()
                .findFragmentByTag(BudgetListFragment.class.getSimpleName());
        if (fragment == null) {
            // fragment create
            fragment = BudgetListFragment.newInstance();

            // add to stack
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentMain, fragment, BudgetListFragment.class.getSimpleName())
                    .commit();
        } else {
            if (core.isTablet()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentMain, fragment, BudgetListFragment.class.getSimpleName())
                        .commit();
            }
        }
    }

    private void showBudgetDetails(long id, String budgetName) {
        String tag = BudgetDetailFragment.class.getName() + "_" + Long.toString(id);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);

        if (fragment == null) {
            fragment = BudgetDetailFragment.newInstance(id, budgetName);
        }

        showFragment(fragment, tag);
    }

    /**
     * Displays the fragment and associate the tag
     *
     * @param fragment
     * @param tagFragment
     */
    public void showFragment(Fragment fragment, String tagFragment) {
        // In tablet layout, do not try to display the Home Fragment again. Show empty fragment.
        if (isDualPanel() && tagFragment.equalsIgnoreCase(BudgetListFragment.class.getName())) {
            fragment = new Fragment();
            tagFragment = "Empty";
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_right,
                R.anim.slide_out_left);
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack.
        if (isDualPanel()) {
            transaction.replace(R.id.fragmentDetail, fragment, tagFragment);
        } else {
            // Single panel UI.
            transaction.replace(R.id.fragmentMain, fragment, tagFragment);

            // todo: enable going back only if showing the list.
//            boolean showingList = tagFragment.equals(BudgetListFragment.class.getName());
//            setDisplayHomeAsUpEnabled(showingList);

            transaction.addToBackStack(null);
        }

        // Commit the transaction
        transaction.commit();
    }

}
