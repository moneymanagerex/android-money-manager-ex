package com.money.manager.ex.errorhandle;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.money.manager.ex.R;

public class CrashReportActivity extends AppCompatActivity {
    Intent intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (intent != null) return;

        setContentView(R.layout.activity_auth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        handleIntent();
    }

    void handleIntent() {
        if (intent != null) return;

        intent = getIntent();
        if (intent == null) return;
        if ( ! getIntent().getAction().equals("HANDLE_ERROR") ) {
            return;
        }

        String source = intent.getStringExtra("ERROR");
        String report = intent.getStringExtra(Intent.EXTRA_TEXT);

        TextView reportUI = findViewById(R.id.editTextReport);
        reportUI.setText(report);

        Button openIssue = findViewById(R.id.buttonOpenIssue);
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

        Button cancel = findViewById(R.id.buttonQuit);
        cancel.setOnClickListener(view -> closeActivity());
    }

    private void closeActivity() {
        this.finish();
    }
}