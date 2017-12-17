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
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.home.DatabaseMetadataFactory;
import com.money.manager.ex.home.RecentDatabasesProvider;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.moneymanagerex.android.testhelpers.TestApplication;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.LinkedHashMap;

/**
 * Tests for Recent Database Provider.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, application = TestApplication.class)
public class RecentDatabaseProviderTests {

    private RecentDatabasesProvider _testObject;

    @Before
    public void setUp() throws Exception {
        MmexApplication app = (MmexApplication) RuntimeEnvironment.application;
        _testObject = new RecentDatabasesProvider(app);
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
//        Assert.assertEquals("{}", preference);
//        assertThat(preference).isEqualTo("{}");

        // update full collection

        LinkedHashMap<String, DatabaseMetadata> testEntries = getEntries();
        _testObject.map = testEntries;
        _testObject.save();

        // Load

        _testObject.load();
        LinkedHashMap<String, DatabaseMetadata> actual = _testObject.map;
        Gson gson = new Gson();

        // compare individual elements.
        
        for (DatabaseMetadata entry : actual.values()) {
            DatabaseMetadata expected = testEntries.get(entry.localPath);
//            assertThat(gson.toJson(entry)).isEqualTo(gson.toJson(expected));
        }
    }

    //@Test
    public void testInsert() {
        DatabaseMetadata entry = getEntry(false);
        String expected = "{\"filename.mmb\":{\"localPath\":\"filename.mmb\",\"remotePath\":\"\",\"linkedToCloud\":false}}";

        _testObject.add(entry);

        String actual = _testObject.toJson();

        Assert.assertEquals(expected, actual);
    }

//    @Test public void testGetCurrent() {
//        // todo prepare conditions
//
//        DatabaseMetadata current = _testObject.getCurrent();
//
//        assertThat(current).isNotNull();
//    }

    //@Test
    public void add_does_not_create_duplicate() {
        DatabaseMetadata entry1 = DatabaseMetadataFactory.getInstance("path1");
        _testObject.add(entry1);

        DatabaseMetadata entry2 = DatabaseMetadataFactory.getInstance("path2");
        _testObject.add(entry2);

        DatabaseMetadata entry1Duplicate = DatabaseMetadataFactory.getInstance("path1");
        _testObject.add(entry1Duplicate);

        // _testObject.get("path1");
//        assertThat(_testObject.count()).isEqualTo(2);
    }

    // Private

    private LinkedHashMap<String, DatabaseMetadata> getEntries() {
        LinkedHashMap<String, DatabaseMetadata> map = new LinkedHashMap<>();

        for(int i = 0; i < 3; i++) {
            DatabaseMetadata entry = getEntry(true);
            map.put(entry.localPath, entry);
        }

        return map;
    }

    private DatabaseMetadata getEntry(boolean useRandomPath) {
        DatabaseMetadata entry = new DatabaseMetadata();

        String unique = useRandomPath ? Double.toString(Math.random()) : "";

        entry.remotePath = "";
        entry.localPath = "filename" + unique + ".mmb";

        return entry;
    }
}