/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.money.manager.ex.assetallocation;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.money.manager.ex.R;
import com.money.manager.ex.assetallocation.report.ReportHtmlFormatter;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.file.TextFileExport;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;
import com.money.manager.ex.settings.InvestmentSettings;

import java.io.File;
import java.io.IOException;
import java.util.List;

import info.javaperformance.money.Money;
import timber.log.Timber;

/**
 * Asset Allocation report.
 */
public class AssetAllocationReportActivity
    extends MmxBaseFragmentActivity {

    private Money differenceThreshold;
    private CurrencyService mCurrencyService;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_allocation_report);

        // load difference threshold
        InvestmentSettings settings = new InvestmentSettings(this);
        this.differenceThreshold = settings.getAssetAllocationDifferenceThreshold();

        // get asset allocation
        AssetAllocationService service = new AssetAllocationService(this);
        AssetClass allocation = service.loadAssetAllocation();

        // create a HTML display.
        String html = createHtml(allocation);
        displayOverview(html);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle(R.string.asset_allocation);

        MenuHelper menuHelper = new MenuHelper(this, menu);
        menuHelper.addToContextMenu(ContextMenuIds.Print);
//        menuHelper.addToContextMenu(ContextMenuIds.SaveAsHtml, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        ContextMenuIds menuId = ContextMenuIds.get(itemId);

        switch (menuId) {
            case Print:
                // print to pdf
                createWebPrintJob(this.webView);
                return true;
            case SaveAsHtml:
                exportHtml();
                return true;
            default:
                return false;
        }
    }

    private String createHtml(AssetClass allocation) {
        if (allocation == null) {
            return "N/A";
        }

        String html = "<html>";
        // Styles
        html += "<head>" +
                "   <style>" +
                "       body { background: lightgray; padding: 0; } " +
                "       ul li { border-bottom: 1px solid black;  } " +
                "       li.inline { display: inline; list-style-type: none; padding-right: 10px; } " +
                "   </style>" +
                "</head>";
        html += "<body>";

        html += getSummaryRow(allocation);

        html += getList(allocation.getChildren());

        html += "</body></html>";
        return html;
    }

    private void displayOverview(String html) {
        webView = (WebView) this.findViewById(R.id.overviewWebView);

        // gesture handler
        handleGestures(webView);

        // context menu
        registerForContextMenu(webView);

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

    private void exportHtml() {
        File outFile = null;
        final TextFileExport export = new TextFileExport(this);

        try {
            export.clearCache();
            outFile = export.createExportFile("asset_allocation.html");
        } catch (IOException e) {
            Timber.e(e, "creating temp file");
        }
        if (outFile == null) {
            new UIHelper(this).showToast("File could not be created.");
            return;
        }

        final File finalOutFile = outFile;
        webView.saveWebArchive(outFile.getAbsolutePath(), false, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (!TextUtils.isEmpty(value)) {
                    // offer export
                    export.export(finalOutFile, getString(R.string.asset_allocation));
                }
            }
        });
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

        html += getAssetValues(allocation, color);
//        html += getAssetValuesAsList(allocation, color);

        // Child asset classes
        html += getList(allocation.getChildren());

        html += "</li>";

        return html;
    }

    private String getAssetValues(AssetClass allocation, String color) {
        String html = "";

        ReportHtmlFormatter formatter = new ReportHtmlFormatter(allocation, color);

        // Name
        html += formatter.getName();

        html += " &#183; ";

        // diff %
        html += formatter.getDiffPerc();

        html += " &#183; ";

        // difference amount
        html += formatter.getDiffAmount();

        html += "<br/>";

        // Allocation
        html += formatter.getAllocation();

        // current allocation
        html += formatter.getCurrentAllocation();

        html += " &#183; ";

        // Value
        html += formatter.getValue();

        return html;
    }

    private String getAssetValuesAsList(AssetClass allocation, String color) {
        ReportHtmlFormatter formatter = new ReportHtmlFormatter(allocation, color);

        String html = "<ul class='inline'>";

        html += "<li class='inline'>";
        html += formatter.getName();
        html += "</li>";

        html += "<li class='inline'>";
        html += formatter.getDiffPerc();
        html += "</li>";

        // difference amount
        html += "<li class='inline'>";
        html += formatter.getDiffAmount();
        html += "</li>";

        html += "<li class='inline'>";
        html += formatter.getAllocation();
        html += "</li>";

        html += "<li class='inline'>";
        html += formatter.getCurrentAllocation();
        html += "</li>";

        html += "<li class='inline'>";
        html += formatter.getValue();
        html += "</li>";

        html += "</ul>";

        return html;
    }

    private CurrencyService getCurrencyService() {
        if (mCurrencyService == null) {
            mCurrencyService = new CurrencyService(this);
        }
        return mCurrencyService;
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
        if (allocation == null) return "n/a";

        String html = "";
        FormatUtilities formatter = new FormatUtilities(this);

        html += "<p>" +
            allocation.getName() + ", " +
//            currencyService.getBaseCurrencyCode() + " " +
//            String.format(VALUE_FORMAT, allocation.getCurrentValue().toDouble()) +
            formatter.format(allocation.getCurrentValue(), getCurrencyService().getBaseCurrencyId()) +
            "</p>";
        return html;
    }

    private void handleGestures(WebView webView) {
        GestureDetector.OnGestureListener listener = new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                // show context menu
                //Log.d("test", "long-press");
                showContextMenu();
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        };

        final GestureDetector gd = new GestureDetector(this, listener);

        GestureDetector.OnDoubleTapListener doubleTapListener = new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // show context menu
//                return false;
                return showContextMenu();
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        };
        gd.setOnDoubleTapListener(doubleTapListener);

        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //return false;
                return gd.onTouchEvent(event);
            }
        };
        // attach to the web view
        webView.setOnTouchListener(touchListener);
    }

    private void createWebPrintJob(WebView webView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            new UIHelper(this).showToast(R.string.min_19);
            return;
        } else {
            // Get a PrintManager instance
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

            // Get a print adapter instance
            PrintDocumentAdapter printAdapter;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                printAdapter = webView.createPrintDocumentAdapter(getString(R.string.asset_allocation));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                printAdapter = webView.createPrintDocumentAdapter();
            } else {
                // to satisfy lint.
                return;
            }

            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + " Document";
            // PrintJob printJob =
            printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
        }
    }

    private boolean showContextMenu() {
        return webView.showContextMenu();
    }
}
