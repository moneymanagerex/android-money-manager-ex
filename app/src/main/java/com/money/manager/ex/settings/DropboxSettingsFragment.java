/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
//import com.dropbox.chooser.android.DbxChooser;
import com.money.manager.ex.HelpActivity;
import com.money.manager.ex.dropbox.DropboxScheduler;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.dropbox.DropboxManager;
import com.money.manager.ex.dropbox.DropboxBrowserActivity;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.dropbox.DropboxServiceIntent;
import com.money.manager.ex.dropbox.events.DbFileDownloadedEvent;
import com.money.manager.ex.fragment.TipsDialogFragment;
import com.money.manager.ex.settings.events.AppRestartRequiredEvent;
import com.money.manager.ex.utils.MmexFileUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

/**
 * Dropbox settings.
 */
public class DropboxSettingsFragment
    extends PreferenceFragment {

    private static final int REQUEST_DBX_CHOOSER = 10;
    private static final int REQUEST_DROPBOX_FILE = 20;

    private DropboxHelper mDropboxHelper = null;
    private boolean mDropboxLoginBegin = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_dropbox);
        PreferenceManager.getDefaultSharedPreferences(getActivity());

        // dropbox preference screen
        mDropboxHelper = DropboxHelper.getInstance(getActivity());

        initializeControls();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_DBX_CHOOSER:
