/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.moneymanagerex.android.tests;

import android.test.AndroidTestCase;
import android.test.mock.MockContext;

import com.money.manager.ex.home.RecentDatabaseEntry;
import com.money.manager.ex.home.RecentDatabasesProvider;
import com.money.manager.ex.investment.YqlSecurityPriceUpdater;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for YQL security downloader.
 * Created by Alen Siljak on 20/08/2015.
 */
public class RecentDatabaseProviderTests extends AndroidTestCase {

    private RecentDatabasesProvider _testObject;

    public void setUp() throws Exception {
        super.setUp();

        MockContext context = new MockContext();
        _testObject = new RecentDatabasesProvider(context);
    }

    public void tearDown() throws Exception {
        _testObject = null;
    }

    public void testLoad() throws Exception {
        ArrayList<RecentDatabaseEntry> expected = getList();

        List<RecentDatabaseEntry> actual = _testObject.load();

        Assert.assertEquals(expected, actual);
    }

    public void testSave() {
        ArrayList<RecentDatabaseEntry> list = getList();
        String expected = "todo";

        _testObject.save(list);

        String actual = _testObject.readPreference();

        Assert.assertEquals(expected, actual);
    }

    private ArrayList<RecentDatabaseEntry> getList() {
        ArrayList<RecentDatabaseEntry> list = new ArrayList<>();

        list.add(getEntry());
        list.add(getEntry());
        list.add(getEntry());

        return list;
    }

    private RecentDatabaseEntry getEntry() {
        RecentDatabaseEntry entry = new RecentDatabaseEntry();

        entry.dropboxFileName = "";
        entry.linkedToDropbox = false;
        entry.fileName = "filename.mmb";

        return entry;
    }
}