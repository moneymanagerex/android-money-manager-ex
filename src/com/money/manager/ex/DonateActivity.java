package com.money.manager.ex;

import java.util.ArrayList;
import java.util.List;

import net.robotmedia.billing.BillingController;
import net.robotmedia.billing.BillingRequest.ResponseCode;
import net.robotmedia.billing.helper.AbstractBillingObserver;
import net.robotmedia.billing.model.Transaction.PurchaseState;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.InAppBilling;
import com.money.manager.ex.fragment.BaseFragmentActivity;

public class DonateActivity extends BaseFragmentActivity  {
	
	private final String PURCHASED_SKU = "DonateActivity:Purchased_Sku";
	private String purchasedSku = "";
	// Helper In-app Billing
	private AbstractBillingObserver billingObserver;
	/**
	 * List of valid SKUs
	 */
	ArrayList<String> skus = new ArrayList<String>();

	public void onPurchaseStateChanged(String itemId, PurchaseState state){
		if(state == PurchaseState.PURCHASED){
			Toast.makeText(this, R.string.donate_thank_you, Toast.LENGTH_LONG).show();
			Core core = new Core(this);
			// update the info value
			core.setInfoValue(Core.INFO_SKU_ORDER_ID, itemId);
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
		// add SKU application
		skus.add("android.money.manager.ex.donations.small");
		// Set up the UI
		setContentView(R.layout.donate_activity);
		final Spinner inAppSpinner = (Spinner) findViewById(R.id.spinnerDonateInApp);
		final Button inAppButton = (Button) findViewById(R.id.buttonDonateInApp);
		inAppButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				final int selectedInAppAmount = inAppSpinner.getSelectedItemPosition();
				purchasedSku = skus.get(selectedInAppAmount);
				if (BuildConfig.DEBUG) Log.d(DonateActivity.this.getClass().getSimpleName(), "Clicked " + purchasedSku);

				BillingController.requestPurchase(DonateActivity.this, purchasedSku, true, null);
			}
		});
		// Disabilito il tasto fin che non è pronto
		inAppButton.setEnabled(false);
		// Start the In-App Billing process
		BillingController.setConfiguration(InAppBilling.getConfiguaration());
		billingObserver = new AbstractBillingObserver(this) {

			public void onBillingChecked(boolean supported) {
				DonateActivity.this.onBillingChecked(supported);
			}

			public void onPurchaseStateChanged(String itemId, PurchaseState state) {
				DonateActivity.this.onPurchaseStateChanged(itemId, state);
			}

			public void onRequestPurchaseResponse(String itemId, ResponseCode response) {
				DonateActivity.this.onRequestPurchaseResponse(itemId, response);
			}

			@Override
			public void onSubscriptionChecked(boolean supported) {	}

		};
		BillingController.registerObserver(billingObserver);
		BillingController.checkBillingSupported(getApplicationContext());
		// set enable return
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		BillingController.unregisterObserver(billingObserver);
		super.onDestroy();
	}
	
	public void onRequestPurchaseResponse(String itemId, ResponseCode response) {}

	public void onBillingChecked(boolean supported) {
		if(supported){
			final List<String> inAppName = new ArrayList<String>();
			
			for (int i = 0; i < skus.size(); i++) {
				inAppName.add(skus.get(i));
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
			/*if (!BuildConfig.DEBUG) {
				if (inAppName.size() == 1) {
					inAppButton.setText(inAppName.get(0));
				}
			}*/
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
