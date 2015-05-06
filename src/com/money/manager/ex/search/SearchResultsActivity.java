package com.money.manager.ex.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.money.manager.ex.R;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.fragment.AllDataFragment;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.HomeFragment;

import java.util.ArrayList;

public class SearchResultsActivity extends BaseFragmentActivity {
    public static String WHERE_CLAUSE = "SearchResultActivity:WhereClause";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        processSearchParameters();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_results, menu);
        return true;
    }

    private void processSearchParameters() {
        // Get the where clause, if any.
        Intent intent = getIntent();
        if(intent == null) return;

        ArrayList<String> whereClause = intent.getStringArrayListExtra(WHERE_CLAUSE);

        Bundle args = new Bundle();
        args.putStringArrayList(AllDataFragment.KEY_ARGUMENTS_WHERE, whereClause);
        args.putString(AllDataFragment.KEY_ARGUMENTS_SORT, QueryAllData.ACCOUNTID + ", " + QueryAllData.ID);

        // set the parameters on the Fragment.
        FragmentManager fragmentManager = getSupportFragmentManager();
//        Fragment searchResultsFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
//        AllDataFragment searchResultsFragment = (AllDataFragment) fragmentManager
//                .findFragmentByTag(AllDataFragment.class.getSimpleName());
        AllDataFragment searchResultsFragment = AllDataFragment.newInstance(-1);

        //set arguments
        searchResultsFragment.setArguments(args);
//        searchResultsFragment.setSearResultFragmentLoaderCallbacks(this);
        searchResultsFragment.setShownHeader(true);

        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContent, searchResultsFragment, AllDataFragment.class.getSimpleName())
                .commit();
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
}
