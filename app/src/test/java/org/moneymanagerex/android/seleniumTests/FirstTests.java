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
 * First selenium tests.
 * This class runs the selenium server during the test setup. This is ok for one test class
 * but won't work when running a set of tests.
 */
public class FirstTests {
    private static SelendroidLauncher selendroidServer = null;
    private static WebDriver driver = null;

    @BeforeClass
    public static void startSelendroidServer() throws Exception {
        if (selendroidServer != null) {
            selendroidServer.stopSelendroid();
        }
        SelendroidConfiguration config = new SelendroidConfiguration();
//        config.addSupportedApp("src/main/resources/selendroid-test-app-0.10.0.apk");
        config.addSupportedApp("build/outputs/apk/app-debug-unaligned.apk");
        selendroidServer = new SelendroidLauncher(config);
        selendroidServer.launchSelendroid();

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
        if (selendroidServer != null) {
            selendroidServer.stopSelendroid();
        }
    }

    //@Test
    public void firstTest() {
        // close tutorial
        driver.findElement(By.id("skipTextView")).click();
        //driver.findElement(By.id("buttonLinkDropbox")).click();
        // press Menu key
        new Actions(driver).sendKeys(SelendroidKeys.MENU).perform();
        // Click Entities

        //Assert.assertThat();
    }

//    @Test
//    public void sampleTest() throws Exception {
//        SelendroidCapabilities capa = new SelendroidCapabilities("com.money.manager.ex");
//
//        WebDriver driver = new SelendroidDriver(capa);
//        WebElement inputField = driver.findElement(By.id("my_text_field"));
//        Assert.assertEquals("true", inputField.getAttribute("enabled"));
//        inputField.sendKeys("Selendroid");
//        Assert.assertEquals("Selendroid", inputField.getText());
//        driver.quit();
//    }
}
