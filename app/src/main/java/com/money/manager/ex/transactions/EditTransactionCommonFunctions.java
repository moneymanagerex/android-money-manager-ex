/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.money.manager.ex.transactions;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.datalayer.TaglinkRepository;
import com.money.manager.ex.payee.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.account.AccountListActivity;
import com.money.manager.ex.common.Calculator;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.common.CommonSplitCategoryLogic;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.CategoryRepository;
import com.money.manager.ex.datalayer.IRepository;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Attachment;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.home.RecentDatabasesProvider;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.BehaviourSettings;
import com.money.manager.ex.settings.PerDatabaseFragment;
import com.money.manager.ex.settings.SettingsActivity;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;
import com.money.manager.ex.utils.TransactionColorUtils;
import com.money.manager.ex.utils.TagLinkUtils;
import com.shamanland.fonticon.FontIconView;
import com.squareup.sqlbrite3.BriteDatabase;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;
import android.content.SharedPreferences;


/**
 * Functions shared between Checking Account activity and Recurring Transactions activity.
 */
public class EditTransactionCommonFunctions {

    private static final String DATEPICKER_TAG = "datepicker";
    // to get database uri
    @Inject
    Lazy<RecentDatabasesProvider> mDatabases;

    @Inject Lazy<MmxDateTimeUtils> dateTimeUtilsLazy;

    // Model
    public ITransactionEntity transactionEntity;
    public String payeeName;
    public String mToAccountName;
    public String categoryName, subCategoryName;
    public ArrayList<ISplitTransaction> mSplitTransactions;
    public ArrayList<ISplitTransaction> mSplitTransactionsDeleted;

    // Controls
    public EditTransactionViewHolder viewHolder;

    private final MmxBaseFragmentActivity activity;
    private boolean mSplitSelected;
    private boolean mDirty = false; // indicate whether the data has been modified by the user.
    private String mSplitCategoryEntityName;
    private final BriteDatabase mDatabase;

    private List<Account> AccountList;
    private final ArrayList<String> mAccountNameList = new ArrayList<>();
    private final ArrayList<Long> mAccountIdList = new ArrayList<>();
    private TransactionTypes previousTransactionType = TransactionTypes.Withdrawal;
    private String[] mStatusValues;    // arrays to manage trans.code and status
    private String mUserDateFormat;


    public EditTransactionCommonFunctions(MmxBaseFragmentActivity parentActivity,
                                          ITransactionEntity transactionEntity, BriteDatabase database) {
        super();

        activity = parentActivity;
        this.transactionEntity = transactionEntity;
        this.mDatabase = database;

        MmexApplication.getApp().iocComponent.inject(this);
    }

