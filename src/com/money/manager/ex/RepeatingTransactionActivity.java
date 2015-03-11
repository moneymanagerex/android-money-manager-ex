/*
 * Copyright (C) 2012-2014 Alessandro Lazzari
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
 */
package com.money.manager.ex;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.TableCheckingAccount;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.database.TableSubCategory;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.InputAmountDialog;
import com.money.manager.ex.fragment.InputAmountDialog.InputAmountDialogListener;
import com.money.manager.ex.utils.CurrencyUtils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 */
public class RepeatingTransactionActivity extends BaseFragmentActivity implements InputAmountDialogListener {
    // KEY INTENT per il passaggio dei dati
    public static final String KEY_BILL_DEPOSITS_ID = "RepeatingTransaction:BillDepositsId";
    public static final String KEY_ACCOUNT_ID = "RepeatingTransaction:AccountId";
    public static final String KEY_TO_ACCOUNT_ID = "RepeatingTransaction:ToAccountId";
    public static final String KEY_TO_ACCOUNT_NAME = "RepeatingTransaction:ToAccountName";
    public static final String KEY_TRANS_CODE = "RepeatingTransaction:TransCode";
    public static final String KEY_TRANS_STATUS = "RepeatingTransaction:TransStatus";
    public static final String KEY_TRANS_DATE = "RepeatingTransaction:TransDate";
    public static final String KEY_TRANS_AMOUNT = "RepeatingTransaction:TransAmount";
    public static final String KEY_TRANS_TOTAMOUNT = "RepeatingTransaction:TransTotAmount";
    public static final String KEY_PAYEE_ID = "RepeatingTransaction:PayeeId";
    public static final String KEY_PAYEE_NAME = "RepeatingTransaction:PayeeName";
    public static final String KEY_CATEGORY_ID = "RepeatingTransaction:CategoryId";
    public static final String KEY_CATEGORY_NAME = "RepeatingTransaction:CategoryName";
    public static final String KEY_SUBCATEGORY_ID = "RepeatingTransaction:SubCategoryId";
    public static final String KEY_SUBCATEGORY_NAME = "RepeatingTransaction:SubCategoryName";
    public static final String KEY_NOTES = "RepeatingTransaction:Notes";
    public static final String KEY_TRANS_NUMBER = "RepeatingTransaction:TransNumber";
    public static final String KEY_NEXT_OCCURRENCE = "RepeatingTransaction:NextOccurrence";
    public static final String KEY_REPEATS = "RepeatingTransaction:Repeats";
    public static final String KEY_NUM_OCCURRENCE = "RepeatingTransaction:NumOccurrence";
    public static final String KEY_ACTION = "RepeatingTransaction:Action";
    private static final String LOGCAT = RepeatingTransactionActivity.class.getSimpleName();
    // ID REQUEST Data
    private static final int REQUEST_PICK_PAYEE = 1;
    private static final int REQUEST_PICK_CATEGORY = 3;
    // object of the table
    TableBillsDeposits mRepeatingTransaction = new TableBillsDeposits();
    // action type intent
    private String mIntentAction;
    // id account from and id ToAccount
    private int mAccountId = -1, mToAccountId = -1;
    private List<TableAccountList> mAccountList;
    private String mToAccountName;
    private int mBillDepositsId = -1;
    private String mTransCode, mStatus;
    // info payee
    private int mPayeeId = -1;
    private String mPayeeName, mTextDefaultPayee;
    // info category and subcategory
    private int mCategoryId = -1, mSubCategoryId = -1;
    private String mCategoryName, mSubCategoryName;
    // arrays to manage transcode and status
    private String[] mTransCodeItems, mStatusItems;
    private String[] mTransCodeValues, mStatusValues;
    // arrayslist accountname and accountid
    private ArrayList<String> mAccountNameList = new ArrayList<String>();
    private ArrayList<Integer> mAccountIdList = new ArrayList<Integer>();
    // amount
    private double mTotAmount = 0, mAmount = 0;
    // notes
    private String mNotes = "";
    // next occurrance
    private String mNextOccurrence = "";
    private int mFrequencies = 0;
    private int mNumOccurrence = -1;
    // transaction numbers
    private String mTransNumber = "";
    // reference view into layout
    private Spinner spinAccount, spinToAccount, spinTransCode, spinStatus, spinFrequencies;
    private ImageButton btnTransNumber;
    private EditText edtTransNumber, edtNotes, edtTimesRepeated;
    private TextView txtPayee, txtSelectPayee, txtSelectCategory, txtCaptionAmount, txtRepeats, txtTimesRepeated, txtNextOccurrence, txtTotAmount, txtAmount;