//                handleFileSelectionAll(resultCode, data);
                break;
            case REQUEST_DROPBOX_FILE:
                handleFileSelection(resultCode, data);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // register as event bus listener
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // complete process authentication
        if (mDropboxLoginBegin) {
            mDropboxHelper.completeAuthenticationDropbox();
            mDropboxHelper.sendBroadcastStartServiceScheduled(DropboxScheduler.ACTION_START);
            mDropboxLoginBegin = false;
        }

        // dropbox link and unlink
        if (findPreference(getString(PreferenceConstants.PREF_DROPBOX_LINK)) != null) {
            findPreference(getString(PreferenceConstants.PREF_DROPBOX_LINK)).setSelectable(!mDropboxHelper.isLinked());
            findPreference(getString(PreferenceConstants.PREF_DROPBOX_LINK)).setEnabled(!mDropboxHelper.isLinked());
        }
        if (findPreference(getString(PreferenceConstants.PREF_DROPBOX_UNLINK)) != null) {
            findPreference(getString(PreferenceConstants.PREF_DROPBOX_UNLINK)).setSelectable(mDropboxHelper.isLinked());
            findPreference(getString(PreferenceConstants.PREF_DROPBOX_UNLINK)).setEnabled(mDropboxHelper.isLinked());
        }
        // todo Hide linked file name.

    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    // event handling

    /**
     * Called when file is downloaded from Dropbox.
     */
    @Subscribe
    public void onEvent(DbFileDownloadedEvent event) {
        // set main activity to reload.
//        MainActivity.setRestartActivity(true);
        EventBus.getDefault().post(new AppRestartRequiredEvent());

        // open the new database.
        DropboxManager dropbox = new DropboxManager(getActivity(), mDropboxHelper);
        dropbox.openDownloadedDatabase();
    }

    // private

    private void initializeControls() {
        final PreferenceScreen pDropbox = (PreferenceScreen) findPreference(getString(PreferenceConstants.PREF_DROPBOX_HOWITWORKS));
        if (pDropbox != null) {
            pDropbox.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showWebTipsDialog(getString(PreferenceConstants.PREF_DROPBOX_HOWITWORKS),
                            getString(R.string.dropbox_how_it_works), R.raw.help_dropbox, false);
                    return false;
                }
            });
        }

        // Login to Dropbox.

        final Preference pDropboxLink = findPreference(getString(PreferenceConstants.PREF_DROPBOX_LINK));
        pDropboxLink.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                mDropboxHelper.logIn();
                mDropboxLoginBegin = true;
                return false;
            }
        });

        // Logout from dropbox

        final Preference pDropboxUnlink = findPreference(getString(PreferenceConstants.PREF_DROPBOX_UNLINK));
        pDropboxUnlink.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                mDropboxHelper.logOut();
                mDropboxHelper.sendBroadcastStartServiceScheduled(DropboxScheduler.ACTION_CANCEL);

                // refresh ui
                onResume();
                return false;
            }
        });

        //wiki
        Preference pWiki = findPreference(getString(PreferenceConstants.PREF_DROPBOX_WIKI));
        if (pWiki != null) {
            pWiki.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://code.google.com/p/android-money-manager-ex/wiki/DropboxSync")));
                    Intent intent = new Intent(getActivity(), HelpActivity.class);
                    intent.setData(Uri.parse("android.resource://com.money.manager.ex/" + R.raw.help_dropbox));
                    //intent.setData(Uri.parse("http://code.google.com/p/android-money-manager-ex/wiki/DropboxSync"));
                    startActivity(intent);
                    return false;
                }
            });
        }

        // Select file from app folder

        final Preference pDropboxFile = findPreference(getString(PreferenceConstants.PREF_DROPBOX_LINKED_FILE));
        if (pDropboxFile != null) {
            pDropboxFile.setSummary(mDropboxHelper.getLinkedRemoteFile());
            // check if summary is null and
            if (TextUtils.isEmpty(pDropboxFile.getSummary())) {
                pDropboxFile.setSummary(R.string.click_to_select_file_dropbox);
            }
            // open Dropbox Browser Activity
            pDropboxFile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    selectFileFromAppDirectory();
                    return false;
                }
            });
        }

        // Select file anywhere in Dropbox

        final Preference dropboxFileAll = findPreference(getString(PreferenceConstants.PREF_DROPBOX_LINKED_FILE_ALL));
        if (dropboxFileAll != null) {
            dropboxFileAll.setSummary(mDropboxHelper.getLinkedRemoteFile());
            // check if summary is null and
            if (TextUtils.isEmpty(dropboxFileAll.getSummary())) {
                dropboxFileAll.setSummary(R.string.dropbox_file_summary_all);
            }
            // open Dropbox Browser Activity
            dropboxFileAll.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
//                    showDropboxChooser();
                    showNoNonsenseFilePicker();
                    return false;
                }
            });
        }

        //force download
        PreferenceScreen pDownload = (PreferenceScreen) findPreference(getString(PreferenceConstants.PREF_DROPBOX_DOWNLOAD));
        if (pDownload != null) {
            pDownload.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    downloadFileFromDropbox();
                    return true;
                }
            });
        }

        //force upload
        PreferenceScreen pUpload = (PreferenceScreen) findPreference(getString(PreferenceConstants.PREF_DROPBOX_UPLOAD));
        if (pUpload != null) {
            pUpload.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String dropboxFile = mDropboxHelper.getLinkedRemoteFile();
                    if (TextUtils.isEmpty(dropboxFile)) {
                        dropboxFile = "/" + new File(MoneyManagerApplication.getDatabasePath(getActivity().getApplicationContext())).getName();
                    }
                    uploadFileFromDropbox(dropboxFile);

                    return false;
                }
            });
        }

        //times repeat
        ListPreference pRepeats = (ListPreference) findPreference(getString(PreferenceConstants.PREF_DROPBOX_TIMES_REPEAT));
        if (pRepeats != null) {
            pRepeats.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mDropboxHelper.sendBroadcastStartServiceScheduled(DropboxScheduler.ACTION_CANCEL);
                    mDropboxHelper.sendBroadcastStartServiceScheduled(DropboxScheduler.ACTION_START);
                    return true;
                }
            });
        }
    }

    private void handleFileSelection(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null) return;

        final Preference pDropboxFile = findPreference(getString(PreferenceConstants.PREF_DROPBOX_LINKED_FILE));
        if (pDropboxFile == null) return;

        CharSequence oldFile = "", newFile;
        if (!TextUtils.isEmpty(pDropboxFile.getSummary())) {
            oldFile = pDropboxFile.getSummary();
        }
        newFile = data.getStringExtra(DropboxBrowserActivity.INTENT_DROBPOXFILE_PATH);

        if (newFile == null) return;

        // save value
        mDropboxHelper.setLinkedRemoteFile(newFile.toString());
        pDropboxFile.setSummary(newFile);
        // check if files is modified
        if (!oldFile.equals(newFile)) {
            // force download file
            downloadFileFromDropbox();
        }
    }

