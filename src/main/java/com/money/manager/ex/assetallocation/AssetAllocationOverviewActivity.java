package com.money.manager.ex.assetallocation;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;
import android.util.Xml;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;
import com.money.manager.ex.settings.BehaviourSettings;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import info.javaperformance.money.Money;

public class AssetAllocationOverviewActivity
    extends BaseFragmentActivity {

    public static final String VALUE_FORMAT = "%,.2f";

    private Money differenceThreshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_allocation_overview);

        // load difference threshold
        BehaviourSettings settings = new BehaviourSettings(this);
        this.differenceThreshold = settings.getAssetAllocationDifferenceThreshold();

        // get asset allocation
        AssetAllocationService service = new AssetAllocationService(this);
        AssetClass allocation = service.loadAssetAllocation();

        // create a HTML display.
        String html = createHtml(allocation);
        displayOverview(html);
    }

    private String createHtml(AssetClass allocation) {
        String html = "<html>";
        // Styles
        html += "<head>" +
                "<style>" +
                "body { background: lightgray; padding: 0; } " +
                "ul li { border-bottom: 1px solid black;  } " +
                "</style>" +
                "</head>";
        html += "<body>";

        html += getSummaryRow(allocation);

        html += getList(allocation.getChildren());

        html += "</body></html>";
        return html;
    }

    private void displayOverview(String html) {
        WebView webView = (WebView) this.findViewById(R.id.overviewWebView);

        // enable Unicode
        WebSettings settings = webView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            String base64 = Base64.encodeToString(html.getBytes(), Base64.DEFAULT);
            webView.loadData(base64, "text/html; charset=utf-8", "base64");
        } else {
            String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
            webView.loadData(header + html, "text/html; charset=UTF-8", null);
        }

//        webView.loadData(html, "text/html", "UTF-8");
    }

    /**
     * Create a list with child elements.
     * @param children Asset Allocation/Class
     * @return HTML list (ul) of the child Asset Classes with information.
     */
    private String getList(List<AssetClass> children) {
        String html = "";
        if (children.size() == 0) return html;

        html += "<ul style='padding-left: 20px;'>";

        for(AssetClass child : children) {
            html += getAssetRow(child);
        }

        html += "</ul>";
        return html;
    }

    private String getSummaryRow(AssetClass allocation) {
        String html = "";
        CurrencyService currencyService = new CurrencyService(this);
        FormatUtilities formatter = new FormatUtilities(this);

        html += "<p>" +
            allocation.getName() + ", " +
//            currencyService.getBaseCurrencyCode() + " " +
//            String.format(VALUE_FORMAT, allocation.getCurrentValue().toDouble()) +
            formatter.getValueFormatted(allocation.getCurrentValue(), currencyService.getBaseCurrencyId()) +
            "</p>";
        return html;
    }

    private String getAssetRow(AssetClass allocation) {
        String color = "black";
        double diffPercent = allocation.getDiffAsPercentOfSet().toDouble();
        if (diffPercent >= this.differenceThreshold.toDouble()) {
            color = "green";
        }
        if (diffPercent <= this.differenceThreshold.multiply(-1).toDouble()) {
            color = "darkred";
        }

        // style='list-style-position: inside;'
        String html = "<li>";

        // Name
        html += allocation.getName();

        html += " &#183; ";

        // diff %
        html += "<span style='color: " + color + ";'>";
        html += allocation.getDiffAsPercentOfSet();
        html += "%</span>";

        html += " &#183; ";

        // difference amount
//        color = allocation.getDifference().truncate(2).toDouble() >= this.differenceThreshold.toDouble()
//                ? "green" : "darkred";
        html += "<span style='color: " + color + ";'>";
        html += String.format(VALUE_FORMAT, allocation.getDifference().toDouble());
        html += "</span>";

        html += "<br/>";

        // Allocation
        html += String.format(VALUE_FORMAT, allocation.getAllocation().toDouble()) + "/";
//        color = allocation.getDifference().toDouble() > this.differenceThreshold.toDouble()
//                ? "green" : "darkred";

        // current allocation
        html += "<span style='color: " + color + "; font-weight: bold;'>";
        html += String.format(VALUE_FORMAT, allocation.getCurrentAllocation().toDouble()) +
                "</span>";

        html += " &#183; ";

        // Value
        html += String.format(VALUE_FORMAT, allocation.getValue().toDouble()) + "/" +
            String.format(VALUE_FORMAT, allocation.getCurrentValue().toDouble());

        // Child asset classes
        html += getList(allocation.getChildren());

        html += "</li>";

        return html;
    }
}