    /**
     * getCategoryFromPayee set last category used from payee
     *
     * @param payeeId Identify of payee
     * @return true if category set
     */
    private boolean getCategoryFromPayee(int payeeId) {
        boolean ret = false;
        // take data of payee
        TablePayee payee = new TablePayee();
        Cursor curPayee = getContentResolver().query(payee.getUri(), payee.getAllColumns(), "PAYEEID=" + Integer.toString(payeeId), null, null);
        // check cursor is valid
        if ((curPayee != null) && (curPayee.moveToFirst())) {
            // chek if category is valid
            if (curPayee.getInt(curPayee.getColumnIndex(TablePayee.CATEGID)) != -1) {
                mCategoryId = curPayee.getInt(curPayee.getColumnIndex(TablePayee.CATEGID));
                mSubCategoryId = curPayee.getInt(curPayee.getColumnIndex(TablePayee.SUBCATEGID));
                // create instance of query
                QueryCategorySubCategory category = new QueryCategorySubCategory(getApplicationContext());
                // compose selection
                String where = "CATEGID=" + Integer.toString(mCategoryId) + " AND SUBCATEGID=" + Integer.toString(mSubCategoryId);
                Cursor curCategory = getContentResolver().query(category.getUri(), category.getAllColumns(), where, null, null);
                // check cursor is valid
                if ((curCategory != null) && (curCategory.moveToFirst())) {
                    // take names of category and subcategory
                    mCategoryName = curCategory.getString(curCategory.getColumnIndex(QueryCategorySubCategory.CATEGNAME));
                    mSubCategoryName = curCategory.getString(curCategory.getColumnIndex(QueryCategorySubCategory.SUBCATEGNAME));
                    // return true
                    ret = true;
                }
            }
        }

        return ret;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_PAYEE:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    mPayeeId = data.getIntExtra(PayeeActivity.INTENT_RESULT_PAYEEID, -1);
                    mPayeeName = data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME);
                    // select last category used from payee
                    if (getCategoryFromPayee(mPayeeId)) {
                        refreshCategoryName(); // refresh UI
                    }
                    // refresh UI
                    refreshPayeeName();
                }
                break;
            case REQUEST_PICK_CATEGORY:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    mCategoryId = data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_CATEGID, -1);
                    mCategoryName = data.getStringExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_CATEGNAME);
                    mSubCategoryId = data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_SUBCATEGID, -1);
                    mSubCategoryName = data.getStringExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_SUBCATEGNAME);
                    // refresh UI category
                    refreshCategoryName();
                }
        }
    }

    @Override
    public boolean onActionCancelClick() {
        Core core = new Core(getApplicationContext());
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(android.R.string.cancel)
                .content(R.string.transaction_cancel_confirm)
                .positiveText(R.string.keep_editing)
                .negativeText(R.string.discard)
                .theme(core.getThemeApplication() == R.style.Theme_Money_Manager ? Theme.DARK : Theme.LIGHT)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        setResult(RESULT_CANCELED);
                        finish();
                        super.onNegative(dialog);
                    }
                })
                .build();
        dialog.show();
        return true;
    }

    @Override
    public void onBackPressed() {
        onActionCancelClick();
    }

    @Override
    public boolean onActionDoneClick() {
        if (updateData() == true) {
            // set result ok, send broadcast to update widgets and finish activity
            setResult(RESULT_OK);
            finish();
        }

        return super.onActionDoneClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setDialogMode(Boolean.TRUE);

        super.onCreate(savedInstanceState);

        Core core = new Core(getApplicationContext());
        // manage save instance
        if ((savedInstanceState != null)) {
            mBillDepositsId = savedInstanceState.getInt(KEY_BILL_DEPOSITS_ID);
            mAccountId = savedInstanceState.getInt(KEY_ACCOUNT_ID);
            mToAccountId = savedInstanceState.getInt(KEY_TO_ACCOUNT_ID);
            mToAccountName = savedInstanceState.getString(KEY_TO_ACCOUNT_NAME);
            mTransCode = savedInstanceState.getString(KEY_TRANS_CODE);
            mStatus = savedInstanceState.getString(KEY_TRANS_STATUS);
            mAmount = savedInstanceState.getDouble(KEY_TRANS_AMOUNT);
            mTotAmount = savedInstanceState.getDouble(KEY_TRANS_TOTAMOUNT);
            mPayeeId = savedInstanceState.getInt(KEY_PAYEE_ID);
            mPayeeName = savedInstanceState.getString(KEY_PAYEE_NAME);
            mCategoryId = savedInstanceState.getInt(KEY_CATEGORY_ID);
            mCategoryName = savedInstanceState.getString(KEY_CATEGORY_NAME);
            mSubCategoryId = savedInstanceState.getInt(KEY_SUBCATEGORY_ID);
            mSubCategoryName = savedInstanceState.getString(KEY_SUBCATEGORY_NAME);
            mNotes = savedInstanceState.getString(KEY_NOTES);
            mTransNumber = savedInstanceState.getString(KEY_TRANS_NUMBER);
            mNextOccurrence = savedInstanceState.getString(KEY_NEXT_OCCURRENCE);
            mFrequencies = savedInstanceState.getInt(KEY_REPEATS);
            mNumOccurrence = savedInstanceState.getInt(KEY_NUM_OCCURRENCE);
            // action
            mIntentAction = savedInstanceState.getString(KEY_ACTION);
        }
        // manage intent
        if (getIntent() != null) {
            if (savedInstanceState == null) {
                mAccountId = getIntent().getIntExtra(KEY_ACCOUNT_ID, -1);
                if (getIntent().getAction() != null && Intent.ACTION_EDIT.equals(getIntent().getAction())) {
                    mBillDepositsId = getIntent().getIntExtra(KEY_BILL_DEPOSITS_ID, -1);
                    // select data transaction
                    selectRepeatingTransaction(mBillDepositsId);
                }
            }
            mIntentAction = getIntent().getAction();
            // set title
            getSupportActionBar().setTitle(Constants.INTENT_ACTION_INSERT.equals(mIntentAction) ? R.string.new_repeating_transaction : R.string.edit_repeating_transaction);
        }
        // compose layout
        setContentView(R.layout.repeatingtransaction_activity);
        // take a reference view into layout
        txtPayee = (TextView) findViewById(R.id.textViewPayee);
        txtCaptionAmount = (TextView) findViewById(R.id.textViewHeaderTotalAmount);
        txtRepeats = (TextView) findViewById(R.id.textViewRepeat);
        txtTimesRepeated = (TextView) findViewById(R.id.textViewTimesRepeated);

        txtSelectPayee = (TextView) findViewById(R.id.textViewSelectPayee);
        txtSelectCategory = (TextView) findViewById(R.id.textViewSelectCategory);

        spinAccount = (Spinner) findViewById(R.id.spinnerAccount);
        spinToAccount = (Spinner) findViewById(R.id.spinnerToAccount);
        spinTransCode = (Spinner) findViewById(R.id.spinnerTransCode);
        spinStatus = (Spinner) findViewById(R.id.spinnerStatus);
        spinFrequencies = (Spinner) findViewById(R.id.spinnerFrequencies);

        // listener on dialog amount edittext
        OnClickListener onClickAmount = new OnClickListener() {

            @Override
            public void onClick(View v) {
                Integer currencyId = null;
                if (spinAccount.getSelectedItemPosition() >= 0
                        && spinAccount.getSelectedItemPosition() < mAccountList.size()) {
                    currencyId = mAccountList.get(spinAccount.getSelectedItemPosition()).getCurrencyId();
                }
                double amount = (Double) ((TextView) v).getTag();
                InputAmountDialog dialog = InputAmountDialog.getInstance(v.getId(), amount, currencyId);
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };

        // accountlist <> to populate the spin
        mAccountList = MoneyManagerOpenHelper.getInstance(getApplicationContext()).getListAccounts(core.getAccountsOpenVisible(), core.getAccountFavoriteVisible());
        for (int i = 0; i <= mAccountList.size() - 1; i++) {
            mAccountNameList.add(mAccountList.get(i).getAccountName());
            mAccountIdList.add(mAccountList.get(i).getAccountId());
        }

        txtTotAmount = (TextView) findViewById(R.id.editTextTotAmount);
        txtTotAmount.setOnClickListener(onClickAmount);
        core.formatAmountTextView(txtTotAmount, mTotAmount, getCurrencyIdFromAccountId(!Constants.TRANSACTION_TYPE_TRANSFER.equals(mTransCode) ? mAccountId : mToAccountId));

        txtAmount = (TextView) findViewById(R.id.editTextAmount);
        txtAmount.setOnClickListener(onClickAmount);
        //core.formatAmountTextView(txtAmount, mAmount, currencyId);
        core.formatAmountTextView(txtAmount, mAmount, getCurrencyIdFromAccountId(!Constants.TRANSACTION_TYPE_TRANSFER.equals(mTransCode) ? mToAccountId : mAccountId));

        // create adapter for spinAccount
        ArrayAdapter<String> adapterAccount = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mAccountNameList);
        adapterAccount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinAccount.setAdapter(adapterAccount);
        // select current value
        if (mAccountIdList.indexOf(mAccountId) >= 0) {
            spinAccount.setSelection(mAccountIdList.indexOf(mAccountId), true);
        }
        spinAccount.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Core core = new Core(getBaseContext());
                if ((position >= 0) && (position <= mAccountIdList.size())) {
                    mAccountId = mAccountIdList.get(position);
                    if (Constants.TRANSACTION_TYPE_TRANSFER.equals(mTransCode)) {
                        core.formatAmountTextView(txtAmount, (Double) txtAmount.getTag(), getCurrencyIdFromAccountId(mAccountId));
                    } else {
                        core.formatAmountTextView(txtTotAmount, (Double) txtTotAmount.getTag(), getCurrencyIdFromAccountId(mAccountId));
                    }
                    refreshHeaderAmount();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinToAccount.setAdapter(adapterAccount);
        if (mAccountIdList.indexOf(mToAccountId) >= 0) {
            spinToAccount.setSelection(mAccountIdList.indexOf(mToAccountId), true);
        } else {
            spinToAccount.setSelection(Spinner.INVALID_POSITION, true);
        }
        spinToAccount.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((position >= 0) && (position <= mAccountIdList.size())) {
                    mToAccountId = mAccountIdList.get(position);
                    Core core = new Core(getBaseContext());
                    core.formatAmountTextView(txtAmount, (Double) txtAmount.getTag(), getCurrencyIdFromAccountId(mAccountId));
                    core.formatAmountTextView(txtTotAmount, (Double) txtTotAmount.getTag(), getCurrencyIdFromAccountId(mToAccountId));
                    refreshHeaderAmount();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // populate arrays TransCode
        mTransCodeItems = getResources().getStringArray(R.array.transcode_items);
        mTransCodeValues = getResources().getStringArray(R.array.transcode_values);
        // create adapter for TransCode
        ArrayAdapter<String> adapterTrans = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mTransCodeItems);
        adapterTrans.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinTransCode.setAdapter(adapterTrans);
        // select a current value
        if (TextUtils.isEmpty(mTransCode) == false) {
            if (Arrays.asList(mTransCodeValues).indexOf(mTransCode) >= 0) {
                spinTransCode.setSelection(Arrays.asList(mTransCodeValues).indexOf(mTransCode), true);
            }
        } else {
            mTransCode = (String) spinTransCode.getSelectedItem();
        }
        spinTransCode.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((position >= 0) && (position <= mTransCodeValues.length)) {
                    mTransCode = mTransCodeValues[position];
                }
                // aggiornamento dell'interfaccia grafica
                refreshTransCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        txtSelectPayee.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RepeatingTransactionActivity.this, PayeeActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, REQUEST_PICK_PAYEE);
            }
        });
        txtSelectCategory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RepeatingTransactionActivity.this, CategorySubCategoryExpandableListActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, REQUEST_PICK_CATEGORY);
            }
        });
        // arrays to manage Status
        mStatusItems = getResources().getStringArray(R.array.status_items);
        mStatusValues = getResources().getStringArray(R.array.status_values);
        // create adapter for spinnerStatus
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mStatusItems);
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinStatus.setAdapter(adapterStatus);
        // select current value
        if (TextUtils.isEmpty(mStatus) == false) {
            if (Arrays.asList(mStatusValues).indexOf(mStatus) >= 0) {
                spinStatus.setSelection(Arrays.asList(mStatusValues).indexOf(mStatus), true);
            }
        } else {
            mStatus = (String) spinStatus.getSelectedItem();
        }
        spinStatus.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((position >= 0) && (position <= mStatusValues.length)) {
                    mStatus = mStatusValues[position];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // transaction number
        edtTransNumber = (EditText) findViewById(R.id.editTextTransNumber);
        if (!TextUtils.isEmpty(mTransNumber)) {
            edtTransNumber.setText(mTransNumber);
        }
        btnTransNumber = (ImageButton) findViewById(R.id.buttonTransNumber);
        btnTransNumber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MoneyManagerOpenHelper helper = MoneyManagerOpenHelper.getInstance(getApplicationContext());
                String query = "SELECT MAX(" + TableCheckingAccount.TRANSACTIONNUMBER + ") FROM " +
                        new TableCheckingAccount().getSource() + " WHERE " +
                        TableCheckingAccount.ACCOUNTID + "=?";
                Cursor cursor = helper.getReadableDatabase().rawQuery(query, new String[]{Integer.toString(mAccountId)});
                if (cursor != null && cursor.moveToFirst()) {
                    String transNumber = cursor.getString(0);
                    if (TextUtils.isEmpty(transNumber)) {
                        transNumber = "0";
                    }
                    if ((!TextUtils.isEmpty(transNumber)) && TextUtils.isDigitsOnly(transNumber)) {
                        edtTransNumber.setText(Integer.toString(Integer.parseInt(transNumber) + 1));
                    }
                    cursor.close();
                }
                //helper.close();
            }
        });
        // notes
        edtNotes = (EditText) findViewById(R.id.editTextNotes);
        if (!(TextUtils.isEmpty(mNotes))) {
            edtNotes.setText(mNotes);
        }
        // next occurrence
        txtNextOccurrence = (TextView) findViewById(R.id.editTextNextOccurrence);

        if (!(TextUtils.isEmpty(mNextOccurrence))) {
            try {
                txtNextOccurrence.setTag(new SimpleDateFormat("yyyy-MM-dd").parse(mNextOccurrence));
            } catch (ParseException e) {
                Log.e(LOGCAT, e.getMessage());
            }
        } else {
            txtNextOccurrence.setTag((Date) Calendar.getInstance().getTime());
        }
        formatExtendedDate(txtNextOccurrence);
        txtNextOccurrence.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar date = Calendar.getInstance();
                date.setTime((Date) txtNextOccurrence.getTag());
                DatePickerDialog dialog = new DatePickerDialog(RepeatingTransactionActivity.this, mDateSetListener, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
                dialog.show();
            }

            public DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    try {
                        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) + "-" + Integer.toString(dayOfMonth));
                        txtNextOccurrence.setTag(date);
                        formatExtendedDate(txtNextOccurrence);
                    } catch (Exception e) {
                        Log.e(LOGCAT, e.getMessage());
                    }

                }
            };

        });

        // times repeated
        edtTimesRepeated = (EditText) findViewById(R.id.editTextTimesRepeated);
        if (mNumOccurrence >= 0) {
            edtTimesRepeated.setText(Integer.toString(mNumOccurrence));
        }
        // frequencies
        if (mFrequencies >= 200) {
            mFrequencies = mFrequencies - 200;
        } // set auto execute without user acknowlegement
        if (mFrequencies >= 100) {
            mFrequencies = mFrequencies - 100;
        } // set auto execute on the next occurrence
        spinFrequencies.setSelection(mFrequencies, true);
        spinFrequencies.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mFrequencies = position;
                refreshTimesRepeated();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mFrequencies = 0;
                refreshTimesRepeated();
            }
        });
        // refresh user interface
        refreshTransCode();
        refreshPayeeName();
        refreshCategoryName();
        refreshTimesRepeated();
    }

    /**
     * refersh UI control times repeated
     */
    public void refreshTimesRepeated() {
        edtTimesRepeated.setVisibility(mFrequencies > 0 ? View.VISIBLE : View.GONE);
        txtRepeats.setText((mFrequencies == 11) || (mFrequencies == 12) ? R.string.activates : R.string.repeats);
        txtTimesRepeated.setVisibility(mFrequencies > 0 ? View.VISIBLE : View.GONE);
        txtTimesRepeated.setText(mFrequencies >= 11 ? R.string.activates : R.string.times_repeated);
        edtTimesRepeated.setHint(mFrequencies >= 11 ? R.string.activates : R.string.times_repeated);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the state interface
        outState.putInt(KEY_BILL_DEPOSITS_ID, mBillDepositsId);
        outState.putInt(KEY_ACCOUNT_ID, mAccountId);
        outState.putInt(KEY_TO_ACCOUNT_ID, mToAccountId);
        outState.putString(KEY_TO_ACCOUNT_NAME, mToAccountName);
        outState.putString(KEY_TRANS_CODE, mTransCode);
        outState.putString(KEY_TRANS_STATUS, mStatus);
        outState.putDouble(KEY_TRANS_TOTAMOUNT, (Double) txtTotAmount.getTag());
        outState.putDouble(KEY_TRANS_AMOUNT, (Double) txtAmount.getTag());
        outState.putInt(KEY_PAYEE_ID, mPayeeId);
        outState.putString(KEY_PAYEE_NAME, mPayeeName);
        outState.putInt(KEY_CATEGORY_ID, mCategoryId);
        outState.putString(KEY_CATEGORY_NAME, mCategoryName);
        outState.putInt(KEY_SUBCATEGORY_ID, mSubCategoryId);
        outState.putString(KEY_SUBCATEGORY_NAME, mSubCategoryName);
        outState.putString(KEY_TRANS_NUMBER, edtTransNumber.getText().toString());
        outState.putString(KEY_NOTES, String.valueOf(edtNotes.getTag()));
        outState.putString(KEY_NEXT_OCCURRENCE, new SimpleDateFormat("yyyy-MM-dd").format(txtNextOccurrence.getTag()));
        outState.putInt(KEY_REPEATS, mFrequencies);
        if (!TextUtils.isEmpty(edtTimesRepeated.getText())) {
            outState.putInt(KEY_NUM_OCCURRENCE, Integer.parseInt(edtTimesRepeated.getText().toString()));
        } else {
            outState.putInt(KEY_NUM_OCCURRENCE, -1);
        }
        outState.putString(KEY_ACTION, mIntentAction);
    }

    /**
     * query info payee
     *
     * @param accountId id payee
     * @return true if the data selected
     */
    private boolean selectAccountName(int accountId) {
        TableAccountList account = new TableAccountList();
        Cursor cursor = getContentResolver().query(account.getUri(),
                account.getAllColumns(),
                TableAccountList.ACCOUNTID + "=?",
                new String[]{Integer.toString(accountId)}, null);
        // check if cursor is valid and open
        if ((cursor == null) || (cursor.moveToFirst() == false)) {
            return false;
        }

        // set payeename
        mToAccountName = cursor.getString(cursor.getColumnIndex(TableAccountList.ACCOUNTNAME));

        return true;
    }

    /**
     * Query info of Category and Subcategory
     *
     * @param categoryId
     * @param subCategoryId
     * @return
     */
    private boolean selectCategSubName(int categoryId, int subCategoryId) {
        TableCategory category = new TableCategory();
        TableSubCategory subCategory = new TableSubCategory();
        Cursor cursor;
        // category
        cursor = getContentResolver().query(category.getUri(), category.getAllColumns(), TableCategory.CATEGID + "=?", new String[]{Integer.toString(categoryId)}, null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            // set category name and sub category name
            mCategoryName = cursor.getString(cursor.getColumnIndex(TableCategory.CATEGNAME));
        } else {
            mCategoryName = null;
        }
        // sub-category
        cursor = getContentResolver().query(subCategory.getUri(), subCategory.getAllColumns(), TableSubCategory.SUBCATEGID + "=?", new String[]{Integer.toString(subCategoryId)}, null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            // set category name and sub category name
            mSubCategoryName = cursor.getString(cursor.getColumnIndex(TableSubCategory.SUBCATEGNAME));
        } else {
            mSubCategoryName = null;
        }

        return true;
    }

    /**
     * query info payee
     *
     * @param payeeId id payee
     * @return true if the data selected
     */
    private boolean selectPayeeName(int payeeId) {
        TablePayee payee = new TablePayee();
        Cursor cursor = getContentResolver().query(payee.getUri(),
                payee.getAllColumns(),
                TablePayee.PAYEEID + "=?",
                new String[]{Integer.toString(payeeId)}, null);
        // check if cursor is valid and open
        if ((cursor == null) || (cursor.moveToFirst() == false)) {
            return false;
        }

        // set payeename
        mPayeeName = cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME));

        return true;
    }

    /**
     * this method allows you to search the transaction data
     *
     * @param billId transaction id
     * @return true if data selected, false nothing
     */
    private boolean selectRepeatingTransaction(int billId) {
        Cursor cursor = getContentResolver().query(mRepeatingTransaction.getUri(),
                mRepeatingTransaction.getAllColumns(),
                TableBillsDeposits.BDID + "=?",
                new String[]{Integer.toString(billId)}, null);
        // check if cursor is valid and open
        if ((cursor == null) || (cursor.moveToFirst() == false)) {
            return false;
        }

        // take a data
        mBillDepositsId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.BDID));
        mAccountId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.ACCOUNTID));
        mToAccountId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.TOACCOUNTID));
        mTransCode = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.TRANSCODE));
        mStatus = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.STATUS));
        mAmount = (double) cursor.getDouble(cursor.getColumnIndex(TableBillsDeposits.TRANSAMOUNT));
        mTotAmount = (double) cursor.getDouble(cursor.getColumnIndex(TableBillsDeposits.TOTRANSAMOUNT));
        mPayeeId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.PAYEEID));
        mCategoryId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.CATEGID));
        mSubCategoryId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.SUBCATEGID));
        mTransNumber = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.TRANSACTIONNUMBER));
        mNotes = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.NOTES));
        mNextOccurrence = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.NEXTOCCURRENCEDATE));
        mFrequencies = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.REPEATS));
        mNumOccurrence = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.NUMOCCURRENCES));

        selectAccountName(mToAccountId);
        selectPayeeName(mPayeeId);
        selectCategSubName(mCategoryId, mSubCategoryId);

        return true;
    }

    public void refreshCategoryName() {
        String category = "";
        if (TextUtils.isEmpty(mCategoryName) == false) {
            category = mCategoryName + (TextUtils.isEmpty(mSubCategoryName) == false ? " : " + mSubCategoryName : "");
        }
        // write into text button category/subcategory
        txtSelectCategory.setText(category);
    }

    /**
     * update data into database
     *
     * @return true if update data successful
     */
    private boolean updateData() {
        if (validateData() == false) {
            return false;
        }
        // content value for insert or update data
        ContentValues values = new ContentValues();

        values.put(TableBillsDeposits.ACCOUNTID, mAccountId);
        if (Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode)) {
            values.put(TableBillsDeposits.TOACCOUNTID, mToAccountId);
            values.put(TableBillsDeposits.PAYEEID, -1);
        } else {
            values.put(TableBillsDeposits.PAYEEID, mPayeeId);
        }
        values.put(TableBillsDeposits.TRANSCODE, mTransCode);
        if (TextUtils.isEmpty(txtAmount.getText().toString()) || (!(Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode)))) {
            values.put(TableBillsDeposits.TRANSAMOUNT, (Double) txtTotAmount.getTag());
        } else {
            values.put(TableBillsDeposits.TRANSAMOUNT, (Double) txtAmount.getTag());
        }
        values.put(TableBillsDeposits.STATUS, mStatus);
        values.put(TableBillsDeposits.CATEGID, mCategoryId);
        values.put(TableBillsDeposits.SUBCATEGID, mSubCategoryId);
        values.put(TableBillsDeposits.FOLLOWUPID, -1);
        values.put(TableBillsDeposits.TOTRANSAMOUNT, (Double) txtTotAmount.getTag());
        values.put(TableBillsDeposits.TRANSACTIONNUMBER, edtTransNumber.getText().toString());
        values.put(TableBillsDeposits.NOTES, edtNotes.getText().toString());
        values.put(TableBillsDeposits.NEXTOCCURRENCEDATE, new SimpleDateFormat("yyyy-MM-dd").format(txtNextOccurrence.getTag()));
        values.put(TableBillsDeposits.TRANSDATE, new SimpleDateFormat("yyyy-MM-dd").format(txtNextOccurrence.getTag()));
        values.put(TableBillsDeposits.REPEATS, mFrequencies);
        values.put(TableBillsDeposits.NUMOCCURRENCES, mFrequencies > 0 ? edtTimesRepeated.getText().toString() : null);

        // check whether the application should do the update or insert
        if (Constants.INTENT_ACTION_INSERT.equals(mIntentAction)) {
            // insert
            if (getContentResolver().insert(mRepeatingTransaction.getUri(), values) == null) {
                Core.alertDialog(this, R.string.db_checking_insert_failed).show();
                Log.w(LOGCAT, "Insert new repeating transaction failed!");
                return false;
            }
        } else {
            // update
            if (getContentResolver().update(mRepeatingTransaction.getUri(), values, TableBillsDeposits.BDID + "=?", new String[]{Integer.toString(mBillDepositsId)}) <= 0) {
                Core.alertDialog(this, R.string.db_checking_update_failed).show();
                Log.w(LOGCAT, "Update repeating  transaction failed!");
                return false;
            }
        }

        return true;
    }

    /**
     * update UI interface with PayeeName
     */
    public void refreshPayeeName() {
        // write into text button payee name
        txtSelectPayee.setText(TextUtils.isEmpty(mPayeeName) == false ? mPayeeName : mTextDefaultPayee);
    }

    public void refreshTransCode() {
        TextView txtFromAccount = (TextView) findViewById(R.id.textViewFromAccount);
        TextView txtToAccount = (TextView) findViewById(R.id.textViewToAccount);

        txtFromAccount.setText(Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode) ? R.string.from_account : R.string.account);
        txtToAccount.setVisibility(Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode) ? View.VISIBLE : View.GONE);

        txtCaptionAmount.setVisibility(Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode) ? View.VISIBLE : View.GONE);
        txtAmount.setVisibility(Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode) ? View.VISIBLE : View.GONE);
        spinToAccount.setVisibility(Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode) ? View.VISIBLE : View.GONE);
        //txtPayee.setVisibility(!Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode) ? View.VISIBLE : View.GONE);
        txtSelectPayee.setVisibility(!Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode) ? View.VISIBLE : View.GONE);

        refreshHeaderAmount();
    }

    /**
     * validate data insert in activity
     *
     * @return
     */
    private boolean validateData() {
        if ((Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode)) && (mToAccountId == -1)) {
            Core.alertDialog(this, R.string.error_toaccount_not_selected).show();
            ;
            return false;
        } else if ((!Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode)) && (mPayeeId == -1)) {
            Core.alertDialog(this, R.string.error_payee_not_selected).show();
            ;
            return false;
        }
        if (mCategoryId == -1) {
            Core.alertDialog(this, R.string.error_category_not_selected).show();
            ;
            return false;
        }
        if (TextUtils.isEmpty(txtTotAmount.getText())) {
            if (TextUtils.isEmpty(txtAmount.getText())) {
                Core.alertDialog(this, R.string.error_totamount_empty).show();
                ;
                return false;
            } else {
                txtTotAmount.setText(txtAmount.getText());
            }
        }
        if (TextUtils.isEmpty(txtNextOccurrence.getText().toString())) {
            Core.alertDialog(this, R.string.error_next_occurrence_not_populate).show();
            ;
            return false;
        }
        return true;
    }

    public void formatExtendedDate(TextView dateTextView) {
        try {
            dateTextView.setText(new SimpleDateFormat("EEEE dd MMMM yyyy").format((Date) dateTextView.getTag()));
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
        }
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Double amount) {
        Core core = new Core(getApplicationContext());

        View view = findViewById(id);
        int accountId;
        if (view != null && view instanceof TextView) {
            CurrencyUtils currencyUtils = new CurrencyUtils(getApplicationContext());
            if (Constants.TRANSACTION_TYPE_TRANSFER.equals(mTransCode)) {
                Double originalAmount;
                try {
                    Integer toCurrencyId = mAccountList.get(mAccountIdList.indexOf(id == R.id.textViewTotAmount ? mAccountId : mToAccountId)).getCurrencyId();
                    Integer fromCurrencyId = mAccountList.get(mAccountIdList.indexOf(id == R.id.textViewTotAmount ? mToAccountId : mAccountId)).getCurrencyId();
                    // take a original values
                    originalAmount = id == R.id.textViewTotAmount ? (Double) txtTotAmount.getTag() : (Double) txtAmount.getTag();
                    // convert value
                    Double amountExchange = currencyUtils.doCurrencyExchange(toCurrencyId, originalAmount, fromCurrencyId);
                    // take original amount converted
                    originalAmount = id == R.id.textViewTotAmount ? (Double) txtAmount.getTag() : (Double) txtTotAmount.getTag();
                    if (originalAmount == null)
                        originalAmount = 0d;
                    // check if two values is equals, and then convert value
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    if (originalAmount == 0) {
                        if (decimalFormat.format(originalAmount).equals(decimalFormat.format(amountExchange))) {
                            amountExchange = currencyUtils.doCurrencyExchange(toCurrencyId, amount, fromCurrencyId);
                            core.formatAmountTextView(id == R.id.textViewTotAmount ? txtAmount : txtTotAmount, amountExchange, getCurrencyIdFromAccountId(id == R.id.textViewTotAmount ? mAccountId : mToAccountId));
                        }
                    }

                } catch (Exception e) {
                    Log.e(LOGCAT, e.getMessage());
                }
            }
            if (txtTotAmount.equals(view)) {
                if (Constants.TRANSACTION_TYPE_TRANSFER.equals(mTransCode)) {
                    accountId = mToAccountId;
                } else {
                    accountId = mAccountId;
                }
            } else {
                accountId = mAccountId;
            }
            core.formatAmountTextView((TextView) view, amount, getCurrencyIdFromAccountId(accountId));
        }
    }

    public void refreshHeaderAmount() {
        TextView txtHeaderTotAmount = (TextView) findViewById(R.id.textViewHeaderTotalAmount);
        TextView txtHeaderAmount = (TextView) findViewById(R.id.textViewHeaderAmount);

        if (txtHeaderAmount == null || txtHeaderTotAmount == null)
            return;

        if (!Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode)) {
            txtHeaderTotAmount.setText(R.string.total_amount);
            txtHeaderAmount.setText(R.string.amount);
        } else {
            int index = mAccountIdList.indexOf(mAccountId);
            if (index >= 0) {
                txtHeaderAmount.setText(getString(R.string.withdrawal_from, mAccountList.get(index).getAccountName()));
            }
            index = mAccountIdList.indexOf(mToAccountId);
            if (index >= 0) {
                txtHeaderTotAmount.setText(getString(R.string.deposit_to, mAccountList.get(index).getAccountName()));
            }
        }
    }


    public Integer getCurrencyIdFromAccountId(int accountId) {
        try {
            return mAccountList.get(mAccountIdList.indexOf(accountId)).getCurrencyId();
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
