package com.money.manager.ex.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.datalayer.ReportRepository;
import com.money.manager.ex.domainmodel.Report;

import androidx.fragment.app.Fragment;

import java.util.Map;

import info.javaperformance.money.MoneyFactory;


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

        StringBuilder htmlTable = new StringBuilder("<html><head><style>" +
                "#GeneralReport { font-family: 'Trebuchet MS', Arial, Helvetica, sans-serif; border-collapse: collapse; width: 100%;} " +
                "#GeneralReport td, #GeneralReport th { border: 1px solid #ddd; padding: 8px;} " +
                "#GeneralReport tr:nth-child(even){background-color: #f2f2f2;} " +
                "#GeneralReport tr:hover {background-color: #ddd;} " +
                "#GeneralReport th {padding-top: 12px; padding-bottom: 12px; text-align: left; background-color: #4CAF50; color: white;} " +
                "</style></head><body>");

        // Create Repo instance and load the report
        ReportRepository repo = new ReportRepository(getActivity());
        Report report = repo.loadByName(reportName.trim());

        CurrencyService currencyService = new CurrencyService(getActivity().getApplicationContext());

        // Execute query and get column names and results
        ReportRepository.ReportResult result = repo.runReport(report);

        // If no data, return an empty table message
        if (result.getColumnNames() == null || result.getQueryResult() == null) {
            return "<p>No data available</p>";
        }

        // Start building the HTML table
        htmlTable.append("<table id='GeneralReport'><tr>");

        // Add column names (table header)
        for (String columnName : result.getColumnNames()) {
            htmlTable.append("<th>").append(columnName).append("</th>");
        }

        htmlTable.append("</tr>");

        // Add rows from query result
        for (Map<String, String> row : result.getQueryResult()) {
            htmlTable.append("<tr>");

            for (String columnName : result.getColumnNames()) {
                if(columnName.toLowerCase().contains("amount")
                        || columnName.toLowerCase().contains("deposit")
                        || columnName.toLowerCase().contains("withdrawal")
                        || columnName.toLowerCase().contains("total")
                        || columnName.toLowerCase().contains("initialbal")) {

                    double amount = 0;
                    String rawAmount = row.getOrDefault(columnName, "0");

                    //added for #2663
                    if (!rawAmount.isBlank() && !rawAmount.isEmpty()) {
                        amount = Double.valueOf(rawAmount); // default value is 0 #2604
                    }

                    if (amount < 0) {
                        htmlTable.append("<td style='color:red'>").append(currencyService.getCurrencyFormatted(currencyService.getBaseCurrencyId(),
                                MoneyFactory.fromDouble(amount))).append("</td>");
                    } else {
                        htmlTable.append("<td>").append(currencyService.getCurrencyFormatted(currencyService.getBaseCurrencyId(),
                                MoneyFactory.fromDouble(amount))).append("</td>");
                    }

                } else {
                    htmlTable.append("<td>").append(row.getOrDefault(columnName, "")).append("</td>");
                }
            }
            htmlTable.append("</tr>");
        }

        htmlTable.append("</table></body></html>");

        return htmlTable.toString();
    }
}