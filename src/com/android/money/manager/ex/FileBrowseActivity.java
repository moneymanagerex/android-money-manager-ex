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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.android.money.manager.ex.fragment.MoneyListFragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
 * 
 */
public class FileBrowseActivity extends FragmentActivity {
	private static final String LOGCAT = "FileBrowseActivity";
	// definizione delle azioni custom possibili
	public static final String INTENT_VIEW_FILE = "com.android.money.manager.ex.custom.intent.action.VIEW_FILE";
	public static final String INTENT_VIEW_FOLDER = "com.android.money.manager.ex.custom.intent.action.VIEW_FOLDER";
	
	// definizione enumeratore delle visualizzazioni in funzione delle azioni
	private enum ACTION_VIEW {VIEW_FILE, VIEW_FOLDER, NO_MATCH};
	// definizione degli id menu e context
	private static final int MENU_UPLEVEL = 0;
	private static final int CONTEXT_MENU_SELECT = 1;
	private static final int CONTEXT_MENU_ABORT = 2;
	// definizione della referenze quando salva istanza
	private static final String SAVE_INSTANCE_FILE = "SAVE_ISTANCE_FILE";
	private static final String SAVE_INSTANCE_ACTION = "SAVE_ISTANCE_ACTION";
	// directory corrente dove ci si trova
	private static File mCurDir;
	// fragment visualizzato
	private BrowseFileListFragment listFragment = new BrowseFileListFragment();
	// modalità di visualizzazione scelta
	private static ACTION_VIEW actionView = ACTION_VIEW.NO_MATCH;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Imposto la root
		mCurDir = new File("/");
		// Controllo se arrivo da uno stato salvato
		if (savedInstanceState == null) {
			// prendo l'intent chiamante
			Intent intent = getIntent();
			if (!(intent == null)) {
				// prendo il file da cui partire
				mCurDir = new File(intent.getStringExtra(getPackageName() + ".init"));
				// prendo il tipo di azione
				if (intent.getAction().equals(INTENT_VIEW_FILE)) {
					actionView = ACTION_VIEW.VIEW_FILE;
				} else if (intent.getAction().equals(INTENT_VIEW_FOLDER)) {
					actionView = ACTION_VIEW.VIEW_FOLDER;
				}
			}
		} else {
			mCurDir = new File(savedInstanceState.getString(SAVE_INSTANCE_FILE));
			actionView = (ACTION_VIEW) savedInstanceState.getSerializable(SAVE_INSTANCE_ACTION);
		}
		// sistemo l'actionbar
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// prendo il fragmentmanager della Activity 
		FragmentManager fm = getSupportFragmentManager();
		// Creare il fragmet e lo aggiungo come il nostro unico contenuto
		if (fm.findFragmentById(android.R.id.content) == null) {
			fm.beginTransaction().add(android.R.id.content, listFragment).commit();
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_UPLEVEL, MENU_UPLEVEL,
				getResources().getString(R.string.upLevel))
				.setIcon(
						getResources().getDrawable(
								android.R.drawable.ic_menu_revert))
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(SAVE_INSTANCE_FILE, mCurDir.getPath());
		outState.putSerializable(SAVE_INSTANCE_ACTION, actionView);
	}

	public static void upLevelDirectory() {
		if (mCurDir.isDirectory()) {
			if (!(mCurDir.getParentFile() == null)) {
				// vado su di un livello e reinizializzo il loader
				mCurDir = mCurDir.getParentFile();
			}
		}
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!(mCurDir.getParentFile() == null)) {
				upLevelDirectory();
				listFragment.getLoaderManager().restartLoader(0, null, listFragment);
			}
		}
		return true;
	}

	public static class BrowseFileListFragment extends MoneyListFragment implements
			LoaderManager.LoaderCallbacks<List<File>> {
	
		// Long Item selected
		private View longItemView;
		// Addattore per la visualizzazione dei dati
		FileItemAdapter mAdapter;
		
		private void restartLoader() {
			getLoaderManager().restartLoader(0, null, this);
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			// Imposto il testo
			setEmptyText(getResources().getString(R.string.emptyFolder));
			// We have a menu item to show in action bar.
			setHasOptionsMenu(true);
			// Creo un adattatore vuoto
			mAdapter = new FileItemAdapter(getActivity());
			setListAdapter(mAdapter);
			// Avvio la visualizzazione del progressbar
			setListShown(false);
			// Imposto la presione lunga del tasti
			registerForContextMenu(getListView());
			// Preparo il loader
			getLoaderManager().initLoader(0, null, this);
		}
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			// prendo l'informazione sull'item selezione
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
			final File fileSelect = (File)getListView().getAdapter().getItem(info.position);
			// controllo se sono nelle modalità giuste
			if (actionView.equals(ACTION_VIEW.VIEW_FILE) && fileSelect.isDirectory()) { return; }
			if (actionView.equals(ACTION_VIEW.VIEW_FOLDER) && fileSelect.isFile()) { return; }
			// imposto il titolo
			menu.setHeaderTitle(fileSelect.getAbsolutePath().substring(mCurDir.getAbsolutePath().length()));
			// aggiungo l'item
			menu.add(0, CONTEXT_MENU_SELECT, CONTEXT_MENU_SELECT, getResources().getString(R.string.select));
		}
	
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(getActivity(), MainActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            getActivity().finish();
			case MENU_UPLEVEL:
				upLevelDirectory();
				getLoaderManager().restartLoader(0, null, this);
				break;
			}
			return false;
		}
		
		@Override
		public boolean onContextItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case CONTEXT_MENU_SELECT:
				Intent intentResult = new Intent();
				// prendo le informazioni del file
				AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
				File fileresult = (File)getListView().getAdapter().getItem(info.position);
				// prendo imposto le informazioni nell'intent
				intentResult.putExtra(getActivity().getPackageName() + ".result", fileresult.getPath());
				// chiudo l'activity e restituisco i dati
				getActivity().setResult(RESULT_OK, intentResult);
				getActivity().finish();
				break;
			}
			return false;
		}
	
		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			if (File.class.isInstance(v.getTag())) {
				if (((File) v.getTag()).isDirectory()) {
					mCurDir = (File) v.getTag();
					getLoaderManager().restartLoader(0, null, this);
				}
			}
		}
		
	
		@Override
		public Loader<List<File>> onCreateLoader(int id, Bundle args) {
			// Creo il loader
			return new BrowseFileListLoader(getActivity());
		}
	
		@Override
		public void onLoadFinished(Loader<List<File>> loader, List<File> data) {
			// quando finisco di caricare i dati nell'adattatore
			mAdapter.setData(data);
			// trascrivo sul subtitle dove mi trovo
			((FileBrowseActivity)getActivity()).getSupportActionBar().setSubtitle(mCurDir.getPath());
			// The list should now be shown.
			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
			}
		}
	
		@Override
		public void onLoaderReset(Loader<List<File>> loader) {
			// annullo i dati nell'adapter
			mAdapter.setData(null);			
		}
	}

	public static class BrowseFileListLoader extends
			AsyncTaskLoader<List<File>> {
	
		public BrowseFileListLoader(Context context) {
			super(context);
		}
	
		@Override
		protected void onStartLoading() {
			// Avvio AsyncTaskLoader
			forceLoad();
		}
	
		@Override
		public List<File> loadInBackground() {
			List<File> listDirs = new ArrayList<File>();
			List<File> listFiles = new ArrayList<File>();
			if (mCurDir.isDirectory()) {
				if (!(mCurDir.listFiles() == null)) {
					for (File file : mCurDir.listFiles()) {
						if (file.isDirectory()) {
							listDirs.add(file);
						} else {
							listFiles.add(file);
						}
					}
					// Ordino le due leiste
					Collections.sort(listDirs);
					Collections.sort(listFiles);
	
					// Accodo i file
					listDirs.addAll(listFiles);
	
					// Restituisco i valori
					return listDirs;
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	private static class FileItemAdapter extends ArrayAdapter<File> {

		private static final int layout = R.layout.item_file_browse;
		private LayoutInflater mInflater;

		public FileItemAdapter(Context context) {
			super(context, layout);
			//mInflater = LayoutInflater.from(context);
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			File file = getItem(position);

			if (convertView == null) {
				view = (LinearLayout) mInflater.inflate(layout, parent, false);
			} else {
				view = (LinearLayout) convertView;
			}

			// prendo i riferimenti
			TextView textFileItem = (TextView) view
					.findViewById(R.id.itemfiletext);
			ImageView imageFileItem = (ImageView) view
					.findViewById(R.id.itemfileimage);

			textFileItem.setText(file.getAbsolutePath().substring(
					mCurDir.getAbsolutePath().length()));

			// imposto l'icona
			if (file.isDirectory()) {
				imageFileItem.setImageResource(R.drawable.ic_folder);
			} else {
				imageFileItem.setImageResource(R.drawable.ic_file);
			}

			// set il tag nella view
			view.setTag(file);

			return view;
		}

		public void setData(List<File> data) {
			clear();
			if (data != null) {
				for(File item : data) {
					add(item);
				}
			}
		}
	}
}
