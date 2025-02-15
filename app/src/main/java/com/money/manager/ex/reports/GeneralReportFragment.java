package com.money.manager.ex.reports;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.sqlite.db.SupportSQLiteDatabase;

import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.datalayer.ReportRepository;
import com.money.manager.ex.domainmodel.Report;
import com.money.manager.ex.settings.AppSettings;

import androidx.fragment.app.Fragment;

import java.util.List;


public class GeneralReportFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup group,
                             Bundle saved) {
        return inflater.inflate(R.layout.fragment_general_report, group, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        WebView webView = (WebView) getActivity().findViewById(R.id.GeneralReportWebView);
        String htmlContent = getHtmlReport(GeneralReportActivity.currentReportName);

        webView.setWebViewClient(new WebViewClient());
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
        //Log.d("TAG-htmlReport", htmlContent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private String getHtmlReport(String reportName){

        //Db setup
        MmxOpenHelper MmxHelper = new MmxOpenHelper(getActivity(), new AppSettings(getActivity()).getDatabaseSettings().getDatabasePath());
        //CurrencyService currencyService = new CurrencyService(getActivity().getApplicationContext());

        String sqlQuery = "";
        String htmlTable = "<html><head><style>" +
               "#GeneralReport { font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif; border-collapse: collapse; width: 100%;} " +
               "#GeneralReport td, #GeneralReport th { border: 1px solid #ddd; padding: 8px;} " +
               "#GeneralReport tr:nth-child(even){background-color: #f2f2f2;} " +
                "#GeneralReport tr:hover {background-color: #ddd;} " +
               "#GeneralReport th {padding-top: 12px; padding-bottom: 12px; text-align: left; background-color: #4CAF50; color: white;} " +
                "</style></head><body>";

        try
        {
            ReportRepository repo = new ReportRepository(getActivity());
            List<Report> report = repo.loadByName(reportName.trim());

            if (!report.isEmpty()) {
                sqlQuery = report.get(0).getSqlContent();
            }

            //fetch the data and generate the html table
            Cursor sqlCursor = MmxHelper.getReadableDatabase().query(sqlQuery, null);

            htmlTable = htmlTable + "<table id='GeneralReport'><tr>";

            //get the clmns
            for (int i = 0; i < sqlCursor.getColumnCount(); i++) {
                htmlTable = htmlTable + "<th>" + sqlCursor.getColumnName(i) + "</th>";
            }

            htmlTable = htmlTable + "</tr>";

            //get the data
            for (sqlCursor.moveToFirst(); !sqlCursor.isAfterLast(); sqlCursor.moveToNext())
            {
                htmlTable = htmlTable + "<tr>";

                for (int i = 0; i < sqlCursor.getColumnCount(); i++)
                {
                    htmlTable = htmlTable + "<td>" +
                            sqlCursor.getString(sqlCursor.getColumnIndex(sqlCursor.getColumnName(i))) +
                            "</td>";
                }

                htmlTable = htmlTable + "</tr>";
            }

            htmlTable = htmlTable + "</table></body></html>";

            sqlCursor.close();
        }
        catch(Exception e) {
            //System.err.println("EXCEPTION:"+e);
        }

        return htmlTable;
    }
}