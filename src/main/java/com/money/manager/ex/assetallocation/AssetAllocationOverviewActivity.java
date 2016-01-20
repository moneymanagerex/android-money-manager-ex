package com.money.manager.ex.assetallocation;

import android.os.Bundle;
import android.app.Activity;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;
import android.util.Xml;
import android.webkit.WebView;
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AssetAllocationOverviewActivity
    extends BaseFragmentActivity {

//    public static final String INTENT_ASSET_ALLOCATION = "INTENT_ASSET_ALLOCATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_allocation_overview);

        // get asset allocation
        AssetAllocationService service = new AssetAllocationService(this);
        AssetClass allocation = service.loadAssetAllocation();

        // create a HTML display.
        String html = createHtml(allocation);
        displayOverview(html);
    }

    private String createHtml(AssetClass allocation) {
        String html = "<html><body style='background: lightgray'>";

        // get content
        html += getList(allocation);

        html += "</body></html>";
        return html;
    }

    private void displayOverview(String html) {
        WebView overview = (WebView) this.findViewById(R.id.overviewWebView);
        overview.loadData(html, "text/html", null);
    }

    private void getTable(AssetClass allocation) {
        String html = "";
        html += "<table>" +
            "<thead>" +
            "<tr>" +
            "<th>Asset Class</th>" +
            "<th>Allocation</th>" +
            "<th>Value</th>" +
            "<th>Difference</th>" +
            "</tr>" +
            "</thead>" +
            "<tbody>";
        // summary row
        html += "<tr>" +
            "<td>" + allocation.getName() + "</td>" +
            "</tr>";
        // child rows
        html +=     "<tr>" +
            "<td>Cash</td>" +
            "<td>3% <br/> 100</td>" +
            "<td>2.5% <br/> 150</td>" +
            "<td>13% <br/> 50</td>" +
            "</tr>";
        // complete.
        html += "</tbody></table>";
    }

    private String getList(AssetClass allocation) {
        int defaultPrecision = 2;
        String html = "";
        html += "<ul>";
        html += "<li>" + allocation.getName() +
            ", " +
            allocation.getAllocation().truncate(defaultPrecision).toString() + "/";
            if (allocation.getDifference().toDouble() > 0) {
                html += "<span style='color: green; font-weight: bold;'>";
            } else {
                html += "<span style='color: darkred; font-weight: bold;'>";
            }
        html +=
            allocation.getCurrentAllocation().truncate(defaultPrecision).toString() +
            "</span>" +
            ", " +
            allocation.getValue().truncate(defaultPrecision).toString() + "/" +
            allocation.getCurrentValue().truncate(defaultPrecision).toString() +
            ", ";
        // paint the difference
        if (allocation.getDifference().toDouble() > 0) {
            html += "<span style='color: green;'>";
        } else {
            html += "<span style='color: darkred;'>";
        }
        html +=
            allocation.getDifference().truncate(defaultPrecision).toString();
        html += "</span>";
        // child elements
        for(AssetClass child : allocation.getChildren()) {
            html += getList(child);
        }
        html += "</li>";
        html += "</ul>";
        return html;
    }
}
