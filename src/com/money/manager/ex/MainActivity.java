/*******************************************************************************
 * Copyright (C) 2012 The Android Money Manager Ex Project
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 ******************************************************************************/
package com.money.manager.ex;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.Passcode;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.dropbox.DropboxActivity;
import com.money.manager.ex.fragment.AccountFragment;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.HomeFragment;
import com.money.manager.ex.notifications.MoneyManagerNotifications;
import com.money.manager.ex.reports.CategoriesReportActivity;
import com.money.manager.ex.reports.IncomeVsExpensesActivity;
import com.money.manager.ex.reports.PayeesReportActivity;
import com.money.manager.ex.settings.PreferencesActivity;
import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.TitlePageIndicator;
/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * 
 */
@SuppressLint("DefaultLocale")
public class MainActivity extends BaseFragmentActivity {
	private static class MainActivityTab {
    	private Class<?> mClss;
    	private String mTitle;
    	private TableAccountList mAccountList;
    	
    	public MainActivityTab(Class<?> clss, String title) {
    		this(clss, title, null);
    	}
    	
    	public MainActivityTab(Class<?> clss, String title, TableAccountList account) {
    		this.mClss = clss;
    		this.mTitle = title;
    		this.mAccountList = account;
    	}
    	
    	public TableAccountList getAccountList() {
    		return this.mAccountList;
    	}
    	
    	public Class<?> getClss() {
    		return this.mClss;
    	}
    	
    	public String getTitle() {
    		return this.mTitle;
    	}
    }
	
	public static class TabsAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
        private Context mContext;
        private ArrayList<MainActivityTab> mFrags = new ArrayList<MainActivityTab>();
        
