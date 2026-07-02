/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import android.content.Intent;

import com.money.manager.ex.core.docstorage.FileStorageHelper;
import com.money.manager.ex.settings.DatabaseSettingsFragment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ExportIntentTests {
    private static final String DATABASE_MIME_TYPE = "application/octet-stream";

    @Test
    public void databaseSettingsBackupIntentUsesConcreteMimeType() throws Exception {
        Intent intent = invokeIntentBuilder(DatabaseSettingsFragment.class, "buildBackupIntent");

        assertCreateDocumentIntent(intent);
        assertEquals("your_export_db.mmb", intent.getStringExtra(Intent.EXTRA_TITLE));
        assertTrue(intent.getStringExtra(Intent.EXTRA_TITLE).endsWith(".mmb"));
    }

    @Test
    public void fileStorageCreateIntentUsesTimestampedMmbTitle() throws Exception {
        Intent intent = invokeIntentBuilder(FileStorageHelper.class, "buildCreateFileIntent");

        assertCreateDocumentIntent(intent);
        assertTrue(intent.getStringExtra(Intent.EXTRA_TITLE).matches("your_data_\\d{8}_\\d{6}\\.mmb"));
    }

    private static void assertCreateDocumentIntent(Intent intent) {
        assertEquals(Intent.ACTION_CREATE_DOCUMENT, intent.getAction());
        assertTrue(intent.hasCategory(Intent.CATEGORY_OPENABLE));
        assertEquals(DATABASE_MIME_TYPE, intent.getType());
        assertNotEquals("*/*", intent.getType());
    }

    private static Intent invokeIntentBuilder(Class<?> targetClass, String methodName) throws Exception {
        Method method = targetClass.getDeclaredMethod(methodName);
        method.setAccessible(true);

        return (Intent) method.invoke(null);
    }
}
