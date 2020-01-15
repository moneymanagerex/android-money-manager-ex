package com.money.manager.ex.reports;

import android.app.FragmentManager;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;

import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;

public class GeneralReportActivity
        extends MmxBaseFragmentActivity {

    public static final String GENERAL_REPORT_NAME = "GeneralReportActivity:ReportName";
    public static String currentReportName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_general_report);

        if (getIntent() != null) {
            if (!TextUtils.isEmpty(getIntent().getStringExtra(GENERAL_REPORT_NAME))) {
                currentReportName = getIntent().getStringExtra(GENERAL_REPORT_NAME);
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // set actionbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(currentReportName);
        }

        GeneralReportFragment fragment = new GeneralReportFragment();
        FragmentManager fm = getFragmentManager();

        if (fm.findFragmentById(R.id.GeneralReportFragment) == null) {
            fm.beginTransaction().add(R.id.GeneralReportFragment, fragment, GeneralReportFragment.class.getSimpleName()).commit();
        }
    }
}