    public boolean deleteMarkedSplits(IRepository repository) {
        for (int i = 0; i < mSplitTransactionsDeleted.size(); i++) {
            ISplitTransaction splitToDelete = mSplitTransactionsDeleted.get(i);

            // Ignore unsaved entities.
            if (!splitToDelete.hasId()) continue;

            if (!repository.delete(splitToDelete)) {
                Toast.makeText(getContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                Timber.w("Delete split transaction failed!");
                return false;
            }
            TaglinkRepository taglinkRepository = new TaglinkRepository(getContext());
            taglinkRepository.deleteForType(splitToDelete.getId(), splitToDelete.getTransactionModel());
        }

        return true;
    }

    // issue #1961
    public void displayNotes() {
        if( this.viewHolder.edtNotes == null ) return;
        this.viewHolder.edtNotes.setText(transactionEntity.getNotes());
    }

    public void displayCategoryName() {
        // validation
        if (this.viewHolder.categoryTextView == null) return;

        this.viewHolder.categoryTextView.setText("");

        if (isSplitSelected()) {
            // Split transaction. Show ...
            this.viewHolder.categoryTextView.setText("\u2026");
        } else {
            if (!TextUtils.isEmpty(categoryName)) {
                this.viewHolder.categoryTextView.setText(categoryName);
                if (!TextUtils.isEmpty(subCategoryName)) {
                    this.viewHolder.categoryTextView.setText(Html.fromHtml(
                            this.viewHolder.categoryTextView.getText() + " : <i>" + subCategoryName + "</i>"));
                }
            }
        }
    }

    public void findControls(AppCompatActivity view) {
        this.viewHolder = new EditTransactionViewHolder(view);
    }

    public Long getAccountCurrencyId(long accountId) {
        if (accountId == Constants.NOT_SET) return Constants.NOT_SET;

        AccountRepository repo = new AccountRepository(getContext());
        Long currencyId = repo.loadCurrencyIdFor(accountId);
        if (currencyId == null) {
            new UIHelper(getContext()).showToast(R.string.error_loading_currency);

            currencyId = Constants.NOT_SET;
        }
        return currencyId;
    }

    public String getTransactionType() {
        if (this.transactionEntity.getTransactionType() == null) {
            return null;
        }

        return transactionEntity.getTransactionType().name();
    }

    public FontIconView getDepositButtonIcon() {
        return getActivity().findViewById(R.id.depositButtonIcon);
    }

    public Long getDestinationCurrencyId() {
        Long accountId = this.transactionEntity.getToAccountId();
        // The destination account/currency is hidden by default and may be uninitialized.
        if (!transactionEntity.hasToAccount() && !mAccountIdList.isEmpty()) {
            accountId = mAccountIdList.get(0);
        }

        // Handling some invalid values.
        if (accountId == null || accountId == 0) accountId = Constants.NOT_SET;

        return getAccountCurrencyId(accountId);
    }

    public ArrayList<ISplitTransaction> getDeletedSplitCategories() {
        if(mSplitTransactionsDeleted == null){
            mSplitTransactionsDeleted = new ArrayList<>();
        }
        return mSplitTransactionsDeleted;
    }

    public boolean getDirty() {
        return mDirty;
    }

    public Long getSourceCurrencyId() {
        Long accountId = this.transactionEntity.getAccountId();

        //if (!transactionEntity.has)
        if (accountId == null && !mAccountIdList.isEmpty()) {
            accountId = mAccountIdList.get(0);
        }

        if (accountId == null || accountId == 0) accountId = Constants.NOT_SET;

        return getAccountCurrencyId(accountId);
    }

    public FontIconView getTransferButtonIcon() {
        return getActivity().findViewById(R.id.transferButtonIcon);
    }

    public FontIconView getWithdrawalButtonIcon() {
        return getActivity().findViewById(R.id.withdrawalButtonIcon);
    }

    public boolean hasPayee() {
        return this.transactionEntity.getPayeeId() > 0;
    }

    public boolean hasSplitCategories() {
        return !getSplitTransactions().isEmpty();
    }

    public boolean hasAttachments() {
        if (getAttachments() == null ) return false;
        return  !getAttachments().isEmpty();
    }

    /**
     * Initialize account selectors.
     */
    public void initAccountSelectors() {
        AppSettings settings = new AppSettings(getContext());

        // Account list as the data source to populate the drop-downs.

        AccountService accountService = new AccountService(getContext());
        this.AccountList = accountService.getTransactionAccounts(
                settings.getLookAndFeelSettings().getViewOpenAccounts(),
                settings.getLookAndFeelSettings().getViewFavouriteAccounts());
        if (this.AccountList == null) return;

        for(Account account : this.AccountList) {
            mAccountNameList.add(account.getName());
            mAccountIdList.add(account.getId());
        }

        AccountRepository accountRepository = new AccountRepository(getContext());
        Long accountId = transactionEntity.getAccountId();
        if (accountId != null) {
            addMissingAccountToSelectors(accountRepository, accountId);
        }
        addMissingAccountToSelectors(accountRepository, transactionEntity.getToAccountId());
        // add the default account, if any.
        Long defaultAccount = settings.getGeneralSettings().getDefaultAccountId();
        // Set the current account, if not set already.
        if ((accountId != null && accountId == Constants.NOT_SET) && (defaultAccount != null && defaultAccount != Constants.NOT_SET)) {
            accountId = defaultAccount;
            addMissingAccountToSelectors(accountRepository, accountId);
            // Set the default account as the active account.
            transactionEntity.setAccountId(accountId);
        }

        // Adapter for account selectors.

        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, mAccountNameList);

        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.spinAccount.setAdapter(accountAdapter);
        viewHolder.spinAccountTo.setAdapter(accountAdapter);

        // Selection handler.

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((position < 0) || (position > mAccountIdList.size())) {
                    return;
                }

                setDirty(true);

                boolean isSource = parent == viewHolder.spinAccount;
                boolean isTransfer = transactionEntity.getTransactionType() == TransactionTypes.Transfer;
                Long accountId = mAccountIdList.get(position);

                if (isSource) {
                    long originalCurrencyId = getSourceCurrencyId();

                    transactionEntity.setAccountId(accountId);

                    if (isTransfer) {
                        // calculate the exchange amount if it is 0.
                        if (transactionEntity.getToAmount().isZero()) {
                            Money convertedAmount = calculateToAmount();
                            transactionEntity.setToAmount(convertedAmount);
                            displayToAmount();
                        }
                        // Recalculate the original amount when the currency changes.
                        if (originalCurrencyId != getSourceCurrencyId()) {
                            Money exchangeAmount = calculateAmountFrom();
                            transactionEntity.setAmount(exchangeAmount);
                            displayAmountFrom();
                        }
                    } else {
                        displayAmountFrom();
                    }
                } else {
                    long originalCurrencyId = getDestinationCurrencyId();

                    transactionEntity.setToAccountId(accountId);

                    if (isTransfer) {
                        // calculate the exchange amount if it is 0.
                        if (transactionEntity.getAmount().isZero()) {
                            Money convertedAmount = calculateAmountFrom();
                            transactionEntity.setAmount(convertedAmount);
                            displayAmountFrom();
                        }
                        // Recalculate the original amount when the currency changes.
                        if (originalCurrencyId != getDestinationCurrencyId()) {
                            Money exchangeAmount = calculateToAmount();
                            transactionEntity.setToAmount(exchangeAmount);
                            displayToAmount();
                        }
                    } else {
                        displayToAmount();
                    }
                }

                refreshControlTitles();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        //added by velmuruganc
        viewHolder.swapAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //enable swap only if it is type of transfer
                if(transactionEntity.getTransactionType().equals(TransactionTypes.Transfer)) {
                    //get the account id
                    long fromAccountId = transactionEntity.getAccountId();
                    long toAccountId = transactionEntity.getToAccountId();

                    //get the account index
                    int fromAccountIndex = mAccountIdList.indexOf(fromAccountId);
                    int toAccountIndex = mAccountIdList.indexOf(toAccountId);

                    //Swap From Account to To Account
                    if (fromAccountIndex >= 0) {
                        viewHolder.spinAccountTo.setSelection(fromAccountIndex, true);
                    }
                    viewHolder.spinAccountTo.setOnItemSelectedListener(listener);

                    //Swap To Account to From Account
                    if (toAccountIndex >= 0) {
                        viewHolder.spinAccount.setSelection(toAccountIndex, true);
                    }
                    viewHolder.spinAccount.setOnItemSelectedListener(listener);

                    setDirty(true);
                }
            }
        });

        // Account

        int accountIndex = mAccountIdList.indexOf(accountId);
        if (accountIndex >= 0) {
            viewHolder.spinAccount.setSelection(accountIndex, true);
        }
        viewHolder.spinAccount.setOnItemSelectedListener(listener);

        // To Account

        if (transactionEntity.hasToAccount() && mAccountIdList.contains(transactionEntity.getToAccountId())) {
            viewHolder.spinAccountTo.setSelection(mAccountIdList.indexOf(transactionEntity.getToAccountId()), true);
        }
        viewHolder.spinAccountTo.setOnItemSelectedListener(listener);
    }

    public void initAmountSelectors() {
//        View.OnClickListener onClickAmount = new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // Get currency id from the account for which the amount has been modified.
//                Long currencyId;
//                Money amount;
//
//                if (v.equals(viewHolder.txtAmountTo)) {
//                    // clicked Amount To.
//                    currencyId = getDestinationCurrencyId();
//                    amount = transactionEntity.getToAmount();
//                } else {
//                    // clicked Amount.
//                    currencyId = getSourceCurrencyId();
//                    amount = transactionEntity.getAmount();
//                }
//
//                AmountInputDialog dialog = AmountInputDialog.getInstance(v.getId(), amount, currencyId);
//                dialog.show(activity.getSupportFragmentManager(), dialog.getClass().getSimpleName());
//
//                // The result is received in onFinishedInputAmountDialog.
//            }
//        };

        // amount
        displayAmountFrom();
        viewHolder.txtAmount.setOnClickListener(view -> {
            long currencyId = getSourceCurrencyId();
            Money amount = transactionEntity.getAmount();

//                Intent intent = IntentFactory.getNumericInputIntent(getContext(), amount, currencyId);
//                getActivity().startActivityForResult(intent, REQUEST_AMOUNT);
            Calculator.forActivity(getActivity())
                    .currency(currencyId)
                    .amount(amount)
                    .show(RequestCodes.AMOUNT);
        });

        // amount to
        displayToAmount();
        viewHolder.txtAmountTo.setOnClickListener(view -> {
            long currencyId = getDestinationCurrencyId();
            Money amount = transactionEntity.getToAmount();

//                Intent intent = IntentFactory.getNumericInputIntent(getContext(), amount, currencyId);
//                getActivity().startActivityForResult(intent, REQUEST_AMOUNT_TO);
            Calculator.forActivity(getActivity())
                    .amount(amount).currency(currencyId)
                    .show(RequestCodes.AMOUNT_TO);
        });
    }

    /**
     * Initialize Category selector.
     * @param datasetName name of the dataset (TableBudgetSplitTransactions.class.getSimpleName())
     */
    public void initCategoryControls(final String datasetName) {
        // keep the dataset name for later.
        this.mSplitCategoryEntityName = datasetName;

        this.viewHolder.categoryTextView.setOnClickListener(v -> {
            if (!isSplitSelected()) {
                // select first category.
                Intent intent = new Intent(getActivity(), CategoryListActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                getActivity().startActivityForResult(intent, RequestCodes.CATEGORY);
            } else {
                // select split categories.
                showSplitCategoriesForm(mSplitCategoryEntityName);
            }

            // results are handled in onActivityResult.
        });
    }

    /**
     * Due Date picker
     */
    public void initDateSelector() {
        Date date = this.transactionEntity.getDate();
        if (date == null) {
            date = new MmxDate().toDate();
            transactionEntity.setDate(date);
        }
        showDate(date);

        viewHolder.dateTextView.setOnClickListener(v -> {
            MmxDate dateTime = new MmxDate(transactionEntity.getDate());

            DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
                Date selectedDate = dateTimeUtilsLazy.get().from(year, month, dayOfMonth);
                setDate(selectedDate);
            };

            DatePickerDialog datePicker = new DatePickerDialog(
                    getContext(),
                    listener,
                    dateTime.getYear(),
                    dateTime.getMonthOfYear(),
                    dateTime.getDayOfMonth()
            );

            // Customize the DatePickerDialog if needed
            datePicker.show();
        });

        viewHolder.previousDayButton.setOnClickListener(view -> {
            Date dateTime = new MmxDate(transactionEntity.getDate()).minusDays(1).toDate();
            setDate(dateTime);
        });

        viewHolder.nextDayButton.setOnClickListener(view -> {
            Date dateTime = new MmxDate(transactionEntity.getDate()).plusDays(1).toDate();
            setDate(dateTime);
        });
    }

    public void initializeToolbar() {
//        activity.showStandardToolbarActions();

        // String title
//        ActionBar actionBar = activity.getSupportActionBar();
//        actionBar.setTitle(title);

        // Title
//        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
//        collapsingToolbarLayout.setTitle(getString(R.string.budget));

        // Back arrow / cancel.
        getActivity().setDisplayHomeAsUpEnabled(true);

        // todo: add Save button

    }

    public void initNotesControls() {
        if (!(TextUtils.isEmpty(transactionEntity.getNotes()))) {
            viewHolder.edtNotes.setText(transactionEntity.getNotes());
        }

        viewHolder.edtNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setDirty(true);
                transactionEntity.setNotes(editable.toString());
            }
        });
    }

    public void initTagsControls() {
        (new TagLinkUtils(getContext())).initTagControls(viewHolder.tagsListTextView,
                transactionEntity.getTagLinks(),
                transactionEntity.getId(),
                transactionEntity.getTransactionModel(),
                tagLinks -> {
                    transactionEntity.setTagLinks(tagLinks);
                } );

    }

    public void initPayeeControls() {
        this.viewHolder.txtSelectPayee.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PayeeActivity.class);
            intent.setAction(Intent.ACTION_PICK);
            getActivity().startActivityForResult(intent, RequestCodes.PAYEE);

            // the result is handled in onActivityResult
        });

        viewHolder.removePayeeButton.setOnClickListener(v -> {
            setDirty(true);

            transactionEntity.setPayeeId(Constants.NOT_SET);
            payeeName = "";

            showPayeeName();
        });
    }

    /**
     * Initialize Split Categories button & controls.
     */
    public void initSplitCategories() {
        viewHolder.splitButton.setOnClickListener(v -> {
            boolean splitting = !isSplitSelected();

            if (splitting) {
                showSplitCategoriesForm(mSplitCategoryEntityName);
            } else {
                // User wants to remove split.
                int splitCount = getSplitTransactions().size();
                switch (splitCount) {
                    case 0:
                        // just remove split
                        setSplit(false);
                        break;
                    case 1:
                        convertOneSplitIntoRegularTransaction();
                        break;
                    default:
                        showSplitResetNotice();
                        break;
                }
            }
        });

        refreshSplitControls();
    }

    public void initStatusSelector() {
        String[] statusItems = activity.getResources().getStringArray(R.array.status_items);
        mStatusValues = activity.getResources().getStringArray(R.array.status_values);

        // create adapter for spinnerStatus
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, statusItems);
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.spinStatus.setAdapter(adapterStatus);

        // select current value
        if (!(TextUtils.isEmpty(transactionEntity.getStatus()))) {
            if (Arrays.asList(mStatusValues).contains(transactionEntity.getStatus())) {
                viewHolder.spinStatus.setSelection(Arrays.asList(mStatusValues).indexOf(transactionEntity.getStatus()), true);
            }
        } else {
            transactionEntity.setStatus(mStatusValues[0]);
        }
        viewHolder.spinStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((position >= 0) && (position <= mStatusValues.length)) {
                    String selectedStatus = mStatusValues[position];
                    // If Status has been changed manually, mark data as dirty.
                    if (!selectedStatus.equalsIgnoreCase(transactionEntity.getStatus())) {
                        setDirty(true);
                    }
                    transactionEntity.setStatus(selectedStatus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void initTransactionNumberControls() {
        // Transaction number

        if (!TextUtils.isEmpty(transactionEntity.getTransactionNumber())) {
            viewHolder.edtTransNumber.setText(transactionEntity.getTransactionNumber());
        }

        viewHolder.edtTransNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setDirty(true);

                transactionEntity.setTransactionNumber(editable.toString());
            }
        });

        viewHolder.btnTransNumber.setOnClickListener(v -> {
            AccountTransactionRepository repo = new AccountTransactionRepository(getContext());

            String sql = "SELECT MAX(CAST(" + ITransactionEntity.TRANSACTIONNUMBER + " AS INTEGER)) FROM " +
                repo.getSource() + " WHERE " +
                ITransactionEntity.ACCOUNTID + "=?";

            String accountId = transactionEntity.getAccountId().toString();
            Cursor cursor = mDatabase.query(sql, accountId);
            if (cursor == null) return;

            if (cursor.moveToFirst()) {
                String transNumber = cursor.getString(0);
                if (TextUtils.isEmpty(transNumber)) {
                    transNumber = "0";
                }
                if ((!TextUtils.isEmpty(transNumber)) && TextUtils.isDigitsOnly(transNumber)) {
                    try {
                        // Use Money type to support very large numbers.
                        Money transactionNumber = MoneyFactory.fromString(transNumber);
                        viewHolder.edtTransNumber.setText(transactionNumber.add(MoneyFactory.fromString("1"))
                            .toString());
                    } catch (Exception e) {
                        Timber.e(e, "increasing transaction number");
                    }
                }
            }
            cursor.close();
        });

        if (!transactionEntity.hasId() && transactionEntity.getTransactionNumber().isEmpty() && (new BehaviourSettings(getContext()).getAutoTransactionNumber())) {
            viewHolder.btnTransNumber.callOnClick();
        }
    }

    public void initAttachmentControls() {
        if (!hasAttachments()) return;

        List<String> attachmentList = new ArrayList<>();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String baseUriString = preferences.getString("attachment_folder_uri", null);

        if (baseUriString == null) {
            // Automatically redirect the user to the settings page
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            intent.putExtra(SettingsActivity.EXTRA_FRAGMENT, PerDatabaseFragment.class.getSimpleName());
            getActivity().startActivity(intent);
            return; // Exit early if base URI is not set
        }

        // Parse baseUri from stored preferences
        Uri baseUri = Uri.parse(baseUriString);
        DocumentFile baseFolder = DocumentFile.fromTreeUri(getContext(), baseUri);

        if (baseFolder == null || !baseFolder.exists()) {
            Timber.d("Base folder not found or invalid");
            return; // Exit if the base folder is invalid or doesn't exist
        }

        // Notify observers of changes if any new attachment is added
        ContentResolver contentResolver = getContext().getContentResolver();
        contentResolver.notifyChange(baseUri, null);

        // Access the 'Transaction' subfolder within the base folder
        DocumentFile transactionFolder = baseFolder.findFile("Transaction");
        if (transactionFolder == null || !transactionFolder.exists()) {
            Timber.d("Transaction folder not found");
            return; // Exit if the transaction folder is missing
        }

        // Loop through the list of attachments and check for their existence
        for (Attachment att : getAttachments()) {
            DocumentFile attFile = transactionFolder.findFile(att.getFilename());

            if (attFile != null && attFile.exists()) {
                Uri fileUri = attFile.getUri();

                // Check if we have the necessary read permission for the file
                if (getContext().checkUriPermission(fileUri, Binder.getCallingPid(), Binder.getCallingUid(),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
                    // Add the full URI to the list
                    attachmentList.add(fileUri.toString());
                    Timber.d("File found and added to list: %s", fileUri.toString());
                } else {
                    // Handle missing permission (log, notify, or request permission)
                    Timber.d("No read permission for file: %s", fileUri.toString());
                }
            } else {
                // Handle the case where the file is not found
                Timber.d("File not found: %s", att.getFilename());
            }
        }

        // Update UI: show/hide attachments section based on the list size
        if (attachmentList.isEmpty()) {
            viewHolder.textViewAttachments.setVisibility(View.GONE);
        } else {
            viewHolder.textViewAttachments.setVisibility(View.VISIBLE);
            viewHolder.recyclerAttachments.setAdapter(new AttachmentAdapter(attachmentList));
            viewHolder.recyclerAttachments.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        }
    }

    public void initTransactionTypeSelector() {
        // Handle click events.
        View.OnClickListener onClickListener = v -> {
            setDirty(true);

            // find which transaction type this is.
            TransactionTypes type = (TransactionTypes) v.getTag();
            changeTransactionTypeTo(type);
        };

        if (viewHolder.withdrawalButton != null) {
            viewHolder.withdrawalButton.setTag(TransactionTypes.Withdrawal);

            viewHolder.withdrawalButton.setOnClickListener(onClickListener);
        }
        if (viewHolder.depositButton != null) {
            viewHolder.depositButton.setTag(TransactionTypes.Deposit);

            viewHolder.depositButton.setOnClickListener(onClickListener);
        }
        if (viewHolder.transferButton != null) {
            viewHolder.transferButton.setTag(TransactionTypes.Transfer);

            viewHolder.transferButton.setOnClickListener(onClickListener);
        }

        // Check if the transaction type has been set (for example, when editing an existing transaction).
        TransactionTypes current = transactionEntity.getTransactionType() == null
                ? TransactionTypes.Withdrawal
                : transactionEntity.getTransactionType();
        changeTransactionTypeTo(current);
    }

    public void initColorControls() {
        TransactionColorUtils tsc = new TransactionColorUtils( getContext() );
        tsc.initColorControls( viewHolder.colorTextView,
                transactionEntity.getColor(),
                color -> {
                    transactionEntity.setColor(color);
                    setDirty(true);
                });
    }


    /**
     * Indicate whether the Split Categories is selected/checked.
     * @return boolean
     */
    public boolean isSplitSelected() {
        return mSplitSelected;
    }

    /**
     * Loads info for Category and Subcategory
     * @return A boolean indicating whether the operation was successful.
     */
    public boolean loadCategoryName() {
        this.categoryName = ""; // set default #2041. There is case with empty category?
        if(!this.transactionEntity.hasCategory()) return false;

        CategoryRepository categoryRepository = new CategoryRepository(getContext());
        Category category = categoryRepository.load(this.transactionEntity.getCategoryId());
        if (category != null) {
            this.categoryName = category.getName();
            Timber.d("Determine Recursive Category Name.\n  categoryName: " + this.categoryName);
            // Done handled nested category
            int limit = 0;
            while ( limit < 15 && category != null && category.getParentId() > 0)
            {
                limit++;
                category = categoryRepository.load(category.getParentId());
                if (category != null) {
                    Timber.d("  [%d] Recursive call for id = %d ", limit, category.getId());
                    this.categoryName = category.getName() + ":" + this.categoryName;
                    Timber.d("     new name = [%s]", this.categoryName);
                }
            }
        }

        return true;
    }

    public boolean onActionCancelClick() {
        if (getDirty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(android.R.string.cancel)
                    .setMessage(R.string.transaction_cancel_confirm)
                    .setPositiveButton(R.string.discard, (dialog, which) -> cancelActivity())
                    .setNegativeButton(R.string.keep_editing, (dialog, which) -> dialog.dismiss())
                    .setCancelable(false)
                    .show();
        } else {
            // Just close activity
            cancelActivity();
        }
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((resultCode != AppCompatActivity.RESULT_OK) || (data == null)) return;

        setDirty(true);

        switch (requestCode) {
            case RequestCodes.PAYEE:
                this.transactionEntity.setPayeeId(data.getLongExtra(PayeeActivity.INTENT_RESULT_PAYEEID, Constants.NOT_SET));
                payeeName = data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME);
                // select last category used from payee. Only if category has not been entered earlier.
                if (!isSplitSelected() && !this.transactionEntity.hasCategory() ) {
                    if (setCategoryFromPayee(this.transactionEntity.getPayeeId())) {
                        displayCategoryName(); // refresh UI
                    }
                }
                // refresh UI
                showPayeeName();
                break;

            case RequestCodes.ACCOUNT:
                transactionEntity.setToAccountId(data.getLongExtra(AccountListActivity.INTENT_RESULT_ACCOUNTID, Constants.NOT_SET));
                mToAccountName = data.getStringExtra(AccountListActivity.INTENT_RESULT_ACCOUNTNAME);
                break;

            case RequestCodes.AMOUNT:
                onFinishedInputAmountDialog(R.id.textViewAmount, Calculator.getAmountFromResult(data));
                break;

            case RequestCodes.AMOUNT_TO:
                onFinishedInputAmountDialog(R.id.textViewToAmount, Calculator.getAmountFromResult(data));
                break;

            case RequestCodes.CATEGORY:
                long selectId = data.getLongExtra(CategoryListActivity.INTENT_RESULT_CATEGID, Constants.NOT_SET);
                if ( selectId != -1) {
                    // data is selected
                    this.transactionEntity.setCategoryId(selectId);
                    categoryName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_CATEGNAME);
                    // refresh UI category
                    displayCategoryName();
                }
                break;

            case RequestCodes.SPLIT_TX:
                mSplitTransactions = Parcels.unwrap(data.getParcelableExtra(SplitCategoriesActivity.INTENT_RESULT_SPLIT_TRANSACTION));

                // deleted items
                Parcelable parcelDeletedSplits = data.getParcelableExtra(SplitCategoriesActivity.INTENT_RESULT_SPLIT_TRANSACTION_DELETED);
                if (parcelDeletedSplits != null) {
                    mSplitTransactionsDeleted = Parcels.unwrap(parcelDeletedSplits);
                }

                // Splits and deleted splits must be restored before any action takes place.
                onSplitConfirmed(getSplitTransactions());

                break;
        }
    }

    public void onFinishedInputAmountDialog(int id, Money amount) {
        View view = getActivity().findViewById(id);
        if (view == null || !(view instanceof TextView)) return;

        setDirty(true);

        boolean isTransfer = transactionEntity.getTransactionType().equals(TransactionTypes.Transfer);
        boolean isAmountFrom = id == R.id.textViewAmount;

        // Set and display the selected amount.
        if (isAmountFrom) {
            this.transactionEntity.setAmount(amount);
            displayAmountFrom();
        } else {
            this.transactionEntity.setToAmount(amount);
            displayToAmount();
        }

        // Handle currency exchange on Transfers.
        if (isTransfer) {
            Long fromCurrencyId = getSourceCurrencyId();
            Long toCurrencyId = getDestinationCurrencyId();
            if (fromCurrencyId.equals(toCurrencyId)) {
                // Same currency. Update both values if the transfer is in the same currency.
                this.transactionEntity.setAmount(amount);
                this.transactionEntity.setToAmount(amount);

                displayAmountFrom();
                displayToAmount();
                // Exit here.
                return;
            }

            // Different currency. Recalculate the other amount only if it has not been set.
            boolean shouldConvert = isAmountFrom
                    ? transactionEntity.getToAmount().isZero()
                    : transactionEntity.getAmount().isZero();
            if (shouldConvert){
                // Convert the value and write the amount into the other input box.
                Money convertedAmount;
                if (isAmountFrom) {
                    convertedAmount = calculateToAmount();
                    transactionEntity.setToAmount(convertedAmount);
                    displayToAmount();
                } else {
                    convertedAmount = calculateAmountFrom();
                    transactionEntity.setAmount(convertedAmount);
                    displayAmountFrom();
                }
            }
        }
    }

    /**
     * Handle the controls after the split is checked.
     */
    public void refreshSplitControls() {
        // display category field
        displayCategoryName();

        // enable/disable Amount field.
        viewHolder.txtAmount.setEnabled(!mSplitSelected);
        viewHolder.txtAmountTo.setEnabled(!mSplitSelected);

        updateSplitButton();
    }

    /**
     * Reflect the transaction type change. Show and hide controls appropriately.
     */
    public void onTransactionTypeChanged(TransactionTypes transactionType) {
        transactionEntity.setTransactionType(transactionType);

        boolean isTransfer = transactionType.equals(TransactionTypes.Transfer);

        viewHolder.accountFromLabel.setText(isTransfer ? R.string.from_account : R.string.account);
        viewHolder.tableRowAccountTo.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
        viewHolder.tableRowPayee.setVisibility(!isTransfer ? View.VISIBLE : View.GONE);
        viewHolder.tableRowAmountTo.setVisibility(isTransfer ? View.VISIBLE : View.GONE);

        refreshControlTitles();

        if (isTransfer) {
            onTransferSelected();
            viewHolder.splitButton.setEnabled(false);
        } else {
            // Change sign for the split records. Transfers should delete split records.
            CommonSplitCategoryLogic.changeSign(getSplitTransactions());
            viewHolder.splitButton.setEnabled(true);
        }
    }

    /**
     * Update input control titles to reflect the transaction type.
     */
    public void refreshControlTitles() {
        if (viewHolder.amountHeaderTextView == null || viewHolder.amountToHeaderTextView == null) return;

        boolean isTransfer = transactionEntity.getTransactionType().equals(TransactionTypes.Transfer);

        if (!isTransfer) {
            viewHolder.amountHeaderTextView.setText(R.string.amount);
        } else {
            // Transfer. Adjust the headers on amount text boxes.
            int index = mAccountIdList.indexOf(transactionEntity.getAccountId());
            if (index >= 0) {
                // the title depends on whether we are showing the destination amount.
                if (areCurrenciesSame()) {
                    viewHolder.amountHeaderTextView.setText(getContext().getString(R.string.transfer_amount));
                } else {
                    viewHolder.amountHeaderTextView.setText(getContext().getString(R.string.withdrawal_from,
                            this.AccountList.get(index).getName()));
                }
            }

            index = mAccountIdList.indexOf(transactionEntity.getToAccountId());
            if (index >= 0) {
                viewHolder.amountToHeaderTextView.setText(getContext().getString(R.string.deposit_to,
                        this.AccountList.get(index).getName()));
            }
        }
    }

    /**
     * update UI interface with PayeeName
     */
    public void showPayeeName() {
        // write into text button payee name
        if (this.viewHolder.txtSelectPayee != null) {
            String text = !TextUtils.isEmpty(payeeName)
                    ? payeeName : "";

            this.viewHolder.txtSelectPayee.setText(text);
        }
    }

    /**
     * Reset the effects of transfer when switching to Withdrawal/Deposit.
     */
    public void resetTransfer() {
        // reset destination account and amount
        transactionEntity.setToAccountId(Constants.NOT_SET);
        transactionEntity.setToAmount(MoneyFactory.fromDouble(0));
    }

    public void setSplit(boolean checked) {
        mSplitSelected = checked;

        refreshSplitControls();
    }

    /**
     * query info payee
     * @param payeeId id payee
     * @return true if the data selected
     */
    public boolean loadPayeeName(long payeeId) {
        PayeeRepository repo = new PayeeRepository(getContext());
        Payee payee = repo.load(payeeId);
        if (payee != null) {
            this.payeeName = payee.getName();
        } else {
            this.payeeName = "";
        }

        return true;
    }

    /**
     * setCategoryFromPayee set last category used from payee
     * @param payeeId Identify of payee
     * @return true if category set
     */
    public boolean setCategoryFromPayee(long payeeId) {
        if (payeeId == Constants.NOT_SET) return false;

        PayeeRepository repo = new PayeeRepository(getContext());
        Payee payee = repo.load(payeeId);
        if (payee == null) return false;
        if (!payee.hasCategory()) return false;

        // otherwise

        this.transactionEntity.setCategoryId(payee.getCategoryId());

        loadCategoryName();

        return true;
    }

    public void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    /**
     * Select, or change, the type of transaction (withdrawal, deposit, transfer).
     * Entry polong and the handler for the type selector input control.
     * @param transactionType The type to set the transaction to.
     */
    public void changeTransactionTypeTo(TransactionTypes transactionType) {
        this.previousTransactionType = this.transactionEntity.getTransactionType();
        this.transactionEntity.setTransactionType(transactionType);

        // Clear all buttons.

        Core core = new Core(activity);
        int backgroundInactive = core.getColourFromAttribute(R.attr.button_background_inactive);

        viewHolder.withdrawalButton.setBackgroundColor(backgroundInactive);
        getWithdrawalButtonIcon().setTextColor(ContextCompat.getColor(activity, R.color.material_red_700));
        viewHolder.depositButton.setBackgroundColor(backgroundInactive);
        getDepositButtonIcon().setTextColor(ContextCompat.getColor(activity, R.color.material_green_700));
        viewHolder.transferButton.setBackgroundColor(backgroundInactive);
        getTransferButtonIcon().setTextColor(ContextCompat.getColor(activity, R.color.material_grey_700));

        // Style the selected button.

        UIHelper uiHelper = new UIHelper(getContext());
        int backgroundSelected = ContextCompat.getColor(getContext(), R.color.md_accent);
        int foregroundSelected = uiHelper.getToolbarItemColor();

        switch (transactionType) {
            case Deposit:
                viewHolder.depositButton.setBackgroundColor(backgroundSelected);
                getDepositButtonIcon().setTextColor(foregroundSelected);
                break;
            case Withdrawal:
                viewHolder.withdrawalButton.setBackgroundColor(backgroundSelected);
                getWithdrawalButtonIcon().setTextColor(foregroundSelected);
                break;
            case Transfer:
                viewHolder.transferButton.setBackgroundColor(backgroundSelected);
                getTransferButtonIcon().setTextColor(foregroundSelected);
                break;
        }

        // Handle the change.

        onTransactionTypeChanged(transactionType);
    }

    public boolean validateData() {
        boolean isTransfer = transactionEntity.getTransactionType().equals(TransactionTypes.Transfer);
        Core core = new Core(getContext());

        if (isTransfer) {
            if (transactionEntity.getToAccountId() == Constants.NOT_SET) {
                core.alert(R.string.error_toaccount_not_selected);
                return false;
            }
            if (transactionEntity.getToAccountId().equals(transactionEntity.getAccountId())) {
                core.alert(R.string.error_transfer_to_same_account);
                return false;
            }

            // Amount To is required and has to be positive.
            if (this.transactionEntity.getToAmount().toDouble() < 0) {
                core.alert(R.string.error_amount_must_be_positive);
                return false;
            }
        }

        // Amount is required and must be positive. Sign is determined by transaction type.
        if (transactionEntity.getAmount().toDouble() < 0) {
            core.alert(R.string.error_amount_must_be_positive);
            return false;
        }

        // Payee is required if tx is not transfer
        if (!transactionEntity.hasPayee() && !isTransfer)
        {
            core.alert(R.string.error_payee_not_selected);
            return false;
        }

        // Category is required if tx is not a split or transfer.
        boolean hasCategory = transactionEntity.hasCategory();
        if (!hasCategory && (!isSplitSelected()) && !isTransfer) {
            core.alert(R.string.error_category_not_selected);
            return false;
        }

        // Split records must exist if split is checked.
        if (isSplitSelected() && getSplitTransactions().isEmpty()) {
            core.alert(R.string.error_split_transaction_empty);
            return false;
        }
        // Splits sum must be positive.
        if (!CommonSplitCategoryLogic.validateSumSign(getSplitTransactions())){
            core.alert(R.string.split_amount_negative);
            return false;
        }

        return true;
    }

    /**
     * Remove splits when switching to Transfer.
     */
    public void confirmDeletingCategories() {
        removeAllSplitCategories();
        setSplit(false);
        transactionEntity.setTransactionType(TransactionTypes.Transfer);
        onTransactionTypeChanged(TransactionTypes.Transfer);
    }

    /**
     * When cancelling changing the transaction type to Transfer, revert back to the
     * previous transaction type.
     */
    public void cancelChangingTransactionToTransfer() {
        // Select the previous transaction type.
        changeTransactionTypeTo(previousTransactionType);
    }

    /**
     * After the user accepts, remove any split categories.
     */
    public void removeAllSplitCategories() {
        List<ISplitTransaction> splitTransactions = getSplitTransactions();

        for(int i = 0; i < splitTransactions.size(); i++) {
            ISplitTransaction split = splitTransactions.get(i);
            // How do we get this?
            //if (split == null) continue;

            long id = split.getId();
            ArrayList<ISplitTransaction> deletedSplits = getDeletedSplitCategories();

            if(id == -1) {
                // Remove any newly created splits.
                splitTransactions.remove(i);
                i--;
            } else {
                // Delete any splits already in the database. Avoid adding duplicate records.
                if(!deletedSplits.contains(split)) {
                    deletedSplits.add(split);
                }
            }
        }
    }

    /**
     * Check if there is only one Split Category and transforms the transaction to a non-split
     * transaction, removing the split category record.
     * @return True if there is only one split. Need to update the transaction.
     */
    public boolean convertOneSplitIntoRegularTransaction() {
        if (getSplitTransactions().size() != 1) return false;

        // use the first split category record.
        ISplitTransaction splitTransaction = getSplitTransactions().get(0);

        // reuse the amount & category
        transactionEntity.setAmount(splitTransaction.getAmount());
        displayAmountFrom();

        transactionEntity.setCategoryId(splitTransaction.getCategoryId());
        loadCategoryName();
//        displayCategoryName();

        // issue #2380
        if ( ! splitTransaction.getNotes().isEmpty() ) {
            // issue #1961
            transactionEntity.setNotes(splitTransaction.getNotes());
        }
        displayNotes();

        // reset split indicator & display category
        setSplit(false);

        getDeletedSplitCategories().add(splitTransaction);
        getSplitTransactions().remove(splitTransaction);

        // e deletion in the specific implementation.
        return true;
    }

    /*
        Private
    */

    private void addMissingAccountToSelectors(AccountRepository accountRepository, Long accountId) {
        if (accountId == null || accountId <= 0) return;

        // #316. In case the account from recurring transaction is not in the visible list,
        // load it separately.
        if (!mAccountIdList.contains(accountId)) {
            Account savedAccount = accountRepository.load(accountId);

            if (savedAccount != null) {
                this.AccountList.add(savedAccount);
                mAccountNameList.add(savedAccount.getName());
                mAccountIdList.add(savedAccount.getId());
            }
        }
    }

    private boolean areCurrenciesSame() {
        if (transactionEntity.getAccountId() == null) return false;
        if (transactionEntity.getToAccountId() == null) return false;

        AccountRepository repo = new AccountRepository(getContext());
        Account accountFrom = repo.load(transactionEntity.getAccountId());
        if (accountFrom == null) return false;

        Account accountTo = repo.load(transactionEntity.getToAccountId());
        if (accountTo == null) return false;

        return accountFrom.getCurrencyId().equals(accountTo.getCurrencyId());
    }

    /**
     * Perform currency exchange to get the Amount From.
     */
    private Money calculateAmountFrom() {
        CurrencyService currencyService = new CurrencyService(getContext());

        return currencyService.doCurrencyExchange(getSourceCurrencyId(), transactionEntity.getToAmount(),
                getDestinationCurrencyId());
    }

    private Money calculateToAmount() {
        CurrencyService currencyService = new CurrencyService(getContext());

        return currencyService.doCurrencyExchange(getDestinationCurrencyId(), transactionEntity.getAmount(),
                getSourceCurrencyId());
    }

    private void cancelActivity() {
        getActivity().setResult(AppCompatActivity.RESULT_CANCELED);
        getActivity().finish();
    }

    /**
     * Create a split item using the amount and category from the existing transaction.
     * if there is a Category selected, and we are enabling Splits, use the selected category for
     * the initial split record.
     */
    private ISplitTransaction createSplitFromTransaction() {
        // Add the new split record of the same type as the parent.
        ISplitTransaction entity = SplitItemFactory.create(this.mSplitCategoryEntityName, transactionEntity.getTransactionType());

        entity.setAmount(this.transactionEntity.getAmount());

        if (this.transactionEntity.hasCategory()) {
            entity.setCategoryId(this.transactionEntity.getCategoryId());
        }

        if (this.transactionEntity.getTagLinks() != null ) {
            entity.setTagLinks(this.transactionEntity.getTagLinks());
        }

        return entity;
    }

    private void displayAmountFrom() {
        Money amount = transactionEntity.getAmount() == null
            ? MoneyFactory.fromDouble(0)
            : transactionEntity.getAmount();

        displayAmountFormatted(viewHolder.txtAmount, amount, getSourceCurrencyId());
    }

    private void displayToAmount() {
        // if the currencies are the same, show only one Amount field.
        int amountToVisibility = areCurrenciesSame() ? View.GONE : View.VISIBLE;
        viewHolder.tableRowAmountTo.setVisibility(amountToVisibility);

        Money amount = transactionEntity.getToAmount() == null ? MoneyFactory.fromDouble(0) : transactionEntity.getToAmount();
        //displayToAmount(amount);

        displayAmountFormatted(viewHolder.txtAmountTo, amount, getDestinationCurrencyId());
    }

    private void displayAmountFormatted(TextView view, Money amount, Long currencyId) {
        if (amount == null) return;
        if (currencyId == null || currencyId == Constants.NOT_SET) return;

        CurrencyService currencyService = new CurrencyService(getContext());

        String amountDisplay = currencyService.getCurrencyFormatted(currencyId, amount);

        view.setText(amountDisplay);
        view.setTag(amount.toString());
    }

    private MmxBaseFragmentActivity getActivity() {
        return activity;
    }

    private MmxBaseFragmentActivity getContext() {
        return activity;
    }

    private ArrayList<ISplitTransaction> getSplitTransactions() {
        if (mSplitTransactions == null) {
            mSplitTransactions = new ArrayList<>();
        }
        return mSplitTransactions;
    }

    private ArrayList<Attachment> getAttachments() {
        return this.transactionEntity.getAttachments();
    }

    private String getUserDateFormat() {
        if (TextUtils.isEmpty(mUserDateFormat)) {
            mUserDateFormat = dateTimeUtilsLazy.get().getUserDatePattern(getContext());
        }
        return mUserDateFormat;
    }

    /**
     * Returning from the Split Categories form after OK button was pressed.
     */
    private void onSplitConfirmed(List<ISplitTransaction> splits) {
        if (splits.isEmpty()) {
            // All split categories removed.
            resetCategory();
            setSplit(false);
            return;
        }

        // if there is only one split item, e it immediately.
        if (splits.size() == 1) {
            convertOneSplitIntoRegularTransaction();
            return;
        }

        // Multiple split categories exist at this point.

        resetCategory();

        // indicate that the split is active & refresh display
        setSplit(true);

        // Use the sum of all splits as the Amount.
        Money splitSum = MoneyFactory.fromString("0");
        for (int i = 0; i < splits.size(); i++) {
            splitSum = splitSum.add(splits.get(i).getAmount());
        }
        transactionEntity.setAmount(splitSum);
        displayAmountFrom();
    }

    /**
     * The user is switching to Transfer transaction type.
     */
    private void onTransferSelected() {
        // Check whether to delete split categories, if any.
        if(hasSplitCategories()) {
            // Prompt the user to confirm deleting split categories.
            // Use DialogFragment in order to redraw the binaryDialog when switching device orientation.

            DialogFragment dialog = new YesNoDialog();
            Bundle args = new Bundle();
            args.putString("title", getContext().getString(R.string.warning));
            args.putString("message", getContext().getString(R.string.no_transfer_splits));
            args.putString("purpose", YesNoDialog.PURPOSE_DELETE_SPLITS_WHEN_SWITCHING_TO_TRANSFER);
            dialog.setArguments(args);

            dialog.show(getActivity().getSupportFragmentManager(), "tag");

            // Dialog result is handled in onEvent handlers in the listeners.

            return;
        }

        // un-check split.
        setSplit(false);

        // Set the destination account, if not already.
        if (transactionEntity.getToAccountId() == null || transactionEntity.getToAccountId().equals(Constants.NOT_SET)) {
            if (mAccountIdList.isEmpty()) {
                // notify the user and exit.
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.warning)
                        .setMessage(R.string.no_accounts_available_for_selection)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            // Handle positive button click if needed
                            dialog.dismiss();
                        })
                        .show();
                return;
            } else {
                transactionEntity.setToAccountId(mAccountIdList.get(0));
            }
        }

        // calculate AmountTo only if not set previously.
        if (transactionEntity.getToAmount().isZero()) {
            Money amountTo = calculateToAmount();
            transactionEntity.setToAmount(amountTo);
        }
        displayToAmount();
    }

    private void resetCategory() {
        // Reset the Sub/Category on the transaction.
        transactionEntity.setCategoryId(Constants.NOT_SET);
    }

    private void showDate(Date dateTime) {
        // Constants.LONG_DATE_MEDIUM_DAY_PATTERN
        String format = "EEE, " + getUserDateFormat();
        String display = dateTimeUtilsLazy.get().format(dateTime, format);
        viewHolder.dateTextView.setText(display);
    }

    private void showSplitCategoriesForm(String datasetName) {
        // If there are no splits, use the current values for the initial split record.
        List<ISplitTransaction> splitsToShow = getSplitTransactions();
        if (getSplitTransactions().isEmpty()) {
            ISplitTransaction currentTransaction = createSplitFromTransaction();
            splitsToShow.add(currentTransaction);
        }

        Intent intent = new Intent(getContext(), SplitCategoriesActivity.class);
        intent.putExtra(SplitCategoriesActivity.KEY_DATASET_TYPE, datasetName);
        intent.putExtra(SplitCategoriesActivity.KEY_TRANSACTION_TYPE, transactionEntity.getTransactionType().getCode());
        intent.putExtra(SplitCategoriesActivity.KEY_SPLIT_TRANSACTION, Parcels.wrap(splitsToShow));
        intent.putExtra(SplitCategoriesActivity.KEY_SPLIT_TRANSACTION_DELETED, Parcels.wrap(mSplitTransactionsDeleted));

        Long fromCurrencyId = getSourceCurrencyId();
        intent.putExtra(SplitCategoriesActivity.KEY_CURRENCY_ID, fromCurrencyId);

        getActivity().startActivityForResult(intent, RequestCodes.SPLIT_TX);
    }

    /**
     * If the user wants to reset the Split but there are multiple records, show the notice
     * that the records must be adjusted manually.
     */
    private void showSplitResetNotice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.split_transaction)
                .setMessage(R.string.split_reset_notice)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    // Handle positive button click if needed
                    dialog.dismiss();
                })
                .show();
    }

    private void setDate(Date dateTime) {
        setDirty(true);

        transactionEntity.setDate(dateTime);

        showDate(dateTime);
    }

    private void updateSplitButton() {
        // update Split button
        int buttonColour, buttonBackground;
        if (isSplitSelected()) {
//            buttonColour = R.color.button_foreground_active;
            buttonColour = R.color.md_accent;
            buttonBackground = R.color.md_primary;
            // #188: if there is a Category selected and we are switching to Split Categories.
        } else {
            buttonColour = R.color.button_foreground_inactive;
            buttonBackground = new UIHelper(getContext()).isUsingDarkTheme()
                    ? R.color.button_background_inactive_dark
                    : R.color.button_background_inactive_light;
        }
        viewHolder.splitButton.setTextColor(ContextCompat.getColor(getContext(), buttonColour));
        viewHolder.splitButton.setBackgroundColor(ContextCompat.getColor(getContext(), buttonBackground));
    }

    public void saveTags() {
        // save TagLinks
        TaglinkRepository taglinkRepository = new TaglinkRepository( getContext()) ;
        if (transactionEntity.getTagLinks() != null) {
            taglinkRepository.saveAllFor(transactionEntity.getTransactionModel(), transactionEntity.getId(), transactionEntity.getTagLinks());
        } else {
            taglinkRepository.deleteForType(transactionEntity.getId(), transactionEntity.getTransactionModel() );
        }

    }

}
