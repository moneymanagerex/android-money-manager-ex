package com.money.manager.ex.reports;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;

public class GeneralReportActivity extends MmxBaseFragmentActivity {

    public static final String GENERAL_REPORT_NAME = "GeneralReportActivity:ReportName";
    public static final String GENERAL_REPORT_GROUP_NAME = "GeneralReportActivity:GroupName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_general_report);

        String reportName = (getIntent() != null && getIntent().getStringExtra(GENERAL_REPORT_NAME) != null)
                ? getIntent().getStringExtra(GENERAL_REPORT_NAME) : "";

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(getToolbar());
            showStandardToolbarActions(getToolbar());
            // enable returning back from toolbar.
            setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(reportName);
        }

        GeneralReportFragment fragment = GeneralReportFragment.newInstance(reportName);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.GeneralReportFragment, fragment, GeneralReportFragment.class.getSimpleName())
                .commit();

    }

}
