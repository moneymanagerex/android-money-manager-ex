package com.android.money.manager.ex.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.android.money.manager.ex.MoneyManagerApplication;

public abstract class BaseFragmentActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// apply theme application
		((MoneyManagerApplication)getApplication()).setThemeApplication(this);
	}
}
