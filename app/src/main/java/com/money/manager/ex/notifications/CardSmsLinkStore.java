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

import android.content.Context;

import com.money.manager.ex.account.AccountStatuses;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.servicelayer.InfoService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import timber.log.Timber;

/**
 * Card (issuer + last 4 digits) -> account links chosen by the user.
 * Stored in INFOTABLE_V1 so they travel with the database file.
 */
public class CardSmsLinkStore {

    private static final String INFO_KEY = "SMS_CARD_ACCOUNT_LINKS";

    private final Context mContext;

    public CardSmsLinkStore(Context context) {
        mContext = context.getApplicationContext();
    }

    public static String key(String issuer, String last4) {
        return issuer + ":" + last4;
    }

    /**
     * @return linked account id, or null if not linked or the account is gone / closed.
     */
    public Long getAccountId(String issuer, String last4) {
        Long accountId = getAll().get(key(issuer, last4));
        if (accountId == null) return null;

        Account account = new AccountRepository(mContext).load(accountId);
        if (account == null || !AccountStatuses.OPEN.title.equals(account.getStatus())) {
            return null;
        }
        return accountId;
    }

    public void link(String issuer, String last4, long accountId) {
        Map<String, Long> links = getAll();
        links.put(key(issuer, last4), accountId);
        save(links);
    }

    public void unlink(String issuer, String last4) {
        Map<String, Long> links = getAll();
        links.remove(key(issuer, last4));
        save(links);
    }

    /** Keyed by "ISSUER:last4". */
    public Map<String, Long> getAll() {
        Map<String, Long> result = new TreeMap<>();
        String value = new InfoService(mContext).getInfoValue(INFO_KEY);
        if (value == null || value.isEmpty()) return result;

        try {
            JSONObject json = new JSONObject(value);
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String k = keys.next();
                result.put(k, json.getLong(k));
            }
        } catch (JSONException e) {
            Timber.e(e, "reading card sms links");
        }
        return result;
    }

    private void save(Map<String, Long> links) {
        JSONObject json = new JSONObject();
        try {
            for (Map.Entry<String, Long> entry : links.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }
        } catch (JSONException e) {
            Timber.e(e, "writing card sms links");
            return;
        }
        new InfoService(mContext).setInfoValue(INFO_KEY, json.toString());
    }
}
