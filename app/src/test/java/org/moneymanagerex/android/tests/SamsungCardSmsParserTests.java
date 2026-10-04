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

package org.moneymanagerex.android.tests;

import com.money.manager.ex.notifications.SamsungCardSmsParser;
import com.money.manager.ex.notifications.SmsSenderAllowlist;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Samsung Card (Korea) SMS parsing and SMS sender allowlist.
 */
public class SamsungCardSmsParserTests {

    @Test
    public void domesticApproval() {
        SamsungCardSmsParser.Result r = parse("삼성5185승인 이*윤\n7,100원 일시불\n10/04 13:42 투썸플레이스위례\n누적1,538,206원");

        assertEquals("5185", r.cardLast4);
        assertFalse(r.overseas);
        assertFalse(r.cancel);
        assertEquals("KRW", r.currencyCode);
        assertEquals(new BigDecimal("7100"), r.amount);
        assertEquals(1, r.installmentMonths);
        assertDateTime(r, 10, 4, 13, 42);
        assertEquals("투썸플레이스위례", r.merchant);
    }

    @Test
    public void domesticInstallmentApproval() {
        SamsungCardSmsParser.Result r = parse("삼성5185승인 이*윤\n2,520,000원 03개월\n06/09 21:40 삼성전자(주)\n누적4,077,275원");

        assertFalse(r.cancel);
        assertEquals(new BigDecimal("2520000"), r.amount);
        assertEquals(3, r.installmentMonths);
        assertDateTime(r, 6, 9, 21, 40);
        assertEquals("삼성전자(주)", r.merchant);
    }

    @Test
    public void domesticCancel() {
        SamsungCardSmsParser.Result r = parse("삼성5185취소 이*윤\n-2,999,000원 03개월\n06/26 10:01 삼성전자(주)\n누적2,804,028원");

        assertTrue(r.cancel);
        assertFalse(r.overseas);
        assertEquals(new BigDecimal("2999000"), r.amount);
        assertEquals(3, r.installmentMonths);
        assertDateTime(r, 6, 26, 10, 1);
        assertEquals("삼성전자(주)", r.merchant);
    }

    @Test
    public void overseasApproval() {
        SamsungCardSmsParser.Result r = parse("[Web발신]\n삼성5185해외승인 이*윤\nUSD 155.88\n08/31 23:46 PADDLE.NET*FEEDLY");

        assertTrue(r.overseas);
        assertFalse(r.cancel);
        assertEquals("USD", r.currencyCode);
        assertEquals(new BigDecimal("155.88"), r.amount);
        assertEquals(1, r.installmentMonths);
        assertDateTime(r, 8, 31, 23, 46);
        assertEquals("PADDLE.NET*FEEDLY", r.merchant);
    }

    @Test
    public void overseasCancel() {
        SamsungCardSmsParser.Result r = parse("[Web발신]\n삼성5185해외취소 이*윤\nUSD -0.99\n10/01 07:14 DISCORD*TEMPORARYAUTH");

        assertTrue(r.overseas);
        assertTrue(r.cancel);
        assertEquals(new BigDecimal("0.99"), r.amount);
        assertDateTime(r, 10, 1, 7, 14);
        assertEquals("DISCORD*TEMPORARYAUTH", r.merchant);
    }

    @Test
    public void overseasCancelWithoutLineBreaks() {
        SamsungCardSmsParser.Result r = parse("[Web발신]삼성5185해외취소 이*윤USD -0.9910/01 07:14 DISCORD*TEMPORARYAUTH");

        assertTrue(r.cancel);
        assertEquals("USD", r.currencyCode);
        assertEquals(new BigDecimal("0.99"), r.amount);
        assertDateTime(r, 10, 1, 7, 14);
        assertEquals("DISCORD*TEMPORARYAUTH", r.merchant);
    }

    @Test
    public void domesticWithoutLineBreaks() {
        SamsungCardSmsParser.Result r = parse("삼성5185승인 이*윤7,100원 일시불10/04 13:42 투썸플레이스위례누적1,538,206원");

        assertEquals(new BigDecimal("7100"), r.amount);
        assertEquals("투썸플레이스위례", r.merchant);
    }

