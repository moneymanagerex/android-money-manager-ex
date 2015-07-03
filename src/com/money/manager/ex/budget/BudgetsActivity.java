package com.money.manager.ex.budget;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.core.Core;

public class BudgetsActivity
        extends BaseFragmentActivity
implements IBudgetListCallbacks{

    private boolean mIsDualPanel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // layout
        setContentView(R.layout.activity_budgets);

        setSupportActionBar(getToolbar());
        setToolbarStandardAction(getToolbar());
        // enable returning back from toolbar.
        setDisplayHomeAsUpEnabled(true);

        createFragments();
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

    // End menu

    @Override
    public void onBudgetClicked(long budgetYearId) {
        // budget clicked in the list; show the details fragment.
        // todo: showBudgetDetails(budgetYearId);
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
        BudgetsListFragment fragment = (BudgetsListFragment) getSupportFragmentManager()
                .findFragmentByTag(BudgetsListFragment.class.getSimpleName());
        if (fragment == null) {
            // fragment create
            fragment = BudgetsListFragment.newInstance();
            fragment.setListener(this);

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

    private void showBudgetDetails(long id) {
        String tag = BudgetDetailFragment.class.getName();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);

        if (fragment == null) {
            fragment = BudgetDetailFragment.newInstance(id);
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
        if (isDualPanel() && tagFragment.equalsIgnoreCase(BudgetsListFragment.class.getName())) {
            fragment = new Fragment();
            tagFragment = "Empty";
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_left);
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack.
        if (isDualPanel()) {
            transaction.replace(R.id.fragmentDetail, fragment, tagFragment);
        } else {
            // Single panel UI.
            transaction.replace(R.id.fragmentContent, fragment, tagFragment);

            // todo: enable going back only if showing the list.
//            boolean showingList = tagFragment.equals(BudgetsListFragment.class.getName());
//            setDisplayHomeAsUpEnabled(showingList);
        }
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }

}
