package com.money.manager.ex;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.TableSplitTransactions;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.InputAmountDialog.InputAmountDialogListener;
import com.money.manager.ex.fragment.SplitItemFragment;
import com.money.manager.ex.fragment.SplitItemFragment.SplitItemFragmentCallbacks;

public class SplitTransactionsActivity extends BaseFragmentActivity implements SplitItemFragmentCallbacks, InputAmountDialogListener {
	public static final String KEY_SPLIT_TRANSACTION = "SplitTransactionsActivity:ArraysSplitTransaction";
	public static final String KEY_SPLIT_TRANSACTION_DELETED = "SplitTransactionsActivity:ArraysSplitTransactionDeleted";
	public static final String INTENT_RESULT_SPLIT_TRANSACTION = "SplitTransactionsActivity:ResultSplitTransaction";
	public static final String INTENT_RESULT_SPLIT_TRANSACTION_DELETED = "SplitTransactionsActivity:ResultSplitTransactionDeleted";
	
	private static final int MENU_ADD_SPLIT_TRANSACTION = 1;
	
	private ArrayList<TableSplitTransactions> mSplitTransactions = null;
	private ArrayList<TableSplitTransactions> mSplitDeleted = null;
	private static int mIdTag = 0x8000;
	
	private SplitItemFragment mFragmentInputAmountClick;
	
	private void addFragmentChild(TableSplitTransactions object) {
		String nameFragment = SplitItemFragment.class.getSimpleName() + "_" + Integer.toString(object.getSplitTransId() == -1 ? mIdTag ++ : object.getSplitTransId());
		SplitItemFragment fragment = (SplitItemFragment) getSupportFragmentManager().findFragmentByTag(nameFragment);
		if (fragment == null) {
			fragment = SplitItemFragment.newIstance(object);
			fragment.setOnSplitItemCallback(this);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			// animation
			// transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
			// Replace whatever is in the fragment_container view with this fragment,
			// and add the transaction to the back stack
			transaction.add(R.id.linearLayoutSplitTransaction, fragment, nameFragment);
			transaction.commit();
		}
	}
	
	public ArrayList<TableSplitTransactions> getAllTableSplitTransaction() {
		ArrayList<TableSplitTransactions> items = new ArrayList<TableSplitTransactions>();
		for(int i = 0; i < mIdTag; i ++) {
			String nameFragment = SplitItemFragment.class.getSimpleName() + "_" + Integer.toString(i);
			SplitItemFragment fragment = (SplitItemFragment)getSupportFragmentManager().findFragmentByTag(nameFragment);
			if (fragment != null && fragment.isVisible()) {
				items.add(fragment.getTableSplitTransactions());
			}
		}
		return items;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// set dialog mode
		setDialogMode(true);
		// load intent
		if (getIntent() != null) {
			mSplitTransactions = getIntent().getParcelableArrayListExtra(KEY_SPLIT_TRANSACTION);
			mSplitDeleted = getIntent().getParcelableArrayListExtra(KEY_SPLIT_TRANSACTION_DELETED);
		}
		// load deleted item
		if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SPLIT_TRANSACTION_DELETED)) {
			mSplitTransactions = savedInstanceState.getParcelableArrayList(KEY_SPLIT_TRANSACTION_DELETED);
		}
		
		// set view
		setContentView(R.layout.splittransaction_activity);
		
		Button buttonAdd = (Button)findViewById(R.id.buttonAdd);
		buttonAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addFragmentChild(new TableSplitTransactions());
			}
		});
		
		if (mSplitTransactions != null) {
			for(int i = 0; i < mSplitTransactions.size(); i ++) {
				addFragmentChild(mSplitTransactions.get(i));
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// item add
		/*MenuItem itemadd = menu.add(MENU_ADD_SPLIT_TRANSACTION, MENU_ADD_SPLIT_TRANSACTION, MENU_ADD_SPLIT_TRANSACTION, R.string.add);
		itemadd.setIcon(new Core(this).resolveIdAttribute(R.attr.ic_action_add));
		itemadd.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);*/

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == MENU_ADD_SPLIT_TRANSACTION) {
			addFragmentChild(new TableSplitTransactions());
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onRemoveItem(TableSplitTransactions object) {
		if (mSplitDeleted == null) {
			mSplitDeleted = new ArrayList<TableSplitTransactions>();
		}
		// add item to delete
		if (object.getSplitTransId() != -1) // not new split transaction
			mSplitDeleted.add(object);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mSplitDeleted != null) 
			outState.putParcelableArrayList(KEY_SPLIT_TRANSACTION_DELETED, mSplitDeleted);
	}
	
	@Override
	public boolean onActionCancelClick() {
		setResult(RESULT_CANCELED);
		finish();
		
		return true;
	}
	
	@Override
	public boolean onActionDoneClick() {
		ArrayList<TableSplitTransactions> items = getAllTableSplitTransaction();
		// check data
		for (int i = 0; i < items.size(); i ++) {
			TableSplitTransactions item = items.get(i);
			if (item.getCategId() == -1 && item.getCategId() == -1) {
				Core.alertDialog(SplitTransactionsActivity.this, R.string.error_category_not_selected).show();
				return false;
			}
		}
		Intent result = new Intent();
		result.putParcelableArrayListExtra(INTENT_RESULT_SPLIT_TRANSACTION, items);
		result.putParcelableArrayListExtra(INTENT_RESULT_SPLIT_TRANSACTION_DELETED, mSplitDeleted);
		setResult(RESULT_OK, result);
		finish();
		
		return true;
	}

	@Override
	public void onFinishedInputAmountDialog(int id, Double amount) {
		SplitItemFragment fragment = getFragmentInputAmountClick();
		if (fragment != null && fragment.isVisible() && fragment.isResumed()) {
			fragment.onFinishedInputAmountDialog(id, amount);
		}
	}

	public SplitItemFragment getFragmentInputAmountClick() {
		return mFragmentInputAmountClick;
	}

	public void setFragmentInputAmountClick(SplitItemFragment mFragmentInputAmountClick) {
		this.mFragmentInputAmountClick = mFragmentInputAmountClick;
	}
}