    @Test
    public void otherSmsIsIgnored() {
        assertNull(SamsungCardSmsParser.parse("[Web발신] 인증번호 [123456]를 입력해주세요."));
        assertNull(SamsungCardSmsParser.parse("Rs.500 debited from A/c XX1234 on 01-01-25"));
        assertNull(SamsungCardSmsParser.parse(null));
    }

    @Test
    public void resolveDateUsesCurrentYear() {
        SamsungCardSmsParser.Result r = parse("삼성5185승인 이*윤 7,100원 일시불 10/04 13:42 투썸플레이스위례");
        Calendar cal = toCalendar(SamsungCardSmsParser.resolveDate(r, date(2026, 10, 4, 13, 43)));

        assertEquals(2026, cal.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH));
        assertEquals(4, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(42, cal.get(Calendar.MINUTE));
    }

    @Test
    public void resolveDateRollsBackAcrossNewYear() {
        SamsungCardSmsParser.Result r = parse("삼성5185승인 이*윤 7,100원 일시불 12/31 23:59 투썸플레이스위례");
        Calendar cal = toCalendar(SamsungCardSmsParser.resolveDate(r, date(2027, 1, 1, 0, 1)));

        assertEquals(2026, cal.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
        assertEquals(31, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void splitInstallmentsEvenly() {
        List<BigDecimal> parts = SamsungCardSmsParser.splitInstallments(new BigDecimal("2520000"), 3, 0);

        assertEquals(Arrays.asList(new BigDecimal("840000"), new BigDecimal("840000"), new BigDecimal("840000")), parts);
    }

    @Test
    public void splitInstallmentsRemainderGoesToFirst() {
        List<BigDecimal> parts = SamsungCardSmsParser.splitInstallments(new BigDecimal("1000000"), 3, 0);

        assertEquals(Arrays.asList(new BigDecimal("333334"), new BigDecimal("333333"), new BigDecimal("333333")), parts);
        assertEquals(new BigDecimal("1000000"), parts.get(0).add(parts.get(1)).add(parts.get(2)));
    }

    @Test
    public void splitSinglePayment() {
        List<BigDecimal> parts = SamsungCardSmsParser.splitInstallments(new BigDecimal("7100"), 1, 0);

        assertEquals(Arrays.asList(new BigDecimal("7100")), parts);
    }

    @Test
    public void normalizeSender() {
        assertEquals("0220008100", SmsSenderAllowlist.normalize("+82 2-2000-8100"));
        assertEquals("0220008100", SmsSenderAllowlist.normalize("0220008100"));
        assertEquals("15888900", SmsSenderAllowlist.normalize("1588-8900"));
        assertEquals("at-sibsms", SmsSenderAllowlist.normalize(" AT-SIBSMS "));
    }

    @Test
    public void allowlist() {
        Set<String> allowed = SmsSenderAllowlist.parse(SmsSenderAllowlist.DEFAULT_SENDERS);

        assertTrue(SmsSenderAllowlist.isAllowed("15888900", allowed));
        assertTrue(SmsSenderAllowlist.isAllowed("+82220008100", allowed));
        assertFalse(SmsSenderAllowlist.isAllowed("01012345678", allowed));
        assertFalse(SmsSenderAllowlist.isAllowed(null, allowed));
        assertFalse(SmsSenderAllowlist.isAllowed("15888900", SmsSenderAllowlist.parse("")));
    }

    private static SamsungCardSmsParser.Result parse(String sms) {
        SamsungCardSmsParser.Result r = SamsungCardSmsParser.parse(sms);
        assertNotNull("not parsed: " + sms, r);
        return r;
    }

    private static void assertDateTime(SamsungCardSmsParser.Result r, int month, int day, int hour, int minute) {
        assertEquals(month, r.month);
        assertEquals(day, r.day);
        assertEquals(hour, r.hour);
        assertEquals(minute, r.minute);
    }

    private static Date date(int year, int month, int day, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month - 1, day, hour, minute);
        return cal.getTime();
    }

    private static Calendar toCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
}
