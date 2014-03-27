/* ========================================================================== *
 * Copyright 2014 USRZ.com and Pier Paolo Fumagalli                           *
 * -------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 *  http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 * ========================================================================== */
package org.usrz.libs.utils.configurations;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * A {@link Configurations} implementation reading <em>key-value</em> mappings
 * from <em>Java {@linkplain Properties properties} files</em>
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class PropertiesConfigurations extends MappedConfigurations {

    /* Our charset, UTF8, always */
    private static final Charset UTF8 = Charset.forName("UTF8");

    /* ====================================================================== */
    /* CONSTRUCTION                                                           */
    /* ====================================================================== */

    /**
     * Create a new {@link PropertiesConfigurations} instance reading a
     * <em>Java {@linkplain Properties properties} file</em> from the
     * specified {@link Reader}.
     */
    public PropertiesConfigurations(Reader reader)
    throws IOException, ConfigurationsException {
        super(parse(reader));
    }

    /**
     * Create a new {@link PropertiesConfigurations} instance reading a
     * <em>Java {@linkplain Properties properties} file</em> from the
     * specified {@link InputStream}.
     */
    public PropertiesConfigurations(InputStream input)
    throws IOException, ConfigurationsException {
        super(parse(input));
    }

    /* ====================================================================== */

    /**
     * Parse a <em>Java properties file</em> and return a {@link Properties}
     * instance with its contents, after validating each key name.
     */
    private static final Properties parse(InputStream input)
    throws IOException {
        if (input == null) throw new NullPointerException("Null input stream");
        return parse(new InputStreamReader(input, UTF8));
    }

    /**
     * Parse a <em>Java properties file</em> and return a {@link Properties}
     * instance with its contents, after validating each key name.
     */
    private static final Properties parse(Reader reader)
    throws IOException {
        if (reader == null) throw new NullPointerException("Null reader");

        /* Load our properties */
        final Properties properties = new Properties();
        properties.load(reader);

        /* Return our properties */
        return properties;
    }

}
