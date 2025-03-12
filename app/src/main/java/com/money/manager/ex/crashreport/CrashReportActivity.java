package com.money.manager.ex.crashreport;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;

import timber.log.Timber;

public class CrashReportActivity extends AppCompatActivity {
    private Intent intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        handleIntent();
    }


    void handleIntent() {
        if (intent != null) return;

        intent = getIntent();
        if (intent == null) return;

        String source = intent.getStringExtra("ERROR");
        String report = intent.getStringExtra(Intent.EXTRA_TEXT);

        TextView reportUI = findViewById(R.id.editTextReport);
        if (reportUI != null)
            reportUI.setText(report);

        Button sendMail = findViewById(R.id.buttonMail);
        if (sendMail != null) {
            sendMail.setOnClickListener(v -> {
                // send mail
                //send file using email
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.EMAIL});
                emailIntent.putExtra(Intent.EXTRA_TEXT, report);
                // the mail subject
                emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Android Crash Report:");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                } catch (Exception e) {
//                    Timber.e(e, "opening email with logcat");
                }

            });
        }


        Button openIssue = findViewById(R.id.buttonOpenIssue);
        if ( openIssue != null ) {
            openIssue.setOnClickListener(v -> {
                String body = "[Put here your description]\n" + report;
                String uri = Uri.parse("https://github.com/moneymanagerex/android-money-manager-ex/issues/new")
                        .buildUpon()
                        .appendQueryParameter("label", "bug")
//                .appendQueryParameter("title", "Your title here")
                        .appendQueryParameter("body", body)
                        .build().toString();
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                this.startActivity(myIntent);

                closeActivity();

            });
        }

        Button cancel = findViewById(R.id.buttonQuit);
        if ( cancel != null ) {
            cancel.setOnClickListener(view -> closeActivity());
        }
    }

    private void closeActivity() {
        this.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}