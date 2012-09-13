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
package com.money.manager.ex.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.Window;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.RESTUtility;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.TokenPair;
import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.SimpleCrypto;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.fragment.BaseFragmentActivity;

public class DropboxActivity extends BaseFragmentActivity {
	private static final String LOGCAT = DropboxActivity.class.getSimpleName();
	// type operation
	private enum TypeOperation {SYNC, DONWLOAD, UPLOAD};
    // info to access sharedpref
    public static final String ACCOUNT_PREFS_NAME = "MONEY_MANAGER_EX_DROPBOX";
    public static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    public static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	public static final String REMOTE_FILE = "DROPBOX_REMOTE_FILE";
	// id menu
	private static final int MENU_REFRESH = 1;
	// Dropbox root
	private static final String DROPBOX_ROOT = "/";
	// Application
	MoneyManagerApplication application = new MoneyManagerApplication();
    // Dropbox Api
    DropboxAPI<AndroidAuthSession> mApi;
    // Check for logged in
    private boolean mLoggedIn;
    // orientation of layout and state of sync
	private int mPrevOrientation;
	private static boolean mSyncInProgress = false;
    // ref object in layout
    private TextView txtLinkDropbox;
    private ListView lstFileDropbox;
    
    private static ProgressDialog mDialog;
    
	private AndroidAuthSession buildSession() {
		String secret = "";
		try {
			secret = SimpleCrypto.decrypt(MoneyManagerApplication.KEY, "A313D7447960230A802C9A55EDFE281E");
		} catch (Exception e) {
			Log.e(LOGCAT, e.getMessage());
		}
		AppKeyPair appKeyPair = new AppKeyPair(MoneyManagerApplication.DROPBOX_APP_KEY, secret);
	    AndroidAuthSession session;
	
	    String[] stored = getKeys();
	    if (stored != null) {
	        AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
	        session = new AndroidAuthSession(appKeyPair, MoneyManagerApplication.DROPBOX_ACCESS_TYPE, accessToken);
	    } else {
	        session = new AndroidAuthSession(appKeyPair, MoneyManagerApplication.DROPBOX_ACCESS_TYPE);
	    }
	
	    return session;
	}

