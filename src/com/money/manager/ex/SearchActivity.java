package com.money.manager.ex;

import android.os.Bundle;

import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.SearchFragment;

public class SearchActivity extends BaseFragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SearchFragment fragment = (SearchFragment)getSupportFragmentManager().findFragmentByTag(SearchFragment.class.getSimpleName());
		if (fragment == null) {
			// fragment create
			fragment = new SearchFragment();
			// add to stack
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment, SearchFragment.class.getSimpleName()).commit();
		}
		// home
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
