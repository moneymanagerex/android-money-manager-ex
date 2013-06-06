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
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.Passcode;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.dropbox.DropboxServiceIntent;
import com.money.manager.ex.fragment.AccountFragment;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.HomeFragment;
import com.money.manager.ex.fragment.TipsDialogFragment;
import com.money.manager.ex.notifications.MoneyManagerNotifications;
import com.money.manager.ex.preferences.PreferencesActivity;
import com.money.manager.ex.reports.CategoriesReportActivity;
import com.money.manager.ex.reports.IncomeVsExpensesActivity;
import com.money.manager.ex.reports.PayeesReportActivity;
/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * 
 */ 
@SuppressLint("DefaultLocale")
public class MainActivity extends BaseFragmentActivity {
	private static final String LOGCAT = MainActivity.class.getSimpleName();
	private static final String KEY_IS_AUTHENTICATED = "MainActivity:isAuthenticated";
	private static final String KEY_IN_AUTHENTICATION = "MainActivity:isInAuthenticated";
	private static final String KEY_IS_SHOW_TIPS_DROPBOX2 = "MainActivity:isShowTipsDropbox2"; 

	// definizione dei requestcode da passare alle activity
	public static final int REQUEST_PICKFILE_CODE = 1;
	public static final int REQUEST_PASSCODE = 2;
	
	// state if restart activity
    private static boolean mRestartActivity = false;
    
    /**
	 * @return the mRestart
	 */
	public static boolean isRestartActivitySet() {
		return mRestartActivity;
	}

    /**
	 * @param mRestart the mRestart to set
	 */
	public static void setRestartActivity(boolean mRestart) {
		MainActivity.mRestartActivity = mRestart;
	}
	
	private boolean isAuthenticated = false;
	private boolean isInAuthentication = false;
	private boolean isShowTipsDropbox2 = false;
	
	// list of account visible
	List<TableAccountList> mAccountList;
    
    // notification
    private static MoneyManagerNotifications notifications;
    
    // dropbox object
    @SuppressWarnings("unused")
	private DropboxHelper mDropboxHelper;
    
    // state dual panel
    private boolean mIsDualPanel = false;
	
	/**
	 * Change database applications
	 * @param pathDatabase new path of databases
	 */
	private void changeDatabase(String pathDatabase) {
		// save the database file
		MoneyManagerApplication.setDatabasePath(getApplicationContext(), pathDatabase);
		MoneyManagerApplication.resetDonateDialog(getApplicationContext());
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
		//set if shown open menu
		fragment.setShownOpenDatabaseItemMenu(isDualPanel());
		//transaction
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		//animation
		transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		if (isDualPanel()) {
			transaction.replace(R.id.fragmentDetail, fragment, nameFragment);
		} else {
			transaction.replace(R.id.fragmentContent, fragment, nameFragment);
			transaction.addToBackStack(null);
		}
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
		exitDialog.setIcon(R.drawable.ic_launcher);
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
		// debug mode rotate screen
		setShownRotateInDebugMode(false);
		// if large screen set orietation landscape 
		/*if ((getResources().getConfiguration().screenLayout &  Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {     
			 setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			 setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		}*/
		
		//close notification
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(DropboxServiceIntent.NOTIFICATION_DROPBOX_OPEN_FILE);
		//check intent is valid
		if (getIntent() != null && getIntent().getData() != null) {
			String pathFile = getIntent().getData().getEncodedPath();
			// decode
			try {
				pathFile = URLDecoder.decode(pathFile, "UTF-8"); //decode file path
				if (BuildConfig.DEBUG) Log.d(LOGCAT, "Path intent file to open:" + pathFile);
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
		// check type mode
		onCreateFragments(savedInstanceState);
		//show tips dialog
		showTipsDialog(savedInstanceState);
		//show donate dialog
		Core core = new Core(this);
		if (TextUtils.isEmpty(core.getInfoValue(Core.INFO_SKU_ORDER_ID)))
			MoneyManagerApplication.showDonateDialog(this, false);
		//show change log and path
		MoneyManagerApplication.showChangeLog(this, false, false);
		MoneyManagerApplication.showDatabasePathWork(this);

		//notification
		if (notifications == null) {
			notifications = new MoneyManagerNotifications(this);
			notifications.notifyRepeatingTransaction();
		}
		
		//create a connection to dropbox
		mDropboxHelper = DropboxHelper.getInstance(getApplicationContext());
	}
	
	/**
	 * this method call for classic method (show fragments)
	 * @param savedInstanceState
	 */
	private void onCreateFragments(Bundle savedInstanceState) {
		setContentView(R.layout.main_fragments_activity);
		LinearLayout fragmentDetail = (LinearLayout)findViewById(R.id.fragmentDetail); 
		setDualPanel(fragmentDetail != null && fragmentDetail.getVisibility() == View.VISIBLE); 
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
		//check if it has already made ​​a donation
		MenuItem item = menu.findItem(R.id.menu_donate);
		if (item != null) {
			Core core = new Core(this);
			item.setVisible(TextUtils.isEmpty(core.getInfoValue(Core.INFO_SKU_ORDER_ID)));
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onDestroy() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if(notificationManager != null) 
			notificationManager.cancelAll();
		super.onDestroy();
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
		if (item.getItemId() == R.id.menu_search_transaction) {
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
		} else if (item.getItemId() == R.id.menu_open_database) {
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
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// check if restart activity
		if (isRestartActivitySet()) {
			restartActivity(); // restart and exit
			return;
		}
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_IS_AUTHENTICATED, isAuthenticated);
		outState.putBoolean(KEY_IN_AUTHENTICATION, isInAuthentication);
		outState.putBoolean(KEY_IS_SHOW_TIPS_DROPBOX2, isShowTipsDropbox2);
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

	
	public void showTipsDialog(Bundle savedInstanceState) {
		if (savedInstanceState == null || (savedInstanceState != null && !savedInstanceState.getBoolean(KEY_IS_SHOW_TIPS_DROPBOX2))) {
			//show tooltip for dropbox
			TipsDialogFragment tipsDropbox = TipsDialogFragment.getInstance(getApplicationContext(), "passtodropbox2");
			if (tipsDropbox != null) {
				tipsDropbox.setTitle(Html.fromHtml("<small>" + getString(R.string.tips_title_new_version_dropbox) + "</small>"));
				tipsDropbox.setTips(getString(R.string.tips_new_version_dropbox));
				//tipsDropbox.setCheckDontShowAgain(true);
				tipsDropbox.show(getSupportFragmentManager(), "passtodropbox2");
				isShowTipsDropbox2 = true; // set shown
			}
		}
	}
	
	
	/**
	 * show a fragment select with position or account id
	 * @param position to page
	 * @param accountId account id of the fragment to be loaded
	 */
	public void showFragment(int position, int accountId) {
		changeFragment(accountId);
	}

	/**
	 * @return the mIsDualPanel
	 */
	public boolean isDualPanel() {
		return mIsDualPanel;
	}

	/**
	 * @param mIsDualPanel the mIsDualPanel to set
	 */
	public void setDualPanel(boolean mIsDualPanel) {
		this.mIsDualPanel = mIsDualPanel;
	}
}
