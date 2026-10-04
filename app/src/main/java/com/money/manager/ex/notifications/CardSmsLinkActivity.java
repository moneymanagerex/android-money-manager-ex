/*
 * Copyright (C) 2012-2025 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.notifications;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.money.manager.ex.R;
import com.money.manager.ex.account.AccountListActivity;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.datalayer.AccountRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Lets the user link a card (issuer + last 4 digits) to an account.
 * Opened from settings (list of links) or from the "card not linked" notification
 * (goes straight to the account picker, then records the pending SMS).
 */
public class CardSmsLinkActivity
        extends MmxBaseFragmentActivity {

    private static final int REQUEST_PICK_ACCOUNT = 1;
    private static final String STATE_ISSUER = "issuer";
    private static final String STATE_LAST4 = "last4";

    private CardSmsLinkStore mStore;
    private final List<String> mKeys = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;

    // card currently being linked
    private String mIssuer;
    private String mLast4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_sms_links);
        setDisplayHomeAsUpEnabled(true);

        mStore = new CardSmsLinkStore(this);

        ListView list = findViewById(R.id.card_sms_links_list);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        list.setAdapter(mAdapter);
        list.setOnItemClickListener((parent, view, position, id) -> {
            String[] parts = mKeys.get(position).split(":", 2);
            pickAccount(parts[0], parts[1]);
        });
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmUnlink(mKeys.get(position));
            return true;
        });

        findViewById(R.id.card_sms_links_add).setOnClickListener(v -> showAddDialog());

        if (savedInstanceState != null) {
            mIssuer = savedInstanceState.getString(STATE_ISSUER);
            mLast4 = savedInstanceState.getString(STATE_LAST4);
        } else if (hasPendingSms()) {
            pickAccount(getIntent().getStringExtra(SamsungCardSmsProcessor.EXTRA_ISSUER),
                    getIntent().getStringExtra(SamsungCardSmsProcessor.EXTRA_LAST4));
        }

        refreshList();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_ISSUER, mIssuer);
        outState.putString(STATE_LAST4, mLast4);
    }

    @Override
    protected void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_PICK_ACCOUNT) return;

        if (resultCode != Activity.RESULT_OK || data == null || mLast4 == null) {
            if (hasPendingSms()) finish();
            return;
        }

        long accountId = data.getLongExtra(AccountListActivity.INTENT_RESULT_ACCOUNTID, -1);
        if (accountId <= 0) return;

        mStore.link(mIssuer, mLast4, accountId);
        Toast.makeText(this, getString(R.string.card_sms_linked, mLast4,
                data.getStringExtra(AccountListActivity.INTENT_RESULT_ACCOUNTNAME)), Toast.LENGTH_SHORT).show();

        if (hasPendingSms()) {
            recordPendingSms();
            finish();
            return;
        }
        refreshList();
    }

    private boolean hasPendingSms() {
        return getIntent() != null && getIntent().hasExtra(SamsungCardSmsProcessor.EXTRA_SMS_BODY);
    }

    private void recordPendingSms() {
        String body = getIntent().getStringExtra(SamsungCardSmsProcessor.EXTRA_SMS_BODY);
        String sender = getIntent().getStringExtra(SamsungCardSmsProcessor.EXTRA_SMS_SENDER);

        SamsungCardSmsParser.Result card = SamsungCardSmsParser.parse(body);
        if (card != null) {
            new SamsungCardSmsProcessor(this).process(card, body, sender);
        }
    }

    private void pickAccount(String issuer, String last4) {
        mIssuer = issuer;
        mLast4 = last4;

        Intent intent = new Intent(this, AccountListActivity.class);
        intent.setAction(Intent.ACTION_PICK);
        launchActivityForResult(intent, REQUEST_PICK_ACCOUNT);
    }

    private void showAddDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        input.setHint(R.string.card_sms_links_last4_hint);

        new AlertDialog.Builder(this)
                .setTitle(R.string.card_sms_links_add)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String last4 = input.getText().toString().trim();
                    if (!last4.matches("\\d{4}")) {
                        Toast.makeText(this, R.string.card_sms_links_last4_invalid, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pickAccount(SamsungCardSmsParser.ISSUER, last4);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void confirmUnlink(String key) {
        String[] parts = key.split(":", 2);
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.card_sms_links_unlink_confirm, parts[1]))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    mStore.unlink(parts[0], parts[1]);
                    refreshList();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void refreshList() {
        AccountRepository accounts = new AccountRepository(this);

        mKeys.clear();
        List<String> rows = new ArrayList<>();
        for (Map.Entry<String, Long> entry : mStore.getAll().entrySet()) {
            String[] parts = entry.getKey().split(":", 2);
            if (parts.length < 2) continue;

            String accountName = accounts.loadName(entry.getValue());
            if (accountName == null || accountName.isEmpty()) {
                accountName = getString(R.string.card_sms_links_account_missing);
            }
            mKeys.add(entry.getKey());
            rows.add(getString(R.string.card_sms_links_row, issuerName(parts[0]), parts[1], accountName));
        }

        mAdapter.clear();
        mAdapter.addAll(rows);
        mAdapter.notifyDataSetChanged();

        findViewById(R.id.card_sms_links_empty).setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        findViewById(R.id.card_sms_links_list).setVisibility(rows.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private String issuerName(String issuer) {
        return SamsungCardSmsParser.ISSUER.equals(issuer)
                ? getString(R.string.card_sms_issuer_samsung)
                : issuer;
    }
}
