/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.about;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pedrovgs.lynx.LynxActivity;
import com.github.pedrovgs.lynx.LynxConfig;
import com.money.manager.ex.DonateActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.fragment.BaseFragmentActivity;

public class AboutFragment extends Fragment {
    private static final String LOGCAT = AboutFragment.class.getSimpleName();
    private static Fragment mInstance;

    public static Fragment newInstance(int page) {
        if (mInstance == null) {
            mInstance = new AboutFragment();
        }
        return mInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String text, version = "", build = "";
        View view = inflater.inflate(R.layout.about_fragment, container, false);

        BaseFragmentActivity activity = (BaseFragmentActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Version application
        TextView txtVersion = (TextView) view.findViewById(R.id.textViewVersion);
        try {
            version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            build = Integer.toString(getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode);
            txtVersion.setText(txtVersion.getText() + " " + version + " (" + getString(R.string.build) + " " + build + ")");
        } catch (NameNotFoundException e) {
            Log.e(LOGCAT, e.getMessage());
        }

        // take a object into layout
        TextView txtFeedback = (TextView) view.findViewById(R.id.textViewLinkFeedback);
        text = "<u>" + txtFeedback.getText() + "</u>";
        txtFeedback.setText(Html.fromHtml(text));
        txtFeedback.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"android.money.manager.ex@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "MoneyManagerEx for Android: Feedback");
                try {
                    startActivity(Intent.createChooser(intent, "Send mail..."));
                } catch (Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        // logcat
        TextView txtLogcat = (TextView) view.findViewById(R.id.textViewLogcat);
        text = "<u>" + txtLogcat.getText() + "</u>";
        txtLogcat.setText(Html.fromHtml(text));
        txtLogcat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LynxConfig lynxConfig = new LynxConfig();
                lynxConfig.setMaxNumberOfTracesToShow(4000);

                Intent lynxActivityIntent = LynxActivity.getIntent(getActivity(), lynxConfig);
                startActivity(lynxActivityIntent);
            }
        });
        // rate application
        TextView txtRate = (TextView) view.findViewById(R.id.textViewLinkRate);
        text = "<u>" + txtRate.getText() + "</u>";
        txtRate.setText(Html.fromHtml(text));
        txtRate.setMovementMethod(LinkMovementMethod.getInstance());
        OnClickListenerUrl clickListenerRate = new OnClickListenerUrl();
        clickListenerRate.setUrl("http://play.google.com/store/apps/details?id=com.money.manager.ex");
        txtRate.setOnClickListener(clickListenerRate);

        // issues tracker application
        TextView txtIssues = (TextView) view.findViewById(R.id.textViewIssuesTracker);
        text = "<u>" + txtIssues.getText() + "</u>";
        txtIssues.setText(Html.fromHtml(text));
        txtIssues.setMovementMethod(LinkMovementMethod.getInstance());
        OnClickListenerUrl clickListenerIssuesTracker = new OnClickListenerUrl();
        clickListenerIssuesTracker.setUrl("https://github.com/moneymanagerex/android-money-manager-ex/issues/");
        txtIssues.setOnClickListener(clickListenerIssuesTracker);

        // report set link
        TextView txtReport = (TextView) view.findViewById(R.id.textViewLinkWebSite);
        text = "<u>" + txtReport.getText() + "</u>";
        txtReport.setText(Html.fromHtml(text));
        txtReport.setMovementMethod(LinkMovementMethod.getInstance());
        OnClickListenerUrl clickListenerFeedback = new OnClickListenerUrl();
        clickListenerFeedback.setUrl("http://www.moneymanagerex.org/?utm_campaign=Application_Android&utm_medium=MMEX_" + version + "&utm_source=Website");
        txtReport.setOnClickListener(clickListenerFeedback);

        // MMEX for Android web page
        TextView txtWebsite = (TextView) view.findViewById(R.id.textViewWebSite);
        text = "<u>" + txtWebsite.getText() + "</u>";
        txtWebsite.setText(Html.fromHtml(text));
        txtWebsite.setMovementMethod(LinkMovementMethod.getInstance());
        OnClickListenerUrl clickListenerWebsite = new OnClickListenerUrl();
        clickListenerWebsite.setUrl("http://moneymanagerex.github.io/android-money-manager-ex/");
        txtWebsite.setOnClickListener(clickListenerWebsite);

        // image view google plus
        OnClickListenerUrl clickListenerGooglePlus = new OnClickListenerUrl();
        clickListenerGooglePlus.setUrl("http://goo.gl/R693Ih");
        ImageView imageViewGooglePlus = (ImageView) view.findViewById(R.id.imageViewGooglePlus);
        imageViewGooglePlus.setOnClickListener(clickListenerGooglePlus);
        // image view github
        OnClickListenerUrl clickListenerGithub = new OnClickListenerUrl();
        clickListenerGithub.setUrl("https://github.com/moneymanagerex/android-money-manager-ex");
        ImageView imageViewGithub = (ImageView) view.findViewById(R.id.imageViewGithub);
        imageViewGithub.setOnClickListener(clickListenerGithub);
        // image view twitter
        OnClickListenerUrl clickListenerTwitter = new OnClickListenerUrl();
        clickListenerTwitter.setUrl("https://twitter.com/MMEXForAndroid");
        ImageView imageViewTwitter = (ImageView) view.findViewById(R.id.imageViewTwitter);
        imageViewTwitter.setOnClickListener(clickListenerTwitter);
        // GPLv2 license
        TextView txtLicense = (TextView) view.findViewById(R.id.textViewLicense);
        text = "<u>" + txtLicense.getText() + "</u>";
        txtLicense.setText(Html.fromHtml(text));
        OnClickListenerUrl clickListenerLicense = new OnClickListenerUrl();
        clickListenerLicense.setUrl("http://www.gnu.org/licenses/old-licenses/gpl-2.0.html");
        txtLicense.setOnClickListener(clickListenerLicense);
        // donate
        Button buttonDonate = (Button) view.findViewById(R.id.buttonDonateInApp);
        buttonDonate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), DonateActivity.class));
            }
        });

        return view;
    }

    // implement a class to manage the opening of several url
    private class OnClickListenerUrl implements OnClickListener {
        private String mUrl;

        @Override
        public void onClick(View v) {
            if (TextUtils.isEmpty(getUrl()))
                return;
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getUrl()));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        public String getUrl() {
            return mUrl;
        }

        public void setUrl(String mUrl) {
            this.mUrl = mUrl;
        }

    }

}
