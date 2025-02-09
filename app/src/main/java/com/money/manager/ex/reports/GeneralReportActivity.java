package com.money.manager.ex.reports;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import android.text.TextUtils;

public class GeneralReportActivity extends MmxBaseFragmentActivity {

    public static final String GENERAL_REPORT_NAME = "GeneralReportActivity:ReportName";
    public static final String GENERAL_REPORT_GROUP_NAME = "GeneralReportActivity:GroupName";
    public static String currentReportName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_general_report);

        if (getIntent() != null && !TextUtils.isEmpty(getIntent().getStringExtra(GENERAL_REPORT_NAME)) ) {
            currentReportName = getIntent().getStringExtra(GENERAL_REPORT_NAME);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // set actionbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(currentReportName);
        }

        GeneralReportFragment fragment = new GeneralReportFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.GeneralReportFragment, fragment, GeneralReportFragment.class.getSimpleName())
                .commit();

    }

}
