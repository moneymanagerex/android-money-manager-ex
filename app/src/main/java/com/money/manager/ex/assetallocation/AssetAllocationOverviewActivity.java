package com.money.manager.ex.assetallocation;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;
import com.money.manager.ex.settings.InvestmentSettings;
import com.shamanland.fonticon.FontIconDrawable;

import java.util.List;

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
//                .setIcon(FontIconDrawable.inflate(this, R.xml.ic_print));
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
        html += String.format("<span style='color: %s;'>", color);
        html += allocation.getDiffAsPercentOfSet();
        html += "%</span>";

        html += " &#183; ";

        // difference amount
//        color = allocation.getDifference().truncate(2).toDouble() >= this.differenceThreshold.toDouble()
//                ? "green" : "darkred";
        html += String.format("<span style='color: %s;'>", color);
        html += String.format(VALUE_FORMAT, allocation.getDifference().toDouble());
        html += "</span>";

        html += "<br/>";

        // Allocation
        html += String.format(VALUE_FORMAT, allocation.getAllocation().toDouble()) + "/";
//        color = allocation.getDifference().toDouble() > this.differenceThreshold.toDouble()
//                ? "green" : "darkred";

        // current allocation
        html += String.format("<span style='color: %s; font-weight: bold;'>", color);
        html += String.format(VALUE_FORMAT, allocation.getCurrentAllocation().toDouble()) +
            "</span>";

        html += " &#183; ";

        // Value
        html += String.format(VALUE_FORMAT + "/" + VALUE_FORMAT,
            allocation.getValue().toDouble(), allocation.getCurrentValue().toDouble());

        // Child asset classes
        html += getList(allocation.getChildren());

        html += "</li>";

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