        public TabsAdapter(FragmentActivity activity) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
        }
        
        public void addTab(MainActivityTab tab) {
            mFrags.add(tab);
            notifyDataSetChanged();
        }
        
        @Override
        public int getCount() {
        	return mFrags.size();
        }
        
        @Override
		public int getIconResId(int index) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
        public Fragment getItem(int position) {
        	Fragment fragment;// = Fragment.instantiate(mContext, mFrags.get(position).getClss().getName(), null);
        	String getName = mFrags.get(position).getClss().getName();
        	if (getName.equals(AccountFragment.class.getName())) {
        		fragment = AccountFragment.newIstance(mFrags.get(position).getAccountList().getAccountId());
        	} else {
        		fragment = Fragment.instantiate(mContext, mFrags.get(position).getClss().getName(), null);
        	}
            return fragment;
        }
        
		@Override
		public CharSequence getPageTitle(int position) {
			return getTitle(position);
		}

		public String getTitle(int position) {
			return mFrags.get(position).getTitle();
		}
		
		public void removeAllTab() {
        	mFrags.clear();
        	notifyDataSetChanged();
        }
    }
	private static final String LOGCAT = MainActivity.class.getSimpleName();
	private static final String KEY_CONTENT = "MainActivity:CurrentPos";
	private static final String KEY_IS_AUTHENTICATED = "MainActivity:isAuthenticated";
	private static final String KEY_IN_AUTHENTICATION = "MainActivity:isInAuthenticated";

	// definizione dei requestcode da passare alle activity
	private static final int REQUEST_PICKFILE_CODE = 1;
	private static final int REQUEST_PASSCODE = 2;
	// flag che indica se devo effettuare il refresh grafico
	private static boolean mRefreshUserInterface = false;
	// state if restart activity
    private static boolean mRestartActivity = false;
    
    /**
	 * @return the mRefreshUserInterface
	 */
	public static boolean isRefreshUserInterface() {
		return mRefreshUserInterface;
	}
    /**
	 * @return the mRestart
	 */
	public static boolean isRestartActivitySet() {
		return mRestartActivity;
	}
    /**
	 * @param mRefreshUserInterface the mRefreshUserInterface to set
	 */
	public static void setRefreshUserInterface(boolean mRefreshUserInterface) {
		MainActivity.mRefreshUserInterface = mRefreshUserInterface;
	}
    /**
	 * @param mRestart the mRestart to set
	 */
	public static void setRestartActivity(boolean mRestart) {
		MainActivity.mRestartActivity = mRestart;
	}
	
	private boolean isAuthenticated = false;
	private boolean isInAuthentication = false;
	
	// list of account visible
	List<TableAccountList> mAccountList;
	// object int layout
	private ViewPager  mViewPager;
	private TabsAdapter mTabsAdapter;

	private TitlePageIndicator mTitlePageIndicator;
	
    // current position
    private int mCurrentPosition = 0;
    
	// Advance mode
    private boolean mAdvanceShow = false;
    
    // notification
    private static MoneyManagerNotifications notifications;
	
	/**
	 * Change database applications
	 * @param pathDatabase new path of databases
	 */
	private void changeDatabase(String pathDatabase) {
		// save the database file
		MoneyManagerApplication.setDatabasePath(getApplicationContext(), pathDatabase);
		// set to restart activity
		setRestartActivity(true);
		restartActivity();
	}
	
	private void changeFragment(int accountId) {
		String nameFragment = AccountFragment.class.getSimpleName() + "_" + Integer.toString(accountId);
		AccountFragment fragment;
		fragment = (AccountFragment) getSupportFragmentManager().findFragmentByTag(nameFragment);
		if (fragment == null) {
			fragment = AccountFragment.newIstance(accountId);
		}
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		//animation
		transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		transaction.replace(R.id.fragmentContent, fragment, nameFragment);
		transaction.addToBackStack(null);
		// Commit the transaction
		transaction.commit();
	}
	
	/**
	 * Dialog to choose exit from application
	 */
	private void exitApplication() {
		AlertDialog.Builder exitDialog = new AlertDialog.Builder(this);
		exitDialog.setTitle(R.string.close_application);
		exitDialog.setMessage(R.string.question_close_application);
		exitDialog.setIcon(android.R.drawable.ic_dialog_info);
		exitDialog.setPositiveButton(android.R.string.yes, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				MoneyManagerApplication.killApplication();
			}
		});
		exitDialog.setNegativeButton(android.R.string.no, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		// show dialog
		exitDialog.create().show();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// check request code
		switch (requestCode) {
		case REQUEST_PICKFILE_CODE:
			if (resultCode==RESULT_OK && data!=null && data.getData()!=null) {
				changeDatabase(data.getData().getPath());
			}
			break;
		case REQUEST_PASSCODE:
			isAuthenticated = false; isInAuthentication = false;
			if (resultCode == RESULT_OK && data != null) {
				Passcode passcode = new Passcode(this);
				String passIntent = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
				String passDb = passcode.getPasscode();
				if (passIntent != null && passDb != null) {
					isAuthenticated = passIntent.equals(passDb);
					if (!isAuthenticated) {
						Toast.makeText(this, R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
					}
				}
			}
			// close if not authenticated
			if (!isAuthenticated) {
				this.finish();
			}
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setRestartActivity(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//check intent is valid
		if (getIntent() != null && getIntent().getData() != null) {
			String pathFile = getIntent().getData().getEncodedPath();
			// decode
			try {
				pathFile = URLDecoder.decode(pathFile, "UTF-8"); //decode file path
				Log.i(LOGCAT, "Path intent file to open:" + pathFile);
				if (new File(pathFile).exists()) {
					MoneyManagerApplication.setDatabasePath(this, pathFile);
				} else {
					Log.w(LOGCAT, "Path intent file to open:" + pathFile + " not exists!!!");
				}
			} catch (Exception e) {
				Log.e(LOGCAT, e.getMessage());
			}
		}
		// check authentication
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(KEY_IS_AUTHENTICATED))
				isAuthenticated = savedInstanceState.getBoolean(KEY_IS_AUTHENTICATED);
			if (savedInstanceState.containsKey(KEY_IN_AUTHENTICATION))
				isInAuthentication = savedInstanceState.getBoolean(KEY_IN_AUTHENTICATION);
		}
		MoneyManagerApplication application = (MoneyManagerApplication)getApplication();
		// load base currency and compose hash currencies
		application.loadBaseCurrencyId(this);
		application.loadHashMapCurrency(this);
		// set advance mode
		mAdvanceShow = application.getTypeHome() == MoneyManagerApplication.TYPE_HOME_ADVANCE;
		// check type mode
		if (mAdvanceShow) {
			onCreatePager(savedInstanceState);
		} else {
			onCreateFragments(savedInstanceState);
		}
		setRefreshUserInterface(true);
		//show donate dialog
		MoneyManagerApplication.showDonateDialog(this, false);
		//show change log and path
		MoneyManagerApplication.showChangeLog(this, false);
		MoneyManagerApplication.showDatabasePathWork(this);

		//notification
		if (notifications == null) {
			notifications = new MoneyManagerNotifications(this);
			notifications.notifyRepeatingTransaction();
		}
	}
	/**
	 * this method call for classic method (show fragments)
	 * @param savedInstanceState
	 */
	private void onCreateFragments(Bundle savedInstanceState) {
		setContentView(R.layout.main_fragments_activity);
		// check if fragment into stack
		HomeFragment fragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(HomeFragment.class.getSimpleName());
		if (fragment == null) {
			// fragment create
			fragment = new HomeFragment();
			// add to stack
			getSupportFragmentManager().beginTransaction().add(R.id.fragmentContent, fragment, HomeFragment.class.getSimpleName()).commit();
		} 
	}
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		getSherlock().getMenuInflater().inflate(R.menu.menu_main, menu);
		//check if render visible move to external storage
		MenuItem item = menu.findItem(R.id.menu_move_database_external);
		if (item != null) {
			item.setVisible(MoneyManagerApplication.getDatabasePath(this).startsWith("/data/data/com.money.manager") && 
					Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));
		}
		//check if it has already made ​​a donation
		item = menu.findItem(R.id.menu_donate);
		if (item != null) {
			Core core = new Core(this);
			item.setVisible(TextUtils.isEmpty(core.getInfoValue(Core.INFO_SKU_ORDER_ID)));
		}
		return super.onCreateOptionsMenu(menu);
	}
	/**
	 * this method call for advance method (show viewpagerindicator)
	 * @param savedInstanceState
	 */
	private void onCreatePager(Bundle savedInstanceState) {
		setContentView(R.layout.main_pager_activity);
		if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
			mCurrentPosition = savedInstanceState.getInt(KEY_CONTENT);
		}
		// create tabs adapter
        mTabsAdapter = new TabsAdapter(this);
		// pointer to view pager
		mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mTabsAdapter);
        mViewPager.setOffscreenPageLimit(1);
        // set title indicator
        mTitlePageIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
        mTitlePageIndicator.setViewPager(mViewPager);
		
        mTitlePageIndicator.setTextColor(getResources().getColor(R.color.color_ice_cream_sandiwich));
		mTitlePageIndicator.setSelectedColor(getResources().getColor(R.color.color_ice_cream_sandiwich));
		mTitlePageIndicator.setSelectedBold(true);

	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		/*
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Fragment fragment = getSupportFragmentManager().findFragmentByTag(HomeFragment.class.getSimpleName());
			// check if show home fragment
			if ((fragment != null && fragment.isVisible()) || mAdvanceShow) {
				exitApplication(); // question if user would exit
				return true;
			}
		}
		*/
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		//quick-fix convert 'switch' to 'if-else'
		if (item.getItemId() == R.id.menu_new_transaction) {
			intent = new Intent(this, CheckingAccountActivity.class);
			intent.setAction(CheckingAccountActivity.INTENT_ACTION_INSERT);
			// start for insert new transaction
			startActivity(intent);
		} else if (item.getItemId() == R.id.menu_search_transaction) {
			startActivity(new Intent(this, SearchActivity.class));
		} else if (item.getItemId() == R.id.menu_account) {
			// manage accounts
			intent = new Intent(this, AccountListActivity.class);
			intent.setAction(Intent.ACTION_EDIT);
			startActivity(intent);
		} else if (item.getItemId() == R.id.menu_category) {
			// manage category
			intent = new Intent(this, CategorySubCategoryActivity.class);
			intent.setAction(Intent.ACTION_EDIT);
			startActivity(intent);
		} else if (item.getItemId() == R.id.menu_payee) {
			// manage payee
			intent = new Intent(this, PayeeActivity.class);
			intent.setAction(Intent.ACTION_EDIT);
			startActivity(intent);
		} else if (item.getItemId() == R.id.menu_repeating_transaction) {
			startActivity(new Intent(this, RepeatingTransactionListActivity.class));
		} else if (item.getItemId() == R.id.menu_currency) {
			intent = new Intent(this, CurrencyFormatsListActivity.class);
			intent.setAction(Intent.ACTION_EDIT);
			startActivity(intent);
		} else if (item.getItemId() == R.id.menu_sync_dropbox) {
			// activity sync dropboxgets
			startActivity(new Intent(this, DropboxActivity.class));
		} else if (item.getItemId() == R.id.menu_move_database_external) {
			// copy files
			Core core = new Core(this);
			File newDatabases = core.backupDatabase();
			if (newDatabases != null) {
				//Toast.makeText(this, Html.fromHtml(getString(R.string.database_has_been_moved, "<b>" + newDatabases.getAbsolutePath() + "</b>")), Toast.LENGTH_LONG).show();
				changeDatabase(newDatabases.getAbsolutePath());
			} else {
				Toast.makeText(this, R.string.copy_database_on_external_storage_failed, Toast.LENGTH_LONG).show();
			}
		} else if (item.getItemId() == R.id.menu_use_external_db) {
			pickFile(Environment.getExternalStorageDirectory());
		} else if (item.getItemId() == R.id.menu_settings) {
			startActivity(new Intent(this, PreferencesActivity.class));
		} else if (item.getItemId() == R.id.menu_report_categories) {
			startActivity(new Intent(this, CategoriesReportActivity.class));
		} else if (item.getItemId() == R.id.menu_report_payees) {
			startActivity(new Intent(this, PayeesReportActivity.class));
		} else if (item.getItemId() == R.id.menu_report_income_vs_expenses) {
			startActivity(new Intent(this, IncomeVsExpensesActivity.class));
		} else if (item.getItemId() == R.id.menu_about) {
			// open about activity
			startActivity(new Intent(this, AboutActivity.class));
		} else if (item.getItemId() == R.id.menu_donate) {
			startActivity(new Intent(this, DonateActivity.class));
		} else if (item.getItemId() == R.id.menu_exit) {
			// close application
			exitApplication();
		}
		return false;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// check if restart activity
		if (isRestartActivitySet()) {
			restartActivity(); // restart and exit
			return;
		}
		// check if refresh user interface
		if (isRefreshUserInterface()) {
			refreshUserInterface();
			// reposition view
			if (mViewPager != null && mCurrentPosition > 0) {
				mViewPager.setCurrentItem(mCurrentPosition);
				mCurrentPosition = 0;
			}
		}
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mViewPager != null) {
			outState.putInt(KEY_CONTENT, mViewPager.getCurrentItem());
		} else {
			outState.putInt(KEY_CONTENT, 0);
		}
		outState.putBoolean(KEY_IS_AUTHENTICATED, isAuthenticated);
		outState.putBoolean(KEY_IN_AUTHENTICATION, isInAuthentication);
	}
	@Override
	protected void onStart() {
		super.onStart();
		// check if has passcode and authenticate
		if (!isAuthenticated) {
			Passcode passcode = new Passcode(this);
			if (passcode.hasPasscode() && !isInAuthentication) {
				Intent intent = new Intent(this, PasscodeActivity.class);
				// set action and data
				intent.setAction(PasscodeActivity.INTENT_REQUEST_PASSWORD);
				intent.putExtra(PasscodeActivity.INTENT_MESSAGE_TEXT, getString(R.string.enter_your_passcode));
				// start activity
				startActivityForResult(intent, REQUEST_PASSCODE);
				// set in authentication
				isInAuthentication = true;
			}
		}
	}
	/**
	 * pick a file to use
	 * @param file start folder
	 */
	private void pickFile(File file) {
	    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	    intent.setDataAndType(Uri.fromFile(file), "vnd.android.cursor.dir/*");
	    intent.setType("file/*");
	    if (((MoneyManagerApplication)getApplication()).isUriAvailable(getApplicationContext(), intent)) { 
		    try {
		        startActivityForResult(intent, REQUEST_PICKFILE_CODE);
		    } catch (Exception e) {
		        Log.e(LOGCAT, e.getMessage());
		        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		    }
	    } else {
	    	Toast.makeText(this, R.string.error_intent_pick_file, Toast.LENGTH_LONG).show();
	    }
	}
	
    /**
	 * refresh user interface advance
	 * 
	 */
	@SuppressLint("DefaultLocale")
	public void refreshUserInterface() {
		if (!(isRefreshUserInterface())) { return; }
		if (!mAdvanceShow) { return; }
		
		mTabsAdapter.removeAllTab();
		// add tab home
		mTabsAdapter.addTab(new MainActivityTab(HomeFragment.class, getResources().getString(R.string.home)));
		
		// take list account
        MoneyManagerOpenHelper helper = new MoneyManagerOpenHelper(this);
        mAccountList = helper.getListAccounts(((MoneyManagerApplication)getApplication()).getAccountsOpenVisible(), ((MoneyManagerApplication)getApplication()).getAccountFavoriteVisible());

        // add tab for account
        for(int i = 0; i < mAccountList.size(); i ++) {
        	if (mAccountList.get(i).getAccountType().toUpperCase().equals("CHECKING")) {
        		mTabsAdapter.addTab(new MainActivityTab(AccountFragment.class, mAccountList.get(i).getAccountName(), mAccountList.get(i)));
        	}
        }
        
        mTabsAdapter.notifyDataSetChanged();
	}
	/**
	 *  for the change setting restart process application
	 */
	protected void restartActivity() {
		if (mRestartActivity) {
			Intent intent = getIntent();
			// finish this activity
			finish();
			// restart
			startActivity(intent);
			// kill process
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		// set state a false
		setRestartActivity(false);
	}
	/**
	 * scroll view pager
	 * @param position to scroll
	 */
	private void scrollToPage(int position) {
		if (mViewPager != null) {
			mViewPager.setCurrentItem(position + 1); // add 1 because 0 is home
		}
	}
	/**
	 * show a fragment select with position or account id
	 * @param position to page
	 * @param accountId account id of the fragment to be loaded
	 */
	public void showFragment(int position, int accountId) {
		if (mAdvanceShow) {
			scrollToPage(position);
		} else {
			changeFragment(accountId);
		}
	}
}
