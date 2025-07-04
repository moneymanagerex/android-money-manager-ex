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
package com.money.manager.ex.common;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import android.provider.DocumentsContract;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.crashreport.CrashReporter;
import com.money.manager.ex.log.ErrorRaisedEvent;
import com.money.manager.ex.settings.AppSettings;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public abstract class MmxBaseFragmentActivity
    extends AppCompatActivity {
    private ActivityResultLauncher<Intent> openDocumentLauncher;
    private ActivityResultLauncher<Intent> directoryPickerLauncher;

    public CompositeSubscription compositeSubscription;

    private Toolbar mToolbar;
    private boolean mDisplayHomeAsUpEnabled = false;

    private BaseListFragment listFragment;
    private String FRAGMENTTAG = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new CrashReporter(this);

        setTheme();

        AppSettings settings = new AppSettings(this);
        String locale = settings.getGeneralSettings().getApplicationLanguage();
        new Core(this).setAppLocale(locale);

        // Add layout inflater for icon fonts in xml.
//        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
//        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));

        // Initialize database encryption.
//        SQLiteDatabase.loadLibs(this);

        this.compositeSubscription = new CompositeSubscription();

        super.onCreate(savedInstanceState);

        // Initialize the ActivityResultLauncher
        openDocumentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // Permission granted, handle the selected content URI here
                        Uri uri = result.getData().getData();
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                });
        // Initialize the ActivityResultLauncher
        directoryPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri treeUri = result.getData().getData();
                        // Handle the selected directory URI
                        // Perform actions using the selected directory URI
                        getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                });
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mToolbar = findViewById(R.id.toolbar);
        if (mToolbar != null) setSupportActionBar(mToolbar);

        if (setEdgeToEdge(R.id.main_content_container_for_edge_to_edge)) return;
        // TODO implement identify main view
        try {
            View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
            setEdgeToEdge(rootView);
        } catch (Exception e) {

        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // home click can be handled in the manifest by setting up the parent activity.
        if (item.getItemId() == android.R.id.home) {// This is used to handle the <- Home arrow button in the toolbar.
//                NavUtils.navigateUpFromSameTask(this);
//                return true;
            if (mDisplayHomeAsUpEnabled) {
                setResult(Activity.RESULT_CANCELED);
                finish();
                return true;
            }

//            case R.id.menu_cancel:
//                if (isDialogMode()) {
//                    onActionCancelClick();
//                    return true;
//                }
//            case R.id.menu_done:
//                if (isDialogMode()) {
//                    onActionDoneClick();
//                    return true;
//                }
//
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
            // set elevation actionbar 0
//            if (getSupportActionBar() != null) {
//                getSupportActionBar().setElevation(0);
//            }

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (!compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }

        try {
            super.onDestroy();
        } catch (Exception e){
            Timber.e(e.getMessage());
        }
    }

    @Subscribe
    public void onEvent(ErrorRaisedEvent event) {
        // display the error to the user
        new UIHelper(this).showToast(event.message);
    }

    /**
     * Add handlers for the OK/Cancel buttons at the bottom of the screen.
     */
    public void addDefaultButtonHandlers() {
        View container = findViewById(R.id.defaultButtons);

        showStandardToolbarActions(container);
    }

    /**
     * Uses the default toolbar and action buttons.
     */
    public void showStandardToolbarActions() {
        View toolbar = getToolbar();
        if (toolbar != null) {
            showStandardToolbarActions(toolbar);
        } else {
            // use the button container at the bottom
            addDefaultButtonHandlers();
        }
    }

    /**
     * Sets OK & Cancel as the toolbar buttons with handlers (onActionDoneClick & onActionCancelClick).
     * @param toolbar Toolbar element.
     */
    public void showStandardToolbarActions(View toolbar) {
        showStandardToolbarActions(toolbar, R.id.action_cancel, R.id.action_done);
    }

    /**
     * Allows customization of the toolbar buttons
     * @param toolbar       Toolbar element to attach to.
     * @param actionCancel  R.id of the negative (cancel) button
     * @param actionDone    R.id of the positive (action) button
     */
    public void showStandardToolbarActions(View toolbar, int actionCancel, int actionDone) {
        if (toolbar != null) {
            View cancelActionView = toolbar.findViewById(actionCancel);
            if (cancelActionView != null)
                cancelActionView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        onActionCancelClick();
                    }
                });
            View doneActionView = toolbar.findViewById(actionDone);
            if (doneActionView != null)
                doneActionView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onActionDoneClick();
                    }
                });
        }
    }

    /**
     * Override to e clicking the Cancel button in the toolbar
     */
    public boolean onActionCancelClick() {
        return true;
    }

    /**
     * Override to e clicking the Action button in the toolbar
     */
    public boolean onActionDoneClick() {
        return true;
    }

    public void setDisplayHomeAsUpEnabled(boolean mDisplayHomeAsUpEnabled) {
        this.mDisplayHomeAsUpEnabled = mDisplayHomeAsUpEnabled;
        getSupportActionBar().setDisplayHomeAsUpEnabled(mDisplayHomeAsUpEnabled);
    }

    // protected

    protected Toolbar getToolbar() {
        return mToolbar;
    }

    protected void setTheme() {
        try {
            UIHelper uiHelper = new UIHelper(this);
            this.setTheme(uiHelper.getThemeId());
        } catch (Exception e) {
            Timber.e(e, "setting theme");
        }
    }

    public void onPermissionGranted(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setData(uri);
        intent.setType("image/*") ;
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        openDocumentLauncher.launch(intent);
    }

    public void openDirectoryPicker(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
       // intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        directoryPickerLauncher.launch(intent);
    }

    protected void inizializeCommon(BaseListFragment mListFragment, String mFragmentTAG) {
        setContentView(R.layout.base_toolbar_activity);
        listFragment = mListFragment;
        FRAGMENTTAG = mFragmentTAG;

        // enable home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // process intent
        FragmentManager fm = getSupportFragmentManager();
        // attach fragment activity
        if (fm.findFragmentById(R.id.content) == null) {
            // todo: use .replace
            fm.beginTransaction()
                    .add(R.id.content, listFragment, FRAGMENTTAG)
                    .commit();
        }

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (FRAGMENTTAG == null) return super.onKeyUp(keyCode, event);

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // set result
            BaseListFragment fragment = (BaseListFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENTTAG);
            if (fragment != null) {
                fragment.getActivity().setResult(RESULT_CANCELED);
                fragment.getActivity().finish();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public void setEdgeToEdge(@NonNull View mainLayout) {
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply inset as padding
            v.setPadding(
                    insets.left,
                    insets.top,
                    insets.right,
                    insets.bottom
            );

            // notify consumed inset
            // use WindowInsetsCompat.CONSUMED if you don't want to handle the inset
            // return windowInsets.inset(insets);
            return WindowInsetsCompat.CONSUMED;
        });
    }

        public boolean setEdgeToEdge(@IdRes int mainView) {
        // handle edge-to-edge
        View mainLayout = findViewById(mainView);
        if (mainLayout == null) return false;
        setEdgeToEdge(mainLayout);
        return true;
    }

}
