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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Samsung Card (Korea) approval / cancellation SMS.
 * Pure Java, no Android dependencies.
 *
 * Examples:
 *   삼성5185승인 이*윤 / 7,100원 일시불 / 10/04 13:42 투썸플레이스위례 / 누적1,538,206원
 *   삼성5185취소 이*윤 / -2,999,000원 03개월 / 06/26 10:01 삼성전자(주) / 누적2,804,028원
 *   [Web발신] 삼성5185해외승인 이*윤 / USD 155.88 / 08/31 23:46 PADDLE.NET*FEEDLY
 *   [Web발신] 삼성5185해외취소 이*윤 / USD -0.99 / 10/01 07:14 DISCORD*TEMPORARYAUTH
 */
public class SamsungCardSmsParser {

    public static final String ISSUER = "SAMSUNG";
    public static final String DOMESTIC_CURRENCY = "KRW";

    // Fields may be separated by spaces/newlines, or by nothing if line breaks were lost.
    private static final Pattern PATTERN = Pattern.compile(
            "삼성(\\d{4})(해외)?(승인|취소)\\s*" +                       // 1 card, 2 overseas, 3 type
            "\\S*\\*\\S*?\\s*" +                                         // masked name, e.g. 이*윤
            "(?:([A-Z]{3})\\s*(-?[\\d,]+(?:\\.\\d{1,2})?)|(-?[\\d,]+)원)\\s*" + // 4 currency, 5 foreign amt | 6 KRW amt
            "(일시불|(\\d{1,2})개월)?\\s*" +                              // 7 payment, 8 installment months
            "(\\d{2})/(\\d{2})\\s*(\\d{2}):(\\d{2})\\s*" +               // 9 month, 10 day, 11 hour, 12 minute
            "(.+?)" +                                                    // 13 merchant
            "(?:\\s*누적-?[\\d,]+원)?$");

    public static class Result {
        public String issuer = ISSUER;
        public String cardLast4;
        public boolean overseas;
        public boolean cancel;
        public String currencyCode;
        /** Absolute amount. */
        public BigDecimal amount;
        /** 1 for 일시불 or when absent. */
        public int installmentMonths = 1;
        public int month;
        public int day;
        public int hour;
        public int minute;
        public String merchant;
    }

    public static Result parse(String body) {
        if (body == null) return null;

        String text = body.replace("[Web발신]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        Matcher m = PATTERN.matcher(text);
        if (!m.find()) return null;

        try {
            Result r = new Result();
            r.cardLast4 = m.group(1);
            r.overseas = m.group(2) != null;
            r.cancel = "취소".equals(m.group(3));

            String amount;
            if (m.group(4) != null) {
                r.currencyCode = m.group(4);
                amount = m.group(5);
            } else {
                r.currencyCode = DOMESTIC_CURRENCY;
                amount = m.group(6);
            }
            r.amount = new BigDecimal(amount.replace(",", "")).abs();

            if (m.group(8) != null) {
                r.installmentMonths = Math.max(1, Integer.parseInt(m.group(8)));
            }

            r.month = Integer.parseInt(m.group(9));
            r.day = Integer.parseInt(m.group(10));
            r.hour = Integer.parseInt(m.group(11));
            r.minute = Integer.parseInt(m.group(12));
            r.merchant = m.group(13).trim();

            if (r.month < 1 || r.month > 12 || r.day < 1 || r.day > 31
                    || r.hour > 23 || r.minute > 59 || r.merchant.isEmpty()) {
                return null;
            }
            return r;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Splits the total into equal monthly installments. The remainder goes to the first one.
     */
    public static List<BigDecimal> splitInstallments(BigDecimal total, int months, int scale) {
        List<BigDecimal> result = new ArrayList<>();
        if (months <= 1) {
            result.add(total);
            return result;
        }

        BigDecimal each = total.divide(BigDecimal.valueOf(months), scale, RoundingMode.DOWN);
        BigDecimal first = total.subtract(each.multiply(BigDecimal.valueOf(months - 1)));

        result.add(first);
        for (int i = 1; i < months; i++) {
            result.add(each);
        }
        return result;
    }

    /**
     * The SMS has no year: use the current one, or the previous one if the date would be in the future.
     */
    public static Date resolveDate(Result r, Date now) {
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(now);

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(nowCal.get(Calendar.YEAR), r.month - 1, r.day, r.hour, r.minute, 0);

        // allow a day of clock skew before assuming the SMS belongs to last year
        nowCal.add(Calendar.DATE, 1);
        if (cal.after(nowCal)) {
            cal.add(Calendar.YEAR, -1);
        }
        return cal.getTime();
    }
}