	private void checkAppKeySetup() {
	    // check if key is valid
	    if (MoneyManagerApplication.DROPBOX_APP_KEY.startsWith("CHANGE") ||
	    		MoneyManagerApplication.DROPBOX_APP_SECRET.startsWith("CHANGE")) {
	        Toast.makeText(this, "You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.", Toast.LENGTH_LONG).show();
	        finish();
	        return;
	    }
	
	    // check if manifest is correctly
	    Intent testIntent = new Intent(Intent.ACTION_VIEW);
	    String scheme = "db-" + MoneyManagerApplication.DROPBOX_APP_KEY;
	    String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
	    testIntent.setData(Uri.parse(uri));
	    PackageManager pm = getPackageManager();
	    if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
	    	Toast.makeText(this, "URL scheme in your app's " +
	                "manifest is not set up correctly. You should have a " +
	                "com.dropbox.client2.android.AuthActivity with the " +
	                "scheme: " + scheme, Toast.LENGTH_LONG).show();
	        finish();
	    }
	}

	private void clearKeys() {
	    SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
	    Editor edit = prefs.edit();
	    edit.clear();
	    edit.commit();
	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a local
	 * store, rather than storing user name & password, and re-authenticating each
	 * time (which is not to be done, ever).
	 *
	 * @return Array of [access_key, access_secret], or null if none stored
	 */
	private String[] getKeys() {
	    SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
	    String key = prefs.getString(ACCESS_KEY_NAME, null);
	    String secret = prefs.getString(ACCESS_SECRET_NAME, null);
	    if (key != null && secret != null) {
	    	String[] ret = new String[2];
	    	ret[0] = key;
	    	ret[1] = secret;
	    	return ret;
	    } else {
	    	return null;
	    }
	}
	
	private String getDropboxFile() {
		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
	    return prefs.getString(REMOTE_FILE, null);
	}
	
	private void setDropboxFile(String file) {
	    // Save the access key for later
	    SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
	    Editor edit = prefs.edit();
	    edit.putString(REMOTE_FILE, file);
	    edit.commit();
	}

	private void logIn() {
		Log.i(LOGCAT, "start authentication");
        // Start the remote authentication
        mApi.getSession().startAuthentication(DropboxActivity.this);
	}
	
	private void logOut() {
	    // remove info to access
	    mApi.getSession().unlink();
	    Log.i(LOGCAT, "unlink from dropbox account");

	    clearKeys();
	    // set UI not logged in
	    setLoggedIn(false);
	}

	private void refresh() {
    	if (mLoggedIn) {
    		// refersh user interface
    		new DirListDropBox(this, mApi, DROPBOX_ROOT).execute();
    	}
	}
	
	private boolean execute(TypeOperation operation) {
		String fileDropbox = null; 
		if (!mLoggedIn) { return false; }
		// check if an item selected
		if (lstFileDropbox.getCheckedItemPosition() == ListView.INVALID_POSITION) { 
			if (operation != TypeOperation.UPLOAD) { return false; }
			fileDropbox = "data_android.mmb"; // force name database to upload
		} else {
			// set filename
			fileDropbox = (String) lstFileDropbox.getItemAtPosition(lstFileDropbox.getCheckedItemPosition());
		}
		// close database
		MoneyManagerOpenHelper database = new MoneyManagerOpenHelper(DropboxActivity.this);
		database.getWritableDatabase().close();
		database.close();
		// create object of syncronization
		SyncDropBox syncDB = new SyncDropBox(DropboxActivity.this, mApi, fileDropbox);
		// check operation
		switch (operation) {
		case SYNC:
			syncDB.sync();
			break;
		case DONWLOAD:
			syncDB.download();
			break;
		case UPLOAD:
			syncDB.upload();
			break;
		}
		
		return true;
	}
	
	private void setLoggedIn(boolean loggedIn) {
		mLoggedIn = loggedIn;
		if (loggedIn) {
			txtLinkDropbox.setVisibility(View.GONE);
			lstFileDropbox.setVisibility(View.VISIBLE);
			if (mSyncInProgress == false) {
				refresh();
			}
		} else {
			txtLinkDropbox.setVisibility(View.VISIBLE);
			lstFileDropbox.setVisibility(View.GONE);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			invalidateOptionsMenu();
		}
		Log.i(LOGCAT, "drop account loggedin = " + Boolean.toString(mLoggedIn));
	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a local
	 * store, rather than storing user name & password, and re-authenticating each
	 * time (which is not to be done, ever).
	 */
	private void storeKeys(String key, String secret) {
	    // Save the access key for later
	    SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
	    Editor edit = prefs.edit();
	    edit.putString(ACCESS_KEY_NAME, key);
	    edit.putString(ACCESS_SECRET_NAME, secret);
	    edit.commit();
	}
	/**
	 * Force when progress dialog open the activity not change orientation
	 */
	private void forceCurrentOrientation() {
		mPrevOrientation = getRequestedOrientation(); // save current position
	    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	if (getWindowManager().getDefaultDisplay().getOrientation() == Surface.ROTATION_0 || getWindowManager().getDefaultDisplay().getOrientation() == Surface.ROTATION_90) {
	    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    	} else {
	    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
	    	}
	    } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    } else {
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
	    }
	}
	
	private void restoreOrietation(int prevOrietation) {
		setRequestedOrientation(prevOrietation);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		super.onCreate(savedInstanceState);
		Log.i(LOGCAT, "activity create");
		// actionbar
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setProgressBarIndeterminateVisibility(Boolean.FALSE);
		    setProgressBarVisibility(Boolean.FALSE);
		}
		// create session of dropbox
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        // contentview activity
        setContentView(R.layout.dropbox_activity);

        checkAppKeySetup();

        txtLinkDropbox = (TextView)findViewById(R.id.textViewLinkDropBox);
        lstFileDropbox = (ListView)findViewById(R.id.listviewFolderDropBox);
        
        // check if application is connected 
        setLoggedIn(mApi.getSession().isLinked());

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(LOGCAT, "activity destroy");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_dropbox, menu);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			menu.findItem(R.id.menu_link).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			menu.findItem(R.id.menu_unlink).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			menu.findItem(R.id.menu_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			menu.findItem(R.id.menu_sync).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.findItem(R.id.menu_download).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.findItem(R.id.menu_upload).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		String prefSync = application.getDropboxSyncMode();
		menu.findItem(R.id.menu_link).setVisible((mLoggedIn == false));
		menu.findItem(R.id.menu_unlink).setVisible(mLoggedIn == true);
		menu.findItem(R.id.menu_refresh).setVisible(mLoggedIn == true);
		menu.findItem(R.id.menu_sync).setVisible(mLoggedIn == true && prefSync.equalsIgnoreCase(getResources().getString(R.string.synchronize)));
		menu.findItem(R.id.menu_download).setVisible(mLoggedIn == true && (prefSync.equalsIgnoreCase(getResources().getString(R.string.synchronize)) || prefSync.equalsIgnoreCase(getResources().getString(R.string.download))));
		menu.findItem(R.id.menu_upload).setVisible(mLoggedIn == true && (prefSync.equalsIgnoreCase(getResources().getString(R.string.synchronize)) || prefSync.equalsIgnoreCase(getResources().getString(R.string.upload))));
		
		return super.onPrepareOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    case android.R.id.home:
	        // close this activity and come back main activity
	        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
	        this.finish();
	        break;
	    case R.id.menu_link:
	    	logIn();
	    	break;
	    case R.id.menu_unlink:
	    	logOut();
	    	break;
	    case R.id.menu_refresh:
	    	refresh();
	    	break;
	    case R.id.menu_sync:
	    	execute(getDropboxFile() != null ? TypeOperation.SYNC : TypeOperation.DONWLOAD);
	    	break;
	    case R.id.menu_download:
	    	execute(TypeOperation.DONWLOAD);
	    	break;
	    case R.id.menu_upload:
	    	execute(TypeOperation.UPLOAD);
	    	break;
		}
		return false;
	}
	
	@Override
	protected void onResume() {
        super.onResume();
        AndroidAuthSession session = mApi.getSession();
        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            try {
                // complete authentication
                session.finishAuthentication();
                // save login credentials 
                TokenPair tokens = session.getAccessTokenPair();
                storeKeys(tokens.key, tokens.secret);
                // set logged in true
                setLoggedIn(true);
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Couldn't authenticate with Dropbox:" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.i(LOGCAT, "Error authenticating", e);
            }
        }
	}
	
	public class DirListDropBox extends AsyncTask<Void, Long, Boolean> {
		private static final int FILE_LIMIT = 1000;
		// context and api and versio
		private Context mContext;
		private DropboxAPI<?> mApi;
		// path to find
		private String mPath;
		// error text
		private String mError;
		// file list
		ArrayList<Entry> mFiles = new ArrayList<Entry>();
		
		public DirListDropBox(Context context, DropboxAPI<?> api, String dropboxPath) {
			this.mContext = context;
			this.mApi = api;
			this.mPath = dropboxPath;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// set orientation
			forceCurrentOrientation();
			// show inderteminate progress
			setProgressBarIndeterminateVisibility(Boolean.TRUE);
		    setProgressBarVisibility(Boolean.TRUE);
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				Entry dirent = mApi.metadata(mPath, FILE_LIMIT, null, true, null);
				// check if path is dri
				if (!dirent.isDir) {
					mError = "Root is file!";
					return false;
				}

				mFiles.clear();
				// compose l'array list
				for(Entry file : dirent.contents) {
					if (file.isDir || file.isDeleted) {
						continue;
					} else {
						mFiles.add(file);
					}	
				}
			} catch (DropboxException e) {
				mError = e.getMessage();
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			lstFileDropbox.setAdapter(null);
			if (result) {
				// array list of file
				ArrayList<String> nameFiles = new ArrayList<String>();

				for(Entry file : mFiles) {
					nameFiles.add(mPath + file.fileName());
				}
 
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_multiple_choice, nameFiles);
				lstFileDropbox.setAdapter(adapter);
				// check into adapter file select
				String fileDropbox = getDropboxFile();
				if (!(fileDropbox == null)) {
					for(int i = 0; i < nameFiles.size(); i ++) {
						if (nameFiles.get(i).equals(fileDropbox)) {
							lstFileDropbox.setItemChecked(i, true);
							break;
						}
					}
				}
			} else {
				Toast.makeText(mContext, mError, Toast.LENGTH_LONG).show();
			}
			setProgressBarIndeterminateVisibility(Boolean.FALSE);
		    setProgressBarVisibility(Boolean.FALSE);
			// restore dell'orientazione
			restoreOrietation(mPrevOrientation);
		}
	}
	
	public class SyncDropBox extends AsyncTask<Void, Long, Boolean> {
		private static final int FILE_LIMIT = 1000;
		// gestione del tipo di sincronizzaizone
		private static final int NONE = 0;
		private static final int DOWNLOAD = 1;
		private static final int UPLOAD = 2;
		private static final int SYNCHRONIZE = 3;
		// referenza al contesto e alle API usate
		private Context mContext;
		private DropboxAPI<?> mApi;
		// percorso del file remoto
		private String mPath;
		// metodo di sincronizzazione
		private int mSync = NONE;
		// canale per lo download e upload del file
		private FileOutputStream mOutputStream;
		private FileInputStream mInputStream;
		// memorizzo la grandezza bytes del files
		private long mFileLen;
		// istanza request per l'upload
		UploadRequest mRequest;
		// messaggio di errore
		private String mVerbose;
		// flag che indica se ho visualizzato l'operazione che vado a fare
		private boolean showToast = false;
		/**
		 * ProgressListener per il download/upload dei file
		 * 
		 * @author a.lazzari
		 *
		 */
		private class SyncProgressListener extends ProgressListener {
			@Override
			public void onProgress(long bytes, long total) {
				mDialog.setProgress((int) bytes / 1000);
			}	
		}
		/**
		 * Costuttore dell'AsyncTask
		 * @param context
		 * @param api
		 * @param dropboxFile
		 */
		public SyncDropBox(Context context, DropboxAPI<?> api, String dropboxFile) {
			this.mContext = context;
			this.mApi = api;
			this.mPath = dropboxFile;
		}
		/**
		 * metodo di download del file remoto di dropbox
		 * @param params
		 * @return
		 */
		public AsyncTask<Void, Long, Boolean> download(Void...params) {
			// impsosto la modalit� solo download
			mSync = DOWNLOAD;
			// avvio la sincronizzazione
			return this.execute(params);
		}
		/**
		 * metodo di upload del file remoto di dropbox
		 * @param params
		 * @return
		 */
		public AsyncTask<Void, Long, Boolean> upload(Void...params) {
			// impsosto la modalit� solo download
			mSync = UPLOAD;
			// avvio la sincronizzazione
			return this.execute(params);
		}
		/**
		 * metodo di sincronizzazione del file remoto di dropbox con il locale
		 * @param params
		 * @return
		 */
		public AsyncTask<Void, Long, Boolean> sync(Void...params) {
			// impsosto la modalit� solo download
			mSync = SYNCHRONIZE;
			// avvio la sincronizzazione
			return this.execute(params);
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Forzo il mancato orientamento
			forceCurrentOrientation();
			// Creo il dialog
			mDialog = new ProgressDialog(this.mContext);
			mDialog.setMessage(this.mContext.getResources().getString(R.string.dropbox_syncProgress));
			mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mDialog.setProgressNumberFormat("%1dKB/%2dKB");
			mDialog.setCancelable(false);
			mDialog.setCanceledOnTouchOutside(false);
			mDialog.setMax(0);
		    // show del progress dialog
		    mDialog.show();
		    // imposto che facendo l'aggiornamento
		    mSyncInProgress = true;
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				File fileDatabase = new File(MoneyManagerOpenHelper.databasePath);
	            // prendo le info del file
	            Entry fileEntry = mApi.metadata(mPath, FILE_LIMIT, null, true, null);
	            // controllo che sia un file
	            if (fileEntry.isDir) {
	            	mVerbose = "Files to sync incorrect";
	            	return false;
	            }
	            //String hash = fileEntry.hash;
	            // controllo se devo fare la sincronizzazione
	            if (mSync == SYNCHRONIZE) {
	            	// prendo l'hash del file di download
		            Date dropModified = RESTUtility.parseDate(fileEntry.modified);
		            Date fileModified = new Date(fileDatabase.lastModified());
		            // controllo le date
		            if (dropModified.after(fileModified)) {
		            	mSync = DOWNLOAD;
		            } else if (dropModified.equals(fileModified)) {
		            	mSync = NONE;
		            } else {
		            	mSync = UPLOAD;
		            }
	            }
	            // se non devo fare niente perch� uguali esco
	            if (mSync == NONE) { return true; }
	            // memorizzo la grandezza in bytes
	            switch (mSync) {
	            case DOWNLOAD:
	            	mFileLen = fileEntry.bytes;
	            	break;
	            case UPLOAD:
	            	mFileLen = fileDatabase.length();
	            	break;
	            }
	            // imposto il massimo nella barra
	            mDialog.setMax((int) mFileLen / 1000);
	            publishProgress(null);
				// apro il canale sul database
				try {
					switch (mSync) {
					case DOWNLOAD:						
						mOutputStream = new FileOutputStream(MoneyManagerOpenHelper.databasePath);
						DropboxFileInfo info = mApi.getFile(mPath, null, mOutputStream, new SyncProgressListener());
						break;
					case UPLOAD:
						mInputStream = new FileInputStream(MoneyManagerOpenHelper.databasePath);
						mRequest = mApi.putFileOverwriteRequest(mPath, mInputStream, fileDatabase.length(), new SyncProgressListener());
			            if (mRequest != null) {
			                mRequest.upload();
			                return true;
			            }
					default:
						break;
					}	
	            } catch (FileNotFoundException e) {
	                mVerbose = e.getMessage();
	                return false;
	            }	            
			} catch (DropboxException e) {
				mVerbose = e.getMessage();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				// imposto il file selezionato per la sincronizzazione
				setDropboxFile(mPath);
				mVerbose = getResources().getString(R.string.synchronize_success);
			}
			Toast.makeText(mContext, mVerbose, Toast.LENGTH_LONG).show();
			// chiudo i canali
			try {
				if (mOutputStream != null) {
					mOutputStream.close();
				}
				if (mInputStream != null) {
					mInputStream.close();
				}
			} catch (IOException e) {
				Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			// chiudo la progress bar
			mDialog.dismiss();
			// restore la l'orientamento
			restoreOrietation(mPrevOrientation);
			// set finished syncronizzation
			mSyncInProgress = false;
			// set null preferences sdcard
			MoneyManagerApplication.setDatabasePath(getApplicationContext(), null);
			MainActivity.setRestartActivity(true);
		}
		@Override
		protected void onProgressUpdate(Long... values) {
			super.onProgressUpdate(values);
            if (showToast == false) {
				switch (mSync) {
	            case DOWNLOAD:
	            	Toast.makeText(mContext, getResources().getString(R.string.dropbox_downloadProgress), Toast.LENGTH_SHORT).show();
	            	break;
	            case UPLOAD:
	            	Toast.makeText(mContext.getApplicationContext(), getResources().getString(R.string.dropbox_uploadProgress), Toast.LENGTH_SHORT).show();
	            	break;
	            }
				// setto che ho visualizzato il toast
				showToast = true;
            }
            if (mDialog != null) {
            	if (mDialog.isShowing() == false) { mDialog.show(); }
            }
		}
	}
}
