/*
 * Copyright 2006-2020 The MZmine Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Logger;

/**
 * Internet related utilities
 */
public class InetUtils {

    private static final Logger logger = Logger
            .getLogger(InetUtils.class.getName());

    /**
     * Opens a connection to the given URL (typically HTTP) and retrieves the
     * data from server. Data is assumed to be in UTF-8 encoding.
     */
    public static String retrieveData(URL url) throws IOException {

        logger.finest("Retrieving data from URL " + url);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-agent", "MZmine 2");

        // We need to deal with redirects from HTTP to HTTPS manually, this
        // happens for MassBank Europe
        // Based on
        // https://stackoverflow.com/questions/1884230/urlconnection-doesnt-follow-redirect
        switch (connection.getResponseCode()) {
        case HttpURLConnection.HTTP_MOVED_PERM:
        case HttpURLConnection.HTTP_MOVED_TEMP:
            String location = connection.getHeaderField("Location");
            location = URLDecoder.decode(location, "UTF-8");
            URL next = new URL(url, location); // Deal with relative URLs
            connection.disconnect();
            connection = (HttpURLConnection) next.openConnection();
            connection.setRequestProperty("User-agent", "MZmine 2");
        }

        InputStream is = connection.getInputStream();

        if (is == null) {
            throw new IOException("Could not establish a connection to " + url);
        }

        StringBuffer buffer = new StringBuffer();

        try {
            InputStreamReader reader = new InputStreamReader(is, "UTF-8");

            char[] cb = new char[1024];

            int amtRead = reader.read(cb);
            while (amtRead > 0) {
                buffer.append(cb, 0, amtRead);
                amtRead = reader.read(cb);
            }

        } catch (UnsupportedEncodingException e) {
            // This should never happen, because UTF-8 is supported
            e.printStackTrace();
        }

        is.close();

        logger.finest(
                "Retrieved " + buffer.length() + " characters from " + url);

        return buffer.toString();

    }

}