//    private void handleFileSelectionAll(int resultCode, Intent data) {
//        if (resultCode != Activity.RESULT_OK || data == null) return;
//
//        DbxChooser.Result result = new DbxChooser.Result(data);
//        String fileName = result.getName();
//        Uri uri = result.getLink();
//    }
//
//    private void showDropboxChooser() {
//        DbxChooser chooser = new DbxChooser(DropboxHelper.DROPBOX_APP_KEY);
//        chooser.forResultType(DbxChooser.ResultType.DIRECT_LINK)
//            .launch(this, REQUEST_DBX_CHOOSER);
//        // PREVIEW_LINK
//    }

    private void showNoNonsenseFilePicker() {
//        Intent i = new Intent(getActivity(), FilePickerActivity.class);
    }

    private void selectFileFromAppDirectory() {
        Intent intent = new Intent(getActivity(), DropboxBrowserActivity.class);
        intent.putExtra(DropboxBrowserActivity.INTENT_DROBPOXFILE_PATH, mDropboxHelper.getLinkedRemoteFile());
        startActivityForResult(intent, REQUEST_DROPBOX_FILE);
    }

    private void showWebTipsDialog(final String key, final CharSequence title, final int rawResources, boolean force) {
        if (!force) {
            if (getActivity().getSharedPreferences(TipsDialogFragment.PREF_DIALOG, 0).getBoolean(key, false))
                return;
        }
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getActivity())
            .setTitle(title);
        // view body
        @SuppressLint("InflateParams")
        final LinearLayout view = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.dialog_tips, null);
        // set invisible tips
        final TextView textTips = (TextView) view.findViewById(R.id.textViewTips);
        textTips.setVisibility(View.GONE);
        // set webView
        final WebView webTips = (WebView) view.findViewById(R.id.webViewTips);
        webTips.loadData(MmexFileUtils.getRawAsString(getActivity().getApplicationContext(), rawResources), "text/html", "UTF-8");
        webTips.setVisibility(View.VISIBLE);

        final CheckBox checkDont = (CheckBox) view.findViewById(R.id.checkBoxDontShow);
        checkDont.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getActivity().getSharedPreferences(TipsDialogFragment.PREF_DIALOG, 0).edit().putBoolean(key, isChecked).commit();
            }
        });
        // bug CheckBox object of Android
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final double scale = getActivity().getResources().getDisplayMetrics().density;
            checkDont.setPadding(checkDont.getPaddingLeft() + (int) (40.0f * scale + 0.5f),
                    checkDont.getPaddingTop(),
                    checkDont.getPaddingRight(),
                    checkDont.getPaddingBottom());
        }
        alertDialog.setView(view);
        // set neutral button
        alertDialog.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // set auto close to false
        alertDialog.setCancelable(false);
        // show dialog
        alertDialog.create().show();
    }

    private void uploadFileFromDropbox(String dropboxFile) {
        // compose intent to launch service for download
        Intent service = new Intent(getActivity().getApplicationContext(), DropboxServiceIntent.class);
        service.setAction(DropboxServiceIntent.INTENT_ACTION_UPLOAD);
        service.putExtra(DropboxServiceIntent.INTENT_EXTRA_LOCAL_FILE, MoneyManagerApplication.getDatabasePath(getActivity().getApplicationContext()));
        service.putExtra(DropboxServiceIntent.INTENT_EXTRA_REMOTE_FILE, dropboxFile);
        // toast to show
        Toast.makeText(getActivity().getApplicationContext(), R.string.dropbox_upload_is_starting, Toast.LENGTH_LONG).show();
        // start service
        getActivity().startService(service);
    }

    private void downloadFileFromDropbox() {
        // must send the parent activity here in order to display the progress dialog.
        DropboxSettingsActivity parent = (DropboxSettingsActivity) getActivity();
        DropboxManager dropbox = new DropboxManager(parent, mDropboxHelper);
        dropbox.downloadFromDropbox();
    }
}
