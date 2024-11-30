/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.about;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.github.pedrovgs.lynx.LynxActivity;
import com.github.pedrovgs.lynx.LynxConfig;
import com.google.common.base.Charsets;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.home.RecentDatabasesProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Calendar;

import javax.inject.Inject;

import dagger.Lazy;
import timber.log.Timber;

public class AboutFragment extends Fragment {
    private static Fragment mInstance;
    @Inject
    Lazy<RecentDatabasesProvider> mDatabases;

    public static Fragment newInstance() {
        if (mInstance == null) {
            mInstance = new AboutFragment();
        }

        return mInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MmexApplication.getApp().iocComponent.inject(this);

        String text, version;
        View view = inflater.inflate(R.layout.about_fragment, container, false);

        MmxBaseFragmentActivity activity = (MmxBaseFragmentActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Version application
        TextView txtVersion = view.findViewById(R.id.textViewVersion);
        Core core = new Core(getActivity());
        version = core.getAppVersionName();
        int build = core.getAppVersionCode();
        txtVersion.setText(getString(R.string.version) + " " + version + " (" + build + ")");
        text = "<u>" + txtVersion.getText() + "</u>";
        txtVersion.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        txtVersion.setOnClickListener(v -> {
            setClipboard(getActivity(), version);
            Toast.makeText(getActivity(), R.string.version_copied_to_clipboard, Toast.LENGTH_SHORT).show();
        } );

        // + " (" + getString(R.string.build) + " " + build + ")"
        //Copyright
        TextView textViewCopyright = view.findViewById(R.id.textViewCopyright);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String copyrightString = getString(R.string.application_copyright, currentYear);
        textViewCopyright.setText(copyrightString);

        // Send Feedback
        TextView txtFeedback = view.findViewById(R.id.textViewLinkFeedback);
        text = "<u>" + txtFeedback.getText() + "</u>";
        txtFeedback.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        txtFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ Constants.EMAIL });
            intent.putExtra(Intent.EXTRA_SUBJECT, "MoneyManagerEx for Android: Feedback");
            try {
                startActivity(Intent.createChooser(intent, "Send mail..."));
            } catch (Exception e) {
                Timber.e(e);
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // rate application

        // Open Issue
        TextView openIssues = view.findViewById(R.id.textOpenIssues);
        text = "<u>" + openIssues.getText() + "</u>";
        openIssues.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        openIssues.setMovementMethod(LinkMovementMethod.getInstance());
        OnClickListenerUrl clickListenerOpenTracker = new OnClickListenerUrl();
        clickListenerOpenTracker.setUrl(getApplicationWithUrl());
        openIssues.setOnClickListener(clickListenerOpenTracker);


        // application issue tracker
        TextView txtIssues = view.findViewById(R.id.textViewIssuesTracker);
        text = "<u>" + txtIssues.getText() + "</u>";
        txtIssues.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        txtIssues.setMovementMethod(LinkMovementMethod.getInstance());
        OnClickListenerUrl clickListenerIssuesTracker = new OnClickListenerUrl();
        clickListenerIssuesTracker.setUrl("https://github.com/moneymanagerex/android-money-manager-ex/issues/");
        txtIssues.setOnClickListener(clickListenerIssuesTracker);

        // MMEX for Android web page
        TextView txtWebsite = view.findViewById(R.id.textViewWebSite);
        text = "<u>" + txtWebsite.getText() + "</u>";
        String htmlText;
        htmlText = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();
        txtWebsite.setText(htmlText);
        txtWebsite.setMovementMethod(LinkMovementMethod.getInstance());
        OnClickListenerUrl clickListenerWebsite = new OnClickListenerUrl();
        clickListenerWebsite.setUrl("http://android.moneymanagerex.org/?utm_campaign=Application_Android&utm_medium=MMEX_" + version + "&utm_source=Website");
        txtWebsite.setOnClickListener(clickListenerWebsite);

        // report set link
        TextView txtReport = view.findViewById(R.id.textViewLinkWebSite);
        text = "<u>" + txtReport.getText() + "</u>";
        htmlText = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();
        txtReport.setText(htmlText);
        txtReport.setMovementMethod(LinkMovementMethod.getInstance());
        OnClickListenerUrl clickListenerFeedback = new OnClickListenerUrl();
        clickListenerFeedback.setUrl("http://www.moneymanagerex.org/?utm_campaign=Application_Android&utm_medium=MMEX_" + version + "&utm_source=Website");
        txtReport.setOnClickListener(clickListenerFeedback);

        // image view github
        OnClickListenerUrl clickListenerGithub = new OnClickListenerUrl();
        clickListenerGithub.setUrl("https://github.com/moneymanagerex/android-money-manager-ex");
        ImageView imageViewGithub = view.findViewById(R.id.imageViewGithub);
        imageViewGithub.setOnClickListener(clickListenerGithub);
        // image view twitter
        OnClickListenerUrl clickListenerTwitter = new OnClickListenerUrl();
        clickListenerTwitter.setUrl("https://twitter.com/MoneyManagerEx");
        ImageView imageViewTwitter = view.findViewById(R.id.imageViewTwitter);
        imageViewTwitter.setOnClickListener(clickListenerTwitter);
        // GPLv2 license
        TextView txtLicense = view.findViewById(R.id.textViewLicense);
        text = "<u>" + txtLicense.getText() + "</u>";
        txtLicense.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        OnClickListenerUrl clickListenerLicense = new OnClickListenerUrl();
        clickListenerLicense.setUrl("http://www.gnu.org/licenses/old-licenses/gpl-2.0.html");
        txtLicense.setOnClickListener(clickListenerLicense);
        // logcat
        TextView txtLogcat = view.findViewById(R.id.textViewLogcat);
        text = "<u>" + txtLogcat.getText() + "</u>";
        txtLogcat.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
        txtLogcat.setOnClickListener(v -> {
            LynxConfig lynxConfig = new LynxConfig();
            lynxConfig.setMaxNumberOfTracesToShow(4000);

            Intent lynxActivityIntent = LynxActivity.getIntent(getActivity(), lynxConfig);
            startActivity(lynxActivityIntent);
        });

        // Send logcat button
        Button sendLogcatButton = view.findViewById(R.id.sendLogcatButton);
        sendLogcatButton.setOnClickListener(v -> sendLogcat());
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
                Timber.e(e);
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

    private void sendLogcat() {
        String logcat = getLogcat();

        //send file using email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.EMAIL});
        // the attachment
//        emailIntent .putExtra(Intent.EXTRA_STREAM, outputFile.getAbsolutePath());
        emailIntent.putExtra(Intent.EXTRA_TEXT, logcat);
        // the mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Subject");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } catch (Exception e) {
            Timber.e(e, "opening email with logcat");
        }
    }

