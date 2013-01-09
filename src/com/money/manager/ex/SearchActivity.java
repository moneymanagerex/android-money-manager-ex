package com.money.manager.ex;

import android.os.Bundle;

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
	}
}
