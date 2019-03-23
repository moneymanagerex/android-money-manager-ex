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

package org.moneymanagerex.android.seleniumTests;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import io.selendroid.client.SelendroidDriver;
import io.selendroid.client.SelendroidKeys;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.SelendroidLauncher;

/**
 * First Selendroid test, proof of concept.
 * This class works with a manually-run test server. That might be a preferable option when
 * running a set of tests instead of instantiating the server for each test class.
 * Run the server from the scripts directory.
 */
public class MainActivityTests {
    private static WebDriver driver = null;

    @BeforeClass
    public static void setup() throws Exception {
        SelendroidCapabilities caps = new SelendroidCapabilities("com.money.manager.ex");
        // use emulator only.
        caps.setEmulator(true);

        driver = new SelendroidDriver(caps);
    }

    @AfterClass
    public static void stopSelendroidServer() {
        if (driver != null) {
            driver.quit();
        }
    }

    //@Test
    public void runActivity() {
        driver.findElement(By.id("skipTextView")).click();
        new Actions(driver).sendKeys(SelendroidKeys.MENU).perform();

    }
}
