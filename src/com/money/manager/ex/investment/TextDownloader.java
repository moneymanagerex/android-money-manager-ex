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
package com.money.manager.ex.investment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Downloads the content from given URL and returns as string.
 */
public class TextDownloader {
    public String downloadAsText(String url)
            throws IOException {

        URL urlConnection = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlConnection
                .openConnection();
        connection.setDoInput(true);
        connection.connect();

        InputStream input = connection.getInputStream();
        // Read returned value.
        InputStreamReader streamReader = new InputStreamReader(input);
        BufferedReader bufferedReader = new BufferedReader(streamReader);
        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            // return contents as string.
            builder.append(line);
        }

        // close all in reverse order.
        bufferedReader.close();
        streamReader.close();
        input.close();

        return builder.toString();
    }
}
