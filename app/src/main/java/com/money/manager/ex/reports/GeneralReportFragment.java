package com.money.manager.ex.reports;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.money.manager.ex.R;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.settings.AppSettings;

public class GeneralReportFragment extends Fragment {

    private static Fragment mInstance;

    /// Db setup
    public static MmxOpenHelper MmxHelper;
    public static SQLiteDatabase db;

    public static Fragment newInstance(int page) {
        if (mInstance == null) {
            mInstance = new GeneralReportFragment();
        }
        return mInstance;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup group,
                             Bundle saved) {
        return inflater.inflate(R.layout.fragment_general_report, group, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final AppSettings app_settings = new AppSettings(getActivity());

        // Db setup
        MmxHelper = new MmxOpenHelper(getActivity(), app_settings.getDatabaseSettings().getDatabasePath());
        db = MmxHelper.getReadableDatabase();

        WebView webView = (WebView) getActivity().findViewById(R.id.GeneralReportWebView);

        // Display Unicode characters.
        WebSettings settings = webView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");

        webView.loadData(getHtmlReport(GeneralReportActivity.currentReportName),
                "text/html", "utf-8");

        //set adapter
        //GeneralReportAdapter adapter = new GeneralReportAdapter(getActivity(), null);
        //setListAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private String getHtmlReport(String reportName){

        String sqlQuery = "";
        String htmlTable = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
                "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><style>\n" +
                "#GeneralReport {\n" +
                "    font-family: \"Trebuchet MS\", Arial, Helvetica, sans-serif;\n" +
                "    border-collapse: collapse;\n" +
                "    width: 100%;}\n" +
                "#GeneralReport td, #GeneralReport th {\n" +
                "    border: 1px solid #ddd;\n" +
                "    padding: 8px;}\n" +
                "#GeneralReport tr:nth-child(even){background-color: #f2f2f2;}\n" +
                "#GeneralReport tr:hover {background-color: #ddd;}\n" +
                "#GeneralReport th {\n" +
                "    padding-top: 12px;\n" +
                "    padding-bottom: 12px;\n" +
                "    text-align: left;\n" +
                "    background-color: #4CAF50;\n" +
                "    color: white;}\n" +
                "</style></head><body>";

        try
        {
            if(!reportName.trim().isEmpty()) {

                String sql = "SELECT SQLCONTENT FROM REPORT_V1 " +
                        "WHERE REPORTNAME = '" + reportName + "' " +
                        "ORDER BY REPORTNAME LIMIT 1";

                Cursor sqlCursor = db.rawQuery(sql, null);

                if(sqlCursor.moveToFirst())
                {
                    sqlQuery = sqlCursor.getString(sqlCursor.getColumnIndex("SQLCONTENT"));
                }

                sqlCursor.close();
            }

            // fetch the data and generate the html table
            Cursor sqlCursor = db.rawQuery(sqlQuery, null);

            htmlTable = htmlTable + "<table id=\"GeneralReport\"><tr>";

            //get the clmns
            for (int i = 0; i < sqlCursor.getColumnCount(); i++)
            {
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

            sqlCursor.close();

            htmlTable = htmlTable + "</table></body></html>";
        }
        catch(Exception e)
        {
            //System.err.println("EXCEPTION:"+e);
        }

        return htmlTable;
    }
}