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

import com.money.manager.ex.DonateActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.fragment.BaseFragmentActivity;

public class AboutFragment extends Fragment {
    private static final String LOGCAT = AboutFragment.class.getSimpleName();

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String text, version = "", build = "";
        View view = inflater.inflate(R.layout.about_activity, container, false);

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
        // rate application
        TextView txtRate = (TextView) view.findViewById(R.id.textViewLinkRate);
        text = "<u>" + txtRate.getText() + "</u>";
        txtRate.setText(Html.fromHtml(text));
        txtRate.setMovementMethod(LinkMovementMethod.getInstance());
        OnClickListenerUrl clickListenerRate = new OnClickListenerUrl();
        clickListenerRate.setUrl("http://play.google.com/store/apps/details?id=com.money.manager.ex");
        txtRate.setOnClickListener(clickListenerRate);
        // report set link
        TextView txtReport = (TextView) view.findViewById(R.id.textViewLinkWebSite);
        text = "<u>" + txtReport.getText() + "</u>";
        txtReport.setText(Html.fromHtml(text));
        txtReport.setMovementMethod(LinkMovementMethod.getInstance());
        OnClickListenerUrl clickListenerFeedback = new OnClickListenerUrl();
        clickListenerFeedback.setUrl("http://www.moneymanagerex.org/?utm_campaign=Application_Android&utm_medium=MMEX_" + version + "&utm_source=Website");
        txtReport.setOnClickListener(clickListenerFeedback);
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

}