    /**
     *
     * @return
     * References
     * <a href="http://developer.android.com/tools/debugging/debugging-log.html">...</a>
     */
    private String getLogcat() {
//        File outputFile = new File(Environment.getDefaultDatabaseDirectory(), "logcat.txt");
        Process p = null;
        try {
//            Runtime.getRuntime().exec(
//                    "logcat -f " + outputFile.getAbsolutePath());
            p = Runtime.getRuntime().exec("logcat -d");
        } catch (IOException e) {
            Timber.e(e, "executing logcat");
        }
        if (p == null) return "";

        // Read text from the command output.
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()) );
        StringBuilder output = new StringBuilder();
        String line;
        try {
            while ((line = in.readLine()) != null) {
                output.append(line);
                output.append(System.getProperty("line.separator"));
            }

            in.close();
        } catch (IOException e) {
            Timber.e(e, "reading stdout");
        }

        return output.toString();
    }

    /**
     * ProcessBuilder may be used to redirect stdout for a process. Need to try it out.
     */
    private void useProcessBuilder() {
        ProcessBuilder pb = new ProcessBuilder("logcat -d");

    }

    private void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);

    }

    private String getApplicationWithUrl() {
        String body;
        Core core = new Core(getActivity());
        int build = core.getAppVersionCode();
        DatabaseMetadata db = mDatabases.get().getCurrent();



        // Todo Add schema for remote url db
        body = "[Put here your description]\n" +
                "App Version: " + core.getAppVersionName() + " (" + build + ")\n"+
                "Model: " + android.os.Build.MODEL + "\n"+
                "Manufactur: " + android.os.Build.MANUFACTURER +"\n" +
                "CodeName: " + Build.VERSION.CODENAME +"\n" +
                "Release: " + Build.VERSION.RELEASE + "\n" +
                "Api:" + Build.VERSION.SDK_INT +"\n" +
                "LocalDB: " + ( db == null ? "na" : db.localPath )+ "\n" +
                "RemoteDB: " + ( db == null ? "na" : db.remotePath) + "\n" ;

        String uri = Uri.parse("https://github.com/moneymanagerex/android-money-manager-ex/issues/new")
                .buildUpon()
                .appendQueryParameter("label", "bug")
//                .appendQueryParameter("title", "Your title here")
                .appendQueryParameter("body", body)
                .build().toString();
        Timber.d("github open issue url: %s", uri);
        return uri;
    }

}
