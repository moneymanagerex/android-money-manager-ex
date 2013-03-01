package com.money.manager.ex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.money.manager.ex.billing.IabHelper;
import com.money.manager.ex.billing.IabHelper.OnConsumeFinishedListener;
import com.money.manager.ex.billing.IabHelper.OnIabPurchaseFinishedListener;
import com.money.manager.ex.billing.IabHelper.OnIabSetupFinishedListener;
import com.money.manager.ex.billing.IabHelper.QueryInventoryFinishedListener;
import com.money.manager.ex.billing.IabResult;
import com.money.manager.ex.billing.Inventory;
import com.money.manager.ex.billing.Purchase;
import com.money.manager.ex.billing.SkuDetails;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.fragment.BaseFragmentActivity;

public class DonateActivity extends BaseFragmentActivity implements QueryInventoryFinishedListener, OnIabSetupFinishedListener, OnIabPurchaseFinishedListener,
		OnConsumeFinishedListener {
	
	private final int RC_REQUEST = 1;
	private final String PURCHASED_SKU = "DonateActivity:Purchased_Sku";
	private String purchasedSku = "";
	// Helper In-app Billing
	private IabHelper iabHelper;
	/**
	 * Product Names
	 */
	HashMap<String, String> skuNames = new HashMap<String, String>();
	/**
	 * List of valid SKUs
	 */
	ArrayList<String> skus = new ArrayList<String>();

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		Log.d(getClass().getSimpleName(), "onActivityResult(" + requestCode + "," + resultCode + "," + data + ")");
		if (!iabHelper.handleActivityResult(requestCode, resultCode, data))
			super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onConsumeFinished(Purchase purchase, IabResult result) {
		Log.d(getClass().getSimpleName(), "Consume Completed: " + result.getMessage());
		if (result.isSuccess()) {
			Toast.makeText(this, R.string.donate_thank_you, Toast.LENGTH_LONG).show();
			Core core = new Core(this);
			// update the info value
			core.setInfoValue(Core.INFO_SKU_ORDER_ID, purchase.getOrderId());
			finish();
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set up SKUs
		if (BuildConfig.DEBUG) {
			skus.add("android.test.purchased");
			skus.add("android.test.canceled");
			skus.add("android.test.refunded");
			skus.add("android.test.item_unavailable");
		}
		final String[] skuArray = getResources().getStringArray(R.array.donate_in_app_sku_array);
		skus.addAll(Arrays.asList(skuArray));
		// Set up the UI
		setContentView(R.layout.donate_activity);
		final Spinner inAppSpinner = (Spinner) findViewById(R.id.spinnerDonateInApp);
		final Button inAppButton = (Button) findViewById(R.id.buttonDonateInApp);
		inAppButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				final int selectedInAppAmount = inAppSpinner.getSelectedItemPosition();
				purchasedSku = skus.get(selectedInAppAmount);
				Log.d(DonateActivity.this.getClass().getSimpleName(), "Clicked " + purchasedSku);

				iabHelper.launchPurchaseFlow(DonateActivity.this, purchasedSku, RC_REQUEST, DonateActivity.this);
			}
		});
		// Disabilito il tasto fin che non è pronto
		inAppButton.setEnabled(false);
		// Start the In-App Billing process
		iabHelper = new IabHelper(this, new Core(this).getAppBase64());
		iabHelper.enableDebugLogging(BuildConfig.DEBUG);
		iabHelper.startSetup(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (iabHelper != null)
			iabHelper.dispose();
		iabHelper = null;
	}

	@Override
	public void onIabPurchaseFinished(IabResult result, Purchase info) {
		Log.d(getClass().getSimpleName(), "Purchase Completed: " + result.getMessage());
		if (result.isSuccess())
			iabHelper.consumeAsync(info, this);
	}

	@Override
	public void onIabSetupFinished(IabResult result) {
		Log.d(getClass().getSimpleName(), "Billing supported: " + result.getMessage());
		if (result.isSuccess() && iabHelper != null)
			iabHelper.queryInventoryAsync(true, skus, this);
	}

	@Override
	public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
		Log.d(getClass().getSimpleName(), "Inventory Returned: " + result.getMessage() + ": " + inventory);
		// If we failed to get the inventory, then leave the in-app billing UI hidden
		if (result.isFailure())
			return;
		// Make sure we've consumed any previous purchases
		final List<Purchase> purchases = inventory.getAllPurchases();

		if (!purchases.isEmpty())
			iabHelper.consumeAsync(purchases, null);

		final List<String> inAppName = new ArrayList<String>();
		
		for (int i = 0; i < skus.size(); i++) {
			final String currentSku = skus.get(i);
			final SkuDetails sku = inventory.getSkuDetails(currentSku);
			final Purchase purchase = inventory.getPurchase(currentSku);
			if (sku != null && purchase == null) {
				inventory.hasPurchase(currentSku);
				skuNames.put(currentSku, sku.getTitle());
				inAppName.add(sku.getDescription() + " (" + sku.getPrice() + ")");
			}
		}
		
		Spinner inAppSpinner = (Spinner) findViewById(R.id.spinnerDonateInApp);
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, inAppName);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		inAppSpinner.setAdapter(adapter);
		// enable button
		final Button inAppButton = (Button) findViewById(R.id.buttonDonateInApp);
		inAppButton.setEnabled(inAppName.size() > 0);
		// hide spinner if release version
		inAppSpinner.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
		// if has 1 item set button text
		if (!BuildConfig.DEBUG) {
			if (inAppName.size() == 1) {
				inAppButton.setText(inAppName.get(0));
			}
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		purchasedSku = savedInstanceState.containsKey(PURCHASED_SKU) ? savedInstanceState.getString(PURCHASED_SKU) : "";
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(PURCHASED_SKU, purchasedSku);
	}
}
