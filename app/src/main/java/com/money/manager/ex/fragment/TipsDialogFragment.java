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

package com.money.manager.ex.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.utils.AlertDialogWrapper;
import com.money.manager.ex.utils.MmxFileUtils;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class TipsDialogFragment extends DialogFragment {
    public static final String PREF_DIALOG = "com.money.manager.ex_tips_dialog_preferences";
    //Constant update instance
    private static final String KEY_KEY = "TipsDialogFragment:Key";
    private static final String KEY_TITLE = "TipsDialogFragment:Title";
    private static final String KEY_TIPS = "TipsDialogFragment:Tips";
    private static final String KEY_VISIBLE = "TipsDialogFragment:Visible";
    private static final String KEY_CHECK = "TipsDialogFragment:Check";
    private static final String KEY_RAW_WEB = "TipsDialogFragment:RawWeb";
    private static final String KEY_VIEW_AS_WEB = "TipsDialogFragment:ViewAsWeb";
    //member of TipsDialogFragment
    private String mKey;
    private CharSequence mTitle = "";
    private CharSequence mTips = "";
    private int mRawWeb = 0;
    private boolean mVisibleDontShowAgain = true;
    private boolean mCheckDontShowAgain = false;
    private boolean mCanShow = true;
    private boolean mViewAsWeb = false;

    public static TipsDialogFragment getInstance(Context context, String key) {
        return getInstance(context, key, false);
    }

    public static TipsDialogFragment getInstance(Context context, String key, boolean forceShow) {
        TipsDialogFragment tipsDialog = new TipsDialogFragment();
        tipsDialog.mCanShow = context.getSharedPreferences(PREF_DIALOG, 0).getBoolean(key, true) || forceShow;
        tipsDialog.setKey(key);

        return tipsDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            setKey(savedInstanceState.getString(KEY_KEY));
            setTitle(savedInstanceState.getString(KEY_TITLE));
            setTips(savedInstanceState.getString(KEY_TIPS));
            setRawWeb(savedInstanceState.getInt(KEY_RAW_WEB));
            setVisibleDontShowAgain(savedInstanceState.getBoolean(KEY_VISIBLE));
            setCheckDontShowAgain(savedInstanceState.getBoolean(KEY_CHECK));
            setViewAsWeb(savedInstanceState.getBoolean(KEY_VIEW_AS_WEB));
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_tips, null);
        // set tips
        TextView textTips = (TextView) view.findViewById(R.id.textViewTips);
        textTips.setText(getTips());
        textTips.setVisibility(!isViewAsWeb() ? View.VISIBLE : View.GONE);
        // webView
        WebView webView = (WebView) view.findViewById(R.id.webViewTips);
        webView.setVisibility(isViewAsWeb() ? View.VISIBLE : View.GONE);
        if (getRawWeb() != 0) {
            webView.loadData(MmxFileUtils.getRawAsString(getActivity(), getRawWeb()), "text/html", "UTF-8");
        }
        // check box
        CheckBox checkDont = (CheckBox) view.findViewById(R.id.checkBoxDontShow);
        checkDont.setVisibility(isVisibleDontShowAgain() ? View.VISIBLE : View.GONE);
        checkDont.setChecked(isCheckDontShowAgain());
        checkDont.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setCheckDontShowAgain(isChecked);
            }
        });
        // bug CheckBox object of Android
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final double scale = this.getResources().getDisplayMetrics().density;
            checkDont.setPadding(checkDont.getPaddingLeft() + (int) (40.0f * scale + 0.5f),
                    checkDont.getPaddingTop(),
                    checkDont.getPaddingRight(),
                    checkDont.getPaddingBottom());
        }

        return new AlertDialogWrapper(getContext())
            .setTitle(getTitle())
            .setView(view)
            .setCancelable(false)
        .setNeutralButton(android.R.string.ok, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (getActivity() != null) {
                    getActivity().getSharedPreferences(PREF_DIALOG, 0).edit().putBoolean(getKey(), !isCheckDontShowAgain()).commit();
                }
                dialog.dismiss();
            }
        })
         .create();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (mCanShow) super.show(manager, tag);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (getActivity() != null) {
            getActivity().getSharedPreferences(PREF_DIALOG, 0).edit().putBoolean(getKey(), !isCheckDontShowAgain()).commit();
        }
        super.onDismiss(dialog);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_KEY, getKey());
        outState.putString(KEY_TITLE, getTitle().toString());
        outState.putString(KEY_TIPS, getTips().toString());
        outState.putBoolean(KEY_VISIBLE, isVisibleDontShowAgain());
        outState.putBoolean(KEY_CHECK, isCheckDontShowAgain());
        super.onSaveInstanceState(outState);
    }

    /**
     * @return the mKey
     */
    public String getKey() {
        return mKey;
    }

    /**
     * @param mKey the mKey to set
     */
    public void setKey(String mKey) {
        this.mKey = mKey;
    }

    /**
     * @return the mTitle
     */
    public CharSequence getTitle() {
        return !TextUtils.isEmpty(mTitle) ? mTitle : getString(R.string.tips);
    }

    /**
     * @param mTitle the mTitle to set
     */
    public void setTitle(CharSequence mTitle) {
        this.mTitle = mTitle;
    }

    /**
     * @return the mTips
     */
    public CharSequence getTips() {
        return mTips;
    }

    /**
     * @param mTips the mTips to set
     */
    public void setTips(CharSequence mTips) {
        this.mTips = mTips;
    }

    /**
     * @return the mShowAgain
     */
    public boolean isVisibleDontShowAgain() {
        return mVisibleDontShowAgain;
    }

    /**
     * @param mShowAgain the mShowAgain to set
     */
    public void setVisibleDontShowAgain(boolean mShowAgain) {
        this.mVisibleDontShowAgain = mShowAgain;
    }

    /**
     * @return the mCheckShowAgain
     */
    public boolean isCheckDontShowAgain() {
        return mCheckDontShowAgain;
    }

    /**
     * @param mCheckShowAgain the mCheckShowAgain to set
     */
    public void setCheckDontShowAgain(boolean mCheckShowAgain) {
        this.mCheckDontShowAgain = mCheckShowAgain;
    }

    public boolean isViewAsWeb() {
        return mViewAsWeb;
    }

    public void setViewAsWeb(boolean mViewAsWeb) {
        this.mViewAsWeb = mViewAsWeb;
    }

    public int getRawWeb() {
        return mRawWeb;
    }

    public void setRawWeb(int mRawWeb) {
        this.mRawWeb = mRawWeb;
    }
}
