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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Only SMS from these senders are read by the SMS transaction feature.
 * Pure Java, no Android dependencies.
 */
public class SmsSenderAllowlist {

    /** Samsung Card: domestic, overseas. */
    public static final String DEFAULT_SENDERS = "15888900, 0220008100";

    /**
     * Phone numbers: strip separators and convert +82 to the domestic 0 prefix.
     * Alphanumeric sender IDs (e.g. AT-SIBSMS) are only trimmed and lowercased.
     */
    public static String normalize(String sender) {
        if (sender == null) return "";

        String s = sender.trim().toLowerCase();
        String digits = s.replaceAll("[\\s\\-()]", "");

        if (!digits.matches("\\+?\\d+")) {
            return s;
        }

        if (digits.startsWith("+")) digits = digits.substring(1);
        if (digits.startsWith("82") && digits.length() > 9) {
            digits = "0" + digits.substring(2);
        }
        return digits;
    }

    /** Parses a comma / newline separated list into normalized entries. */
    public static Set<String> parse(String list) {
        Set<String> result = new LinkedHashSet<>();
        if (list == null) return result;

        for (String item : list.split("[,;\\n]")) {
            String n = normalize(item);
            if (!n.isEmpty()) result.add(n);
        }
        return result;
    }

    public static String format(Set<String> senders) {
        return String.join(", ", senders);
    }

    public static boolean isAllowed(String sender, Set<String> allowed) {
        return sender != null && allowed.contains(normalize(sender));
    }
}
