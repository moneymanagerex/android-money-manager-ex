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
package com.android.money.manager.ex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.Window;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MenuInflater;
import android.widget.Toast;

import com.android.money.manager.ex.database.MoneyManagerOpenHelper;
import com.android.money.manager.ex.database.TableAccountList;
import com.android.money.manager.ex.dropbox.DropboxActivity;
import com.android.money.manager.ex.fragment.AccountFragment;
import com.android.money.manager.ex.fragment.HomeFragment;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitleProvider;
/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
 * 
 */
public class MainActivity extends FragmentActivity {
	private static final String LOGCAT = MainActivity.class.getSimpleName();
	private static final String KEY_CONTENT = "MainActivity:CurrentPos";
	// definizione dei requestcode da passare alle activity
	private static final int REQUESTCODE_IMPORT = 1001;
	private static final int REQUESTCODE_USE_EXTERNAL_DB = 1010;
	// flag che indica se devo effettuare il refresh grafico
	private static boolean mRefreshUserInterface = false;
	// referenza all'applicazione
	MoneyManagerApplication mApp;
	// definizione dei conti correnti visibili
	List<TableAccountList> mAccountList;
	// definizione degli oggetti all'interno dell'activity
	private ViewPager  mViewPager;
    private TabsAdapter mTabsAdapter;
    private TitlePageIndicator mTitlePageIndicator;
    // posizione corrente nel TabsAdapter
    private int mCurrentPosition = 0;
    // flag per la gestione se del riavvio activity
    private static boolean mRestartActivity = false;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
			mCurrentPosition = savedInstanceState.getInt(KEY_CONTENT);
		}
		// prendo la referenza all'applicazione
		mApp = (MoneyManagerApplication)getApplication();
		// Imposto la navigazione
		//getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// prendo il riferimento a ViewPager
		mViewPager = (ViewPager)findViewById(R.id.pager);
		// creo l'adapter tabs
        mTabsAdapter = new TabsAdapter(this);
        mViewPager.setAdapter(mTabsAdapter);
        mViewPager.setOffscreenPageLimit(0);
        // impostazione dell'indicator
        mTitlePageIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
        mTitlePageIndicator.setViewPager(mViewPager);
        // imposto il colore del title indicator
		mTitlePageIndicator.setTextColor(getResources().getColor(R.color.color_ice_cream_sandiwich));
		mTitlePageIndicator.setSelectedColor(getResources().getColor(R.color.color_ice_cream_sandiwich));
		// imposto la grandezza del testo nell'indicator
		mTitlePageIndicator.setTextSize(20 * new DisplayMetrics().scaledDensity);
		// imposto che il selezionato deve diventare grassetto
		mTitlePageIndicator.setSelectedBold(true);
		// imposto il listener
		mTitlePageIndicator.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				//mCurrentPosition = position;
			}
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				return;
			}
			@Override
			public void onPageScrollStateChanged(int state) {
				return;
			}
		});
		// set che devo effettuare il refresh grafico
		setRefreshUserInterface(true);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// controllo se devo riavviare l'activity
		if (isRestartActivitySet()) {
			restartActivity(); // restart and exit
			return;
		}
		// controllo se devo effettuare il refresh grafico dell'interfaccia
		if (isRefreshUserInterface()) {
			refreshUserInterface();
			// controllo se avevo una posizione memorizzata
			if (mCurrentPosition > 0) {
				mViewPager.setCurrentItem(mCurrentPosition);
				mCurrentPosition = 0;
			}
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_CONTENT, mViewPager.getCurrentItem());
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setRestartActivity(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch(item.getItemId()) {
		case R.id.menu_new_transaction:
			intent = new Intent(this, CheckingAccountActivity.class);
			intent.setAction(CheckingAccountActivity.INTENT_ACTION_INSERT);
			// start for insert new transaction
			startActivity(intent);
			break;
		case R.id.menu_account:
			// manage accounts
			intent = new Intent(this, AccountListActivity.class);
			intent.setAction(Intent.ACTION_EDIT);
			startActivity(intent);
			break;
		case R.id.menu_category:
			// manage category
			intent = new Intent(this, CategorySubCategoryActivity.class);
			intent.setAction(Intent.ACTION_EDIT);
			startActivity(intent);
			break;			
		case R.id.menu_payee:
			// manage payee
			intent = new Intent(this, PayeeActivity.class);
			intent.setAction(Intent.ACTION_EDIT);
			startActivity(intent);
			break;
		case R.id.menu_currency:
			intent = new Intent(this, CurrencyFormatsListActivity.class);
			intent.setAction(Intent.ACTION_EDIT);
			startActivity(intent);
			break;
		case R.id.menu_sync_dropbox:
			// activity sync dropbox
			startActivity(new Intent(this, DropboxActivity.class));
			break;
		case R.id.menu_use_external_db:
			startFileBrowseActivity(REQUESTCODE_USE_EXTERNAL_DB);
			break;
		case R.id.menu_import_storage:
			startFileBrowseActivity(REQUESTCODE_IMPORT);
			break;
		case R.id.menu_settings:
			// open pref activity
			startActivity(new Intent(this, MoneyManagerPrefsActivity.class));
			break;
		case R.id.menu_about:
			// open about activity
			startActivity(new Intent(this, AboutActivity.class));
			break;
		case R.id.menu_exit:
			// close application
			android.os.Process.killProcess(android.os.Process.myPid());
			break;
		}
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		String pathImport;
		// controllo se ritorna con esito positivo
		if (resultCode == RESULT_CANCELED) { return; }
		// controllo il ritorno dell'esito
		switch (requestCode) {
		case REQUESTCODE_IMPORT:
			// controllo se l'intent di ritorno � valido
			if (data == null) { return; }
			// prendo il nome del file
			pathImport = data.getStringExtra(getPackageName() + ".result");
			if (TextUtils.isEmpty(pathImport)) { return; }
			// referenzio i due file da importare
			File fileSource = new File(pathImport);
			File fileDest = new File(MoneyManagerOpenHelper.databasePath);
			// avvio l'importazione
			new ImportExportDatabase().execute(fileSource, fileDest);
			// esco
			break;
		case REQUESTCODE_USE_EXTERNAL_DB:
			// check data
			if (data == null) { return; }
			// take database name file
			pathImport = data.getStringExtra(getPackageName() + ".result");
			if (TextUtils.isEmpty(pathImport)) { return; }
			// save the database file
			MoneyManagerApplication.setDatabasePath(getApplicationContext(), pathImport);
			// set to restart activity
			setRestartActivity(true);
			restartActivity();
			
			break;
		}
	}
	
	/**
	 * @return the mRefreshUserInterface
	 */
	public static boolean isRefreshUserInterface() {
		return mRefreshUserInterface;
	}

	/**
	 * @param mRefreshUserInterface the mRefreshUserInterface to set
	 */
	public static void setRefreshUserInterface(boolean mRefreshUserInterface) {
		MainActivity.mRefreshUserInterface = mRefreshUserInterface;
	}

	/**
	 * metodo per l'avvio dell'activity di browsing del file
	 * @param mode
	 */
	public void startFileBrowseActivity(int mode) {
		// prendo il file di partenza
		File fileIntent = Environment.getExternalStorageDirectory();
		// controllo il null se non montata
		if (fileIntent == null) {
			fileIntent = new File("/");
		}
		Intent intent = new Intent(this, FileBrowseActivity.class);
		intent.putExtra(getPackageName() + ".init", fileIntent.getPath());
		intent.setAction(FileBrowseActivity.INTENT_VIEW_FILE);
		// avvio la finestra per il result
		startActivityForResult(intent, mode);
	}
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
	/**
	 * riavvio la Activity main per il cambio di alcune impostazioni principali
	 */
	protected void restartActivity() {
		if (mRestartActivity) {
			Intent intent = getIntent();
			// chiudo l'activity corrente
			finish();
			// la riavvio
			startActivity(intent);
			// close application
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		// set state a false
		setRestartActivity(false);
	}
	/**
	 * refresh grafico dell'activity
	 * ricostruisce i tab dell'interfaccia
	 */
	public void refreshUserInterface() {
		// indeterminate
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setProgressBarIndeterminateVisibility(false);
		}
		// lettura della valuta di base
		mApp.loadBaseCurrencyId(this);
		// richiamo il metodo per la composizione delle valute
		mApp.loadHashMapCurrency(this);
		if (isRefreshUserInterface() == false) { return; }

		// rimuovo tutti i tab inseriti
		mTabsAdapter.removeAllTab();
		// creo i tab necessari alla visualizzazione
        // aggiungo il tab home
        //mTabsAdapter.addTab(mTabHome, HomeFragment.class);
		mTabsAdapter.addTab(new MainActivityTab(HomeFragment.class, getResources().getString(R.string.home)));
        // definizione dell'helper per l'accesso ai dati
        MoneyManagerOpenHelper helper = new MoneyManagerOpenHelper(this);
        // prendo la lista dei conti correnti da visualizzare nei tab
        mAccountList = helper.getListAccounts(mApp.getAccountsOpenVisible(), mApp.getAccountFavoriteVisible());
        //ciclo gli account e li inserisco
        for(int i = 0; i < mAccountList.size(); i ++) {
        	// aggiungo il tab dell'account
        	if (mAccountList.get(i).getAccountType().toUpperCase().equals("CHECKING")) {
        		mTabsAdapter.addTab(new MainActivityTab(AccountFragment.class, mAccountList.get(i).getAccountName(), mAccountList.get(i)));
        	}
        }
        mTabsAdapter.notifyDataSetChanged();
	}
	/**
	 * classe per l'importazione/esportazione del database dall'applicazione
	 * @see ASyncTask
	 * @author a.lazzari
	 *
	 */
	private class ImportExportDatabase extends AsyncTask<File, Long, Boolean> {
		private ProgressDialog progress;
		
		@Override
		protected void onPreExecute() {
			// creo il progress bar da importare
			progress = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.import_export), getResources().getString(R.string.import_export_database_progress), true);
		}
		@Override
		protected Boolean doInBackground(File... files) {
			try {
				if (!files[1].exists()) {
					// controllo se il file di destinazione esiste se no creo
					files[1].createNewFile();
				}
				// passo alla copia dei file
				copyFile(files[0], files[1]);
			} catch (IOException e) {
				return false;
			}
			return true;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			// se ancora attiva chiudo la finestra di progesso
			if (progress.isShowing()) {
				progress.dismiss();
			}
			// in funzione del risultato mostro un toast
			Toast.makeText(MainActivity.this, getResources().getString(result ? R.string.import_export_success : R.string.import_export_failed), Toast.LENGTH_LONG).show();	
		}
		
		protected void copyFile(File source, File destination) throws IOException {
			FileChannel sourceChannel = new FileInputStream(source).getChannel();
			FileChannel destinationChannel = new FileOutputStream(destination).getChannel();
			try {
				sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
			} finally {
				if (sourceChannel != null)
					sourceChannel.close();
				if (destinationChannel != null)
					destinationChannel.close();
			}
		}
	}
	
    public static class TabsAdapter extends FragmentPagerAdapter implements TitleProvider {
        private Context mContext;
        private ArrayList<MainActivityTab> mFrags = new ArrayList<MainActivityTab>();
        private Map<Integer, Fragment> mMapFragment = new HashMap<Integer, Fragment>();
        
        public TabsAdapter(FragmentActivity activity) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
        }
        
        public void addTab(MainActivityTab tab) {
            mFrags.add(tab);
            notifyDataSetChanged();
        }
        
        public void removeAllTab() {
        	mFrags.clear();
        	mMapFragment.clear();
        	notifyDataSetChanged();
        }

        @Override
        public int getCount() {
        	return mFrags.size();
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
        	mMapFragment.put(position, fragment);
            return fragment;
        }

		@Override
		public String getTitle(int position) {
			final int LENMAX = 15;
			String title = (String) mFrags.get(position).getTitle();
			// ellipse del testo
			/*if (title.length() > LENMAX) {
				title = title.substring(0, LENMAX - 1) + " ...";
			}*/
			return title;
		}
        
		public Fragment getFragment(int position) {
			return mMapFragment.get(position);
		}
    }
    
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
    	
    	public Class<?> getClss() {
    		return this.mClss;
    	}
    	
    	public String getTitle() {
    		return this.mTitle;
    	}
    	
    	public TableAccountList getAccountList() {
    		return this.mAccountList;
    	}
    }
}
