package com.money.manager.ex.assetallocation;

import android.os.Bundle;
import android.app.Activity;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;
import android.util.Xml;
import android.webkit.WebView;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.domainmodel.AssetClass;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AssetAllocationOverviewActivity
    extends BaseFragmentActivity {

    public static final String INTENT_ASSET_ALLOCATION = "INTENT_ASSET_ALLOCATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_allocation_overview);

        // get asset allocation
        Parcelable x = getIntent().getParcelableExtra(INTENT_ASSET_ALLOCATION);
        AssetClass test = (AssetClass) x;

        // create a HTML display.
        String html = createHtml(test);
        displayOverview(html);
    }

    private String createHtml(AssetClass allocation) {
        //DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        String html = "<html><body style='background: gray'>" +
            "<table>" +
                "<thead>" +
                    "<tr>" +
                        "<th>Asset Class</th>" +
                        "<th>Allocation</th>" +
                        "<th>Current</th>" +
                        "<th>Allocation</th>" +
                        "<th>Value</th>" +
                        "<th>% Diff</th>" +
                        "<th>Difference</th>" +
                    "</tr>" +
                "</thead>" +
                "<tbody>" +
                    "<tr>" +
                        "<td>Cash</td>" +
                        "<td>3%</td>" +
                        "<td>100</td>" +
                        "<td>2.5</td>" +
                        "<td>250</td>" +
                        "<td>13%</td>" +
                        "<td>150</td>" +
                    "</tr>" +
                "</tbody>" +
            "</table>" +
            "</body></html>";
        return html;
    }

    private void displayOverview(String html) {
//        // text view version
//        TextView overviewTextView = (TextView) this.findViewById(R.id.overviewTextView);
//        overviewTextView.setText(Html.fromHtml(html));

        // web view version
        WebView overview = (WebView) this.findViewById(R.id.overviewWebView);
        overview.loadData(html, "text/html", null);
    }
}
