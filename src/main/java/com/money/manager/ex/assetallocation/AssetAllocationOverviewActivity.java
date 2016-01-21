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
        String html = "<html><body style='background: lightgray; padding: 0;'>";

        // get content
        html += getList(allocation);

        html += "</body></html>";
        return html;
    }

    private void displayOverview(String html) {
        WebView overview = (WebView) this.findViewById(R.id.overviewWebView);
        overview.loadData(html, "text/html", null);
    }

    private String getList(AssetClass allocation) {
        String format = "%,.2f";
        String html = "";

        html += "<ul style='padding-left: 20px;'>";
        // Name
        html += "<li>" + allocation.getName() + ", ";
        // Allocation
        html +=
            String.format(format, allocation.getAllocation().toDouble()) + "/";
            if (allocation.getDifference().toDouble() > 0) {
                html += "<span style='color: green; font-weight: bold;'>";
            } else {
                html += "<span style='color: darkred; font-weight: bold;'>";
            }
        // current allocation
        html +=
            String.format(format, allocation.getCurrentAllocation().toDouble()) +
            "</span>, ";
        // diff %
        if (allocation.getDiffAsPercentOfSet().toDouble() >= 0) {
            html += "<span style='color: green;'>";
        } else {
            html += "<span style='color: darkred;'>";
        }
        html += allocation.getDiffAsPercentOfSet() +
            " %</span>";
        html += "<br/>";
        // Value
        html += String.format(format, allocation.getValue().toDouble()) + "/" +
            String.format(format, allocation.getCurrentValue().toDouble()) +
            ", ";
        // difference amount
        if (allocation.getDifference().truncate(2).toDouble() >= 0) {
            html += "<span style='color: green;'>";
        } else {
            html += "<span style='color: darkred;'>";
        }
        html +=
            String.format(format, allocation.getDifference().toDouble());
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
