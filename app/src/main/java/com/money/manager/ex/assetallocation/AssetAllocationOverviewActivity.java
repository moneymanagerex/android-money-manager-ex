package com.money.manager.ex.assetallocation;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.file.TextFileExport;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;
import com.money.manager.ex.settings.InvestmentSettings;
import com.money.manager.ex.utils.MmexFileUtils;
import com.shamanland.fonticon.FontIconDrawable;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import info.javaperformance.money.Money;

/**
 * Asset Allocation report.
 */
public class AssetAllocationOverviewActivity
    extends BaseFragmentActivity {

    public static final String VALUE_FORMAT = "%,.2f";

    private Money differenceThreshold;
    private CurrencyService mCurrencyService;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_allocation_overview);

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

        MenuHelper menuHelper = new MenuHelper(this);
        menuHelper.addToContextMenu(ContextMenuIds.Print, menu);
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
        ExceptionHandler handler = new ExceptionHandler(this);

        try {
            export.clearCache();
            outFile = export.createExportFile("asset_allocation.html");
        } catch (IOException e) {
            handler.handle(e, "creating temp file");
        }
        if (outFile == null) {
            handler.showMessage("File could not be created.");
            return;
        }

        final File finalOutFile = outFile;
        webView.saveWebArchive(outFile.getAbsolutePath(), false, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (!StringUtils.isEmpty(value)) {
                    // offer export
                    export.export(finalOutFile, getString(R.string.asset_allocation));
                }
            }
        });
    }

    private String getAssetClassNameHtml(AssetClass allocation) {
        return allocation.getName();
    }

    private String getAssetClassDiffPercHtml(AssetClass allocation, String color) {
        String html = String.format("<span style='color: %s;'>", color);
        html += allocation.getDiffAsPercentOfSet();
        html += "%</span>";

        return html;
    }

    private String getAssetClassDiffAmountHtml(AssetClass allocation, String color) {
        String html = String.format("<span style='color: %s;'>", color);
        html += String.format(Locale.UK, VALUE_FORMAT, allocation.getDifference().toDouble());
        html += "</span>";
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

//        html += getAssetValues(allocation, color);
        html += getAssetValuesAsList(allocation, color);

        // Child asset classes
        html += getList(allocation.getChildren());

        html += "</li>";

        return html;
    }

    private String getAssetValues(AssetClass allocation, String color) {
        String html = "";

        // Name
        html += getAssetClassNameHtml(allocation);

        html += " &#183; ";

        // diff %
        html += getAssetClassDiffPercHtml(allocation, color);

        html += " &#183; ";

        // difference amount
        html += getAssetClassDiffAmountHtml(allocation, color);

        html += "<br/>";

        // Allocation
        html += String.format(VALUE_FORMAT, allocation.getAllocation().toDouble()) + "/";

        // current allocation
        html += String.format("<span style='color: %s; font-weight: bold;'>", color);
        html += String.format(VALUE_FORMAT, allocation.getCurrentAllocation().toDouble()) +
                "</span>";

        html += " &#183; ";

        // Value
        html += String.format(VALUE_FORMAT + "/" + VALUE_FORMAT,
                allocation.getValue().toDouble(), allocation.getCurrentValue().toDouble());

        return html;
    }

    private String getAssetValuesAsList(AssetClass allocation, String color) {
        String html = "<ul class='inline'>";

        html += "<li class='inline'>";
        html += getAssetClassNameHtml(allocation);
        html += "</li>";

        html += "<li class='inline'>";
        html += getAssetClassDiffPercHtml(allocation, color);
        html += "</li>";

        // difference amount
        html += "<li class='inline'>";
        html += getAssetClassDiffAmountHtml(allocation, color);
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
        String html = "";
        FormatUtilities formatter = new FormatUtilities(this);

        html += "<p>" +
            allocation.getName() + ", " +
//            currencyService.getBaseCurrencyCode() + " " +
//            String.format(VALUE_FORMAT, allocation.getCurrentValue().toDouble()) +
            formatter.getValueFormatted(allocation.getCurrentValue(), getCurrencyService().getBaseCurrencyId()) +
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
            ExceptionHandler handler = new ExceptionHandler(this);
            handler.showMessage(R.string.min_19);
            return;
        }

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter();

        // Create a print job with name and adapter instance
        String jobName = getString(R.string.app_name) + " Document";
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());
    }

    private boolean showContextMenu() {
        return webView.showContextMenu();
    }
}
