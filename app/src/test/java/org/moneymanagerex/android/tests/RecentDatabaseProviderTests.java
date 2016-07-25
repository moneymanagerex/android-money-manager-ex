/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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

import com.google.gson.Gson;
import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.home.RecentDatabaseEntry;
import com.money.manager.ex.home.RecentDatabasesProvider;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Recent Database Provider.
 *
 * Created by Alen Siljak on 20/08/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class RecentDatabaseProviderTests {

    private RecentDatabasesProvider _testObject;

    @Before
    public void setUp() throws Exception {
        _testObject = new RecentDatabasesProvider(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
        _testObject = null;
    }

    @Test
    public void testSaveAndLoad() {
        // Save empty collection.

        _testObject.save();
        // test
        String preference = _testObject.readPreference();
        Assert.assertEquals("{}", preference);

        // save full collection

        LinkedHashMap<String, RecentDatabaseEntry> testEntries = getEntries();
        _testObject.map = testEntries;
        _testObject.save();

        // Load

        _testObject.load();
        LinkedHashMap<String, RecentDatabaseEntry> actual = _testObject.map;
        Gson gson = new Gson();

        // compare individual elements.
        
        for (RecentDatabaseEntry entry : actual.values()) {
            RecentDatabaseEntry expected = testEntries.get(entry.filePath);
            assertThat(gson.toJson(entry))
                    .isEqualTo(gson.toJson(expected));
        }
    }

    @Test
    public void testInsert() {
        RecentDatabaseEntry entry = getEntry(false);
        String expected = "{\"filename.mmb\":{\"filePath\":\"filename.mmb\",\"remoteFileName\":\"\",\"linkedToCloud\":false}}";

        _testObject.add(entry);

        String actual = _testObject.toJson();

        Assert.assertEquals(expected, actual);
    }

    // Private

    private LinkedHashMap<String, RecentDatabaseEntry> getEntries() {
        LinkedHashMap<String, RecentDatabaseEntry> map = new LinkedHashMap<>();

        for(int i = 0; i < 3; i++) {
            RecentDatabaseEntry entry = getEntry(true);
            map.put(entry.filePath, entry);
        }

        return map;
    }

    private RecentDatabaseEntry getEntry(boolean useRandomPath) {
        RecentDatabaseEntry entry = new RecentDatabaseEntry();

        String unique = useRandomPath ? Double.toString(Math.random()) : "";

        entry.remoteFileName = "";
        entry.linkedToCloud = false;
        entry.filePath = "filename" + unique + ".mmb";

        return entry;
    }

}