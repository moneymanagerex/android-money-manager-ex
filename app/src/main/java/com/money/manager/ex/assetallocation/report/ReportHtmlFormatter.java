/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.assetallocation.report;

import com.money.manager.ex.domainmodel.AssetClass;

import java.util.Locale;

/**
 * Formats the Asset Allocation for the HTML report.
 */
public class ReportHtmlFormatter {
    public static final String VALUE_FORMAT = "%,.2f";

    public ReportHtmlFormatter(AssetClass allocation, String color) {
        this.allocation = allocation;
        this.color = color;
    }

    private AssetClass allocation;
    private String color;

    public String getName() {
        return allocation.getName();
    }

    public String getDiffPerc() {
        String html = String.format("<span style='color: %s;'>", color);
        html += allocation.getDiffAsPercentOfSet();
        html += "%</span>";

        return html;
    }

    public String getDiffAmount() {
        String html = String.format("<span style='color: %s;'>", color);
        html += String.format(Locale.UK, VALUE_FORMAT, allocation.getDifference().toDouble());
        html += "</span>";
        return html;
    }

    public String getAllocation() {
        return String.format(Locale.UK, VALUE_FORMAT, allocation.getAllocation().toDouble()) + "/";
    }

    public String getCurrentAllocation() {
        String html = String.format("<span style='color: %s; font-weight: bold;'>", color);
        html += String.format(Locale.UK, VALUE_FORMAT, allocation.getCurrentAllocation().toDouble()) +
                "</span>";
        return html;
    }

    public String getValue() {
        return String.format(Locale.UK, VALUE_FORMAT + "/" + VALUE_FORMAT,
                allocation.getValue().toDouble(), allocation.getCurrentValue().toDouble());
    }
}
