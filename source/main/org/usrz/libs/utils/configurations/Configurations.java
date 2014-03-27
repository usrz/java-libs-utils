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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * The {@link Configurations} class is a unmodifiable {@link Map} used to
 * manage configuration items.
 *
 * <p>Configuration items are much like properties but <em>should</em> be
 * keyed with names like <b>listener.port</b> or <b>jdbc.url</b>. In other
 * words they <em>should</em> be identifier tokens separated by dots.</p>
 *
 * <p>Configuration keys should never be <b>null</b> but the <em>empty
 * string</em> is allowed (<b>null</b> is an alias to it).</p>
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public abstract class Configurations implements Map<String, String> {

    /**
     * A unique immutable <em>empty</em> {@link Configurations} instance.
     */
    public static final Configurations EMPTY_CONFIGURATIONS = new Configurations() {

        @Override
        public String getString(Object key, String defaultValue) {
            return defaultValue;
        }

        @Override
        public Set<Entry<String, String>> entrySet() {
            return Collections.emptySet();
        }

        @Override
        public int size() {
            return 0;
        }
    };

    /* Platform-dependant line separator to save configurations */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    /* Charset for saving, always UTF8 */
    private static final Charset UTF8 = Charset.forName("UTF8");

    /* ====================================================================== */
    /* CONSTRUCTION                                                           */
    /* ====================================================================== */

    /**
     * Create a new {@link Configurations} instance.
     */
    protected Configurations() {
        /* Nothing to do */
    }

    /* ====================================================================== */
    /* MERGING AND OVERRIDING                                                 */
    /* ====================================================================== */

    /**
     * Merge the mappings from the specified {@link Map} with those contained by
     * this instance and return a <em>new</em> {@link Configurations} instance.
     *
     * <p>If a mapping is contained both in the specified {@link Map} and in
     * this instance, the one contained by this instance will be kept.</p>
     */
    public final Configurations merge(Map<?, ?> map)
    throws ConfigurationsException {
        final Map<Object, Object> configurations = new HashMap<>();
        configurations.putAll(map);
        configurations.putAll(this);
        return new MappedConfigurations(configurations);
    }

    /**
     * Override the mappings contained by this instance with the ones from the
     * specified {@link Map} and return a <em>new</em> {@link Configurations}
     * instance.
     *
     * <p>If a mapping is contained both in the specified {@link Map} and in
     * this instance, the one contained by this instance will be overridden
     * with the one from the specified {@link Map}.</p>
     */
    public final Configurations override(Map<?, ?> map)
    throws ConfigurationsException {
        final Map<Object, Object> configurations = new HashMap<>();
        configurations.putAll(this);
        configurations.putAll(map);
        return new MappedConfigurations(configurations);
    }

    /* ====================================================================== */
    /* EXTRACTING BY PREFIX AND REMAPPING BY ADDING/REMOVING PREFIXES         */
    /* ====================================================================== */

    /**
     * Prepend the specified <em>prefix</em> to all the mappings contained
     * by this instance and return a <em>new</em> {@link Configurations}
     * instance.
     *
     * <p>Given a prefix like "<code>prefix</code>" and mappings as</p>
     *
     * <pre>
     * key1 = value1
     * key2 = value2
     * </pre>
     *
     * <p>this method will return a {@link Configurations} instance like:</p>
     *
     * <pre>
     * prefix.key1 = value1
     * prefix.key2 = value2
     * </pre>
     */
    public final Configurations prefix(String prefix)
    throws ConfigurationsException {

        /* Check and normalize the prefix */
        if (prefix == null) throw new NullPointerException("Null prefix");
        if (!prefix.endsWith(".")) prefix += ".";

        /* Remap adding the new prefix */
        final Map<String, String> configurations = new HashMap<>();
        for (Entry<String, String> entry: entrySet())
            configurations.put(prefix + entry.getKey(), entry.getValue());

        /* All done */
        return new MappedConfigurations(configurations);
    }

    /**
     * Extract the mappings starting with the specified <em>prefix</em> from
     * this instance and return a <em>new</em> {@link Configurations}
     * instance.
     *
     * <p>Given a prefix like "<code>prefix</code>" and mappings as</p>
     *
     * <pre>
     * prefix.key1 = value1
     * prefix.key2 = value2
     * different.key1 = value1
     * different.key2 = value2
     * </pre>
     *
     * <p>this method will return a {@link Configurations} instance like:</p>
     *
     * <pre>
     * prefix.key1 = value1
     * prefix.key2 = value2
     * </pre>
     */
    public final Configurations extract(String prefix)
    throws ConfigurationsException {

        /* Check and normalize the prefix */
        if (prefix == null) throw new NullPointerException("Null prefix");
        while (prefix.endsWith(".")) prefix = prefix.substring(0, prefix.length() - 1);
        final String prefixDot = prefix + ".";

        /* Remap stripping the prefix */
        final Map<String, String> configurations = new HashMap<>();
        for (Entry<String, String> entry: entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            if (prefix.equals(key) || key.startsWith(prefixDot))
                configurations.put(key, value);
        }

        /* All done */
        return new MappedConfigurations(configurations);
    }

    /**
     * Extract the mappings starting with the specified <em>prefix</em> from
     * this instance and return a <em>new</em> {@link Configurations}
     * instance with the given <em>prefix</em> stripped.
     *
     * <p>Given a prefix like "<code>prefix</code>" and mappings as</p>
     *
     * <pre>
     * prefix.key1 = prefixed value 1
     * prefix.key2 = prefixed value 2
     * different.key1 = a different value 1
     * different.key2 = a different value 2
     * </pre>
     *
     * <p>this method will return a {@link Configurations} instance like:</p>
     *
     * <pre>
     * key1 = prefixed value 1
     * key2 = prefixed value 2
     * </pre>
     */
    public final Configurations strip(String prefix)
    throws ConfigurationsException {

        /* Check and normalize the prefix */
        if (prefix == null) throw new NullPointerException("Null prefix");
        while (prefix.endsWith(".")) prefix = prefix.substring(0, prefix.length() - 1);
        final String prefixDot = prefix + ".";
        final int prefixLen = prefixDot.length();

        /* Remap stripping the prefix */
        final Map<String, String> configurations = new HashMap<>();
        for (Entry<String, String> entry: entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            if (prefix.equals(key)) configurations.put("", value);
            if (key.startsWith(prefixDot))
                configurations.put(key.substring(prefixLen, key.length()), value);
        }

        /* All done */
        return new MappedConfigurations(configurations);
    }

    /* ====================================================================== */
    /* SUB-PROPERTIES EXTRACTION/GROUPING                                     */
    /* ====================================================================== */

    /**
     * Group mappings by prefix, and return a {@link Map} of
     * <code><em>prefix -&gt; {@link Configurations}</em></code>.
     *
     * <p>This method will group configuration items. Given mappings as:</p>
     *
     * <pre>
     * key1 = value1
     * key2 = value2
     * prefix.group1.key1 = prefixed value 1
     * prefix.group1.key2 = prefixed value 2
     * prefix.group2.key1 = a different value 1
     * prefix.group2.key2 = a different value 2
     * </pre>
     *
     * <p>and a prefix like "<code>prefix</code>", this method will return
     * a {@link Map} with two entries. One, keyed by <code>group1</code>
     * containing</p>
     *
     * <pre>
     * key1 = prefixed value 1
     * key2 = prefixed value 2
     * </pre>
     *
     * <p>and a second one, keyed by <code>group2</code> containing</p>
     *
     * <pre>
     * key1 = a different value 1
     * key2 = a different value 2
     * </pre>
     */
    public final Map<String, Configurations> group(String prefix)
    throws ConfigurationsException {

        /* Check and normalize */
        if (prefix == null) throw new NullPointerException("Null prefix");
        if (!prefix.endsWith(".")) prefix += ".";

        /* Do we have anything to tokenize? */
        final Set<String> groups = new HashSet<>();
        for (String key: keySet()) {
            if (key.startsWith(prefix)) {
                int position = key.indexOf('.', prefix.length());
                if (position < 0) position = key.length();
                groups.add(key.substring(prefix.length(), position));
            }
        }

        /* Extract the various sub-configurations */
        final Map<String, Configurations> grouped = new HashMap<>();
        for (String group: groups) {
            grouped.put(group, strip(prefix + group));
        }

        /* All done, return our map, made unmodifiable */
        return Collections.unmodifiableMap(grouped);

    }

    /**
     * Group mappings by prefix, and return a {@link Map} of
     * <code><em>prefix -&gt; {@link Configurations}</em></code> only
     * for the specified groups.
     *
     * <p>This method will group configuration items like {@link #group(String)}
     * but will return only the specified groups. Given mappings as:</p>
     *
     * <pre>
     * prefix.group1.key1 = prefixed value 1
     * prefix.group1.key2 = prefixed value 2
     * prefix.group2.key1 = a different value 1
     * prefix.group2.key2 = a different value 2
     * prefix.group3.key1 = yet another different value 1
     * prefix.group3.key2 = yet another different value 2
     * </pre>
     *
     * <p>then a prefix like "<code>prefix</code>" and groups like
     * "<code>group1</code>" and "<code>group3</code>", this method will return
     * a {@link Map} with two entries. One, keyed by <code>group1</code>
     * containing</p>
     *
     * <pre>
     * key1 = prefixed value 1
     * key2 = prefixed value 2
     * </pre>
     *
     * <p>and a second one, keyed by <code>group3</code> containing</p>
     *
     * <pre>
     * key1 = yet another different value 1
     * key2 = yet another different value 2
     * </pre>
     */
    public final Map<String, Configurations> group(String prefix, String... groups)
    throws ConfigurationsException {

        /* Check and normalize the prefix */
        if (prefix == null) throw new NullPointerException("Null key");
        while (prefix.endsWith(".")) prefix = prefix.substring(0, prefix.length() - 1);

        /* Extract and return only our groups */
        final Map<String, Configurations> grouped = group(prefix);
        final Map<String, Configurations> explicit = new HashMap<>();
        for (String group: groups) {
            if (grouped.containsKey(group)) explicit.put(group, grouped.get(group));
        }

        /* All done, return our map, made unmodifiable */
        return Collections.unmodifiableMap(explicit);
    }

    /**
     * Group mappings by prefix, and return a {@link Map} of
     * <code><em>prefix -&gt; {@link Configurations}</em></code>, optionally
     * containing the group names specified as a value for the prefix.
     *
     * <p>When the <em>explicit</em> flag is <b>false</b> this method will
     * behave exactly like {@link #group(String)}.</p>
     *
     * <p>On the other hand, when the <em>explicit</em> flag is set, this
     * method will behave similarly to {@link #group(String, String...)}
     * but the list of <em>groups</em> to return is derived from the value
     * of the specified <em>prefix</em>. In other words, given mappings as:</p>
     *
     * <pre>
     * prefix = group1,group3
     * prefix.group1.key1 = prefixed value 1
     * prefix.group1.key2 = prefixed value 2
     * prefix.group2.key1 = a different value 1
     * prefix.group2.key2 = a different value 2
     * prefix.group3.key1 = yet another different value 1
     * prefix.group3.key2 = yet another different value 2
     * </pre>
     *
     * <p>when the specified prefix is "<code>prefix</code>", its value
     * (<code>group1,group3</code>) will be parsed and tokenized using
     * either <em>whitespace</em> or <em>commas</em>, and this method will
     * return two mappings, the first keyed by "<code>group1</code>"</p>
     *
     * <pre>
     * key1 = prefixed value 1
     * key2 = prefixed value 2
     * </pre>
     *
     * <p>and a second one, keyed by <code>group3</code> containing</p>
     *
     * <pre>
     * key1 = yet another different value 1
     * key2 = yet another different value 2
     * </pre>
     */
    public final Map<String, Configurations> group(String prefix, boolean explicit)
    throws ConfigurationsException {
        if (!explicit) return group(prefix);

        /* Get the value for our key */
        final String value = this.get(prefix);
        if (value == null) return Collections.emptyMap();

        /* Tokenize the key to get the group names */
        final StringTokenizer tokenizer = new StringTokenizer(value, " \t\r\n,");
        final Set<String> groups = new HashSet<>();
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken().trim();
            if (token.length() > 0) groups.add(token);
        }

        /* All done, return our map, made unmodifiable */
        return group(prefix, groups.toArray(new String[groups.size()]));
    }

    /* ====================================================================== */
    /* BASIC GET METHODS                                                      */
    /* ====================================================================== */

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link String} or <b>null</b> if no mapping was found.
     */
    @Override
    public final String get(Object key) {
        return getString(key, null);
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link String} or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final String get(Object key, String defaultValue) {
        return getString(key, defaultValue);
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link String} or <b>null</b> if no mapping was found.
     */
    public final String getString(Object key) {
        return getString(key, null);
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link String} or the specified <em>default value</em> if no mapping
     * was found.
     */
    public abstract String getString(Object key, String defaultValue);

    /* ====================================================================== */
    /* CONVERSION METHODS                                                     */
    /* ====================================================================== */

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link File} or <b>null</b> if no mapping was found.
     */
    public final File getFile(Object key) {
        return this.getFile(key, null);
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link URI} or <b>null</b> if no mapping was found.
     */
    public final URI getURI(Object key) {
        return this.getURI(key, null);
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link URL} or <b>null</b> if no mapping was found.
     */
    public final URL getURL(Object key) {
        return this.getURL(key, null);
    }

    /* ====================================================================== */

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link File} or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final File getFile(Object key, File defaultValue) {
        return this.get(key, defaultValue);
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link URI} or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final URI getURI(Object key, URI defaultValue) {
        return this.get(key, defaultValue);
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link URL} or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final URL getURL(Object key, URL defaultValue) {
        return this.get(key, defaultValue);
    }

    /* ====================================================================== */

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link File} or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final File get(Object key, File defaultValue) {
        final String value = this.get(key);
        return value == null ? defaultValue : new File(value);
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link URL} or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final URL get(Object key, URL defaultValue) {
        final String value = this.get(key);
        try {
            return value == null ? defaultValue : new URL(value);
        } catch (MalformedURLException exception) {
            throw new ConfigurationsException("Unvalid URL " + value + " for key \"" + key + "\"", exception);
        }
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link URI} or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final URI get(Object key, URI defaultValue) {
        final String value = this.get(key);
        try {
            return value == null ? defaultValue : new URI(value);
        } catch (URISyntaxException exception) {
            throw new ConfigurationsException("Unvalid URI " + value + " for key \"" + key + "\"", exception);
        }
    }

    /* ====================================================================== */
    /* PRIMITIVES METHODS                                                     */
    /* ====================================================================== */

    /**
     * Return the value of associated with the given <em>key</em> as a
     * <b>byte</b> or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final byte get(Object key, byte defaultValue) {
        final String value = this.get(key);
        try {
            return value == null ? defaultValue : Byte.parseByte(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationsException("Invalid byte " + value + " for key \"" + key + "\"", exception);
        }
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * <b>short</b> or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final short get(Object key, short defaultValue) {
        final String value = this.get(key);
        try {
            return value == null ? defaultValue : Short.parseShort(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationsException("Invalid short " + value + " for key \"" + key + "\"", exception);
        }
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * <b>int</b> or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final int get(Object key, int defaultValue) {
        final String value = this.get(key);
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationsException("Invalid int " + value + " for key \"" + key + "\"", exception);
        }
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * <b>long</b> or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final long get(Object key, long defaultValue) {
        final String value = this.get(key);
        try {
            return value == null ? defaultValue : Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationsException("Invalid long " + value + " for key \"" + key + "\"", exception);
        }
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * <b>float</b> or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final float get(Object key, float defaultValue) {
        final String value = this.get(key);
        try {
            return value == null ? defaultValue : Float.parseFloat(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationsException("Invalid float " + value + " for key \"" + key + "\"", exception);
        }
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * <b>double</b> or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final double get(Object key, double defaultValue) {
        final String value = this.get(key);
        try {
            return value == null ? defaultValue : Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationsException("Invalid double " + value + " for key \"" + key + "\"", exception);
        }
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * <b>boolean</b> or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final boolean get(Object key, boolean defaultValue) {
        try {
            final String value = this.get(key).toLowerCase();
            if ("true".equals(value)) return true;
            if ("false".equals(value)) return false;
            throw new ConfigurationsException("Invalid boolean " + value + " for key \"" + key + "\"");
        } catch (NullPointerException exception) {
            return defaultValue;
        }
    }

    /* ====================================================================== */
    /* AUTOBOXING METHODS                                                     */
    /* ====================================================================== */

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link Byte} or <b>null</b> if no mapping was found.
     */
    public final Byte getByte(Object key) {
        return this.getByte(key, null);
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link Short} or <b>null</b> if no mapping was found.
     */
    public final Short getShort(Object key) {
        return this.getShort(key, null);
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link Integer} or <b>null</b> if no mapping was found.
     */
    public final Integer getInteger(Object key) {
        return this.getInteger(key, null);
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link Long} or <b>null</b> if no mapping was found.
     */
    public final Long getLong(Object key) {
        return this.getLong(key, null);
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link Float} or <b>null</b> if no mapping was found.
     */
    public final Float getFloat(Object key) {
        return this.getFloat(key, null);
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link Double} or <b>null</b> if no mapping was found.
     */
    public final Double getDouble(Object key) {
        return this.getDouble(key, null);
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link Boolean} or <b>null</b> if no mapping was found.
     */
    public final Boolean getBoolean(Object key) {
        return this.getBoolean(key, null);
    }

    /* ====================================================================== */

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link Byte} or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final Byte getByte(Object key, Byte defaultValue) {
        final String value = this.get(key);
        if (value == null) return defaultValue;
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationsException("Invalid byte " + value + " for key \"" + key + "\"", exception);
        }
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link Short} or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final Short getShort(Object key, Short defaultValue) {
        final String value = this.get(key);
        if (value == null) return defaultValue;
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationsException("Invalid short " + value + " for key \"" + key + "\"", exception);
        }
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link Integer} or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final Integer getInteger(Object key, Integer defaultValue) {
        final String value = this.get(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationsException("Invalid int " + value + " for key \"" + key + "\"", exception);
        }
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link Long} or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final Long getLong(Object key, Long defaultValue) {
        final String value = this.get(key);
        if (value == null) return defaultValue;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationsException("Invalid long " + value + " for key \"" + key + "\"", exception);
        }
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link Float} or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final Float getFloat(Object key, Float defaultValue) {
        final String value = this.get(key);
        if (value == null) return defaultValue;
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationsException("Invalid float " + value + " for key \"" + key + "\"", exception);
        }
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link Double} or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final Double getDouble(Object key, Double defaultValue) {
        final String value = this.get(key);
        if (value == null) return defaultValue;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationsException("Invalid double " + value + " for key \"" + key + "\"", exception);
        }
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link Boolean} or the specified <em>default value</em> if no mapping
     * was found.
     */
    public final Boolean getBoolean(Object key, Boolean defaultValue) {
        try {
            final String value = this.get(key).toLowerCase();
            if ("true".equals(value)) return true;
            if ("false".equals(value)) return false;
            throw new ConfigurationsException("Invalid boolean " + value + " for key \"" + key + "\"");
        } catch (NullPointerException exception) {
            return defaultValue;
        }
    }

    /* ====================================================================== */
    /* REQUIRED VALUES METHODS                                                */
    /* ====================================================================== */

    /**
     * Return the value of associated with the given <em>key</em> as a
     * <em>boolean</em> or throw a {@link ConfigurationsException} if no
     * mapping was found.
     */
    public final boolean requireBoolean(Object key) {
        final Boolean value = this.getBoolean(key);
        if (value != null) return value.booleanValue();
        throw new ConfigurationsException("Required boolean \"" + key + "\" not found");
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * <em>byte</em> or throw a {@link ConfigurationsException} if no
     * mapping was found.
     */
    public final byte requireByte(Object key) {
        final Byte value = this.getByte(key);
        if (value != null) return value.byteValue();
        throw new ConfigurationsException("Required byte \"" + key + "\" not found");
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * <em>double</em> or throw a {@link ConfigurationsException} if no
     * mapping was found.
     */
    public final double requireDouble(Object key) {
        final Double value = this.getDouble(key);
        if (value != null) return value.doubleValue();
        throw new ConfigurationsException("Required double \"" + key + "\" not found");
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link File} or throw a {@link ConfigurationsException} if no
     * mapping was found.
     */
    public final File requireFile(Object key) {
        final File value = this.getFile(key);
        if (value != null) return value;
        throw new ConfigurationsException("Required file \"" + key + "\" not found");
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * <em>float</em> or throw a {@link ConfigurationsException} if no
     * mapping was found.
     */
    public final float requireFloat(Object key) {
        final Float value = this.getFloat(key);
        if (value != null) return value.floatValue();
        throw new ConfigurationsException("Required float \"" + key + "\" not found");
    }

    /**
     * Return the value of associated with the given <em>key</em> as an
     * <em>int</em> or throw a {@link ConfigurationsException} if no
     * mapping was found.
     */
    public final int requireInteger(Object key) {
        final Integer value = this.getInteger(key);
        if (value != null) return value.intValue();
        throw new ConfigurationsException("Required integer \"" + key + "\" not found");
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * <em>long</em> or throw a {@link ConfigurationsException} if no
     * mapping was found.
     */
    public final long requireLong(Object key) {
        final Long value = this.getLong(key);
        if (value != null) return value.longValue();
        throw new ConfigurationsException("Required long \"" + key + "\" not found");
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * <em>short</em> or throw a {@link ConfigurationsException} if no
     * mapping was found.
     */
    public final short requireShort(Object key) {
        final Short value = this.getShort(key);
        if (value != null) return value.shortValue();
        throw new ConfigurationsException("Required short \"" + key + "\" not found");
    }

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link String} or throw a {@link ConfigurationsException} if no
     * mapping was found.
     */
    public final String requireString(Object key) {
        final String value = this.getString(key);
        if (value != null) return value;
        throw new ConfigurationsException("Required value \"" + key + "\" not found");
    }

    /**
     * Return the value of associated with the given <em>key</em> as an
     * {@link URI} or throw a {@link ConfigurationsException} if no
     * mapping was found.
     */
    public final URI requireURI(Object key) {
        final URI value = this.getURI(key);
        if (value != null) return value;
        throw new ConfigurationsException("Required URI \"" + key + "\" not found");
    }

    /**
     * Return the value of associated with the given <em>key</em> as an
     * {@link URL} or throw a {@link ConfigurationsException} if no
     * mapping was found.
     */
    public final URL requireURL(Object key) {
        final URL value = this.getURL(key);
        if (value != null) return value;
        throw new ConfigurationsException("Required URL \"" + key + "\" not found");
    }

    /* ====================================================================== */
    /* UNMODIFIABLE COLLECTION METHODS                                        */
    /* ====================================================================== */

    /**
     * Returns a {@link Set} view of the keys contained in this instance.
     */
    @Override
    public Set<String> keySet() {
        final Set<String> keys = new HashSet<>();
        for (Entry<String, String> entry: entrySet())
            keys.add(entry.getKey());
        return Collections.unmodifiableSet(keys);
    }

    /**
     * Returns a {@link Collection} view of the values contained in this
     * instance.
     */
    @Override
    public Collection<String> values() {
        final List<String> values = new ArrayList<>();
        for (Entry<String, String> entry: entrySet())
            values.add(entry.getValue());
        return Collections.unmodifiableCollection(values);
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this instance.
     */
    @Override
    public abstract Set<Entry<String, String>> entrySet();

    /* ====================================================================== */
    /* DELEGATED METHODS                                                      */
    /* ====================================================================== */

    /**
     * Returns the number of key-value mappings in this instance.
     */
    @Override
    public abstract int size();

    /**
     * Returns <b>true</b> if this instance contains no key-value mappings.
     */
    @Override
    public final boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns <b>true</b> if this instance contains a mapping for the
     * specified key.
     */
    @Override
    public final boolean containsKey(Object key) {
        final String defaultValue = new String();
        final String value = this.getString(key, defaultValue);
        return value != defaultValue;
    }

    /**
     * Returns <b>true</b> if this instance maps one or more keys to the
     * specified value.
     */
    @Override
    public final boolean containsValue(Object value) {
        if (value == null) return false;
        for (String string: values())
            if (string.equals(value)) return true;
        return false;
    }

    /* ====================================================================== */
    /* DUMPING/LISTING                                                        */
    /* ====================================================================== */

    /**
     * List the contents of this {@link Configurations} instance to the
     * specified {@link OutputStream}.
     *
     * <p>This method will use the <em>UTF-8</em> character set, and will save
     * entries in a way similar to {@linkplain Properties java properties}.</p>
     */
    public final Configurations list(OutputStream output)
    throws IOException {
        return list(new OutputStreamWriter(output, UTF8));
    }

    /**
     * List the contents of this {@link Configurations} instance to the
     * specified {@link Writer}.
     *
     * <p>This method will save entries in a way similar to
     * {@linkplain Properties java properties}.</p>
     */
    public final Configurations list(Writer writer)
    throws IOException {
        final Set<String> sorted = new TreeSet<>(keySet());
        for (String key: sorted) {
            writer.write(key);
            writer.write(" = ");
            writer.write(this.get(key));
            writer.write(LINE_SEPARATOR);
        }
        writer.flush();
        return this;
    }

    /* ====================================================================== */
    /* OBJECT METHODS                                                         */
    /* ====================================================================== */

    /**
     * Compute the unique <em>hash-code</em> for this instance.
     *
     * <p>{@link Configurations} instances with the same <em>key-value</em>
     * mappings will have the same <em>hash-code</em>.</p>
     */
    @Override
    public final int hashCode() {
        int hash = 0;
        for (Entry<String, String> entry: entrySet())
            hash += (entry.getKey().hashCode() ^ entry.getValue().hashCode());
        return hash ^ Configurations.class.hashCode();
    }

    /**
     * Compare the specified {@link Object} for equality to this one.
     *
     * <p>To be considered <em>equal</em> the specified {@link Object} must be
     * a {@link Configurations} instance, and contain the same mappings as this
     * instance.</p>
     */
    @Override
    public final boolean equals(Object object) {
        if (object == this) return true;
        if (object == null) return false;
        try {
            final Configurations configurations = (Configurations) object;
            if (size() != configurations.size()) return false;
            for (Entry<String, String> entry: entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (value.equals(configurations.get(key))) continue;
                return false;
            }
            return true;
        } catch (ClassCastException exception) {
            return false;
        }
    }

    /* ====================================================================== */
    /* Unsupported METHODS                                                    */
    /* ====================================================================== */

    /**
     * <b>Unsupported</b>: {@link Configurations} instances are unmodifiable.
     *
     * @deprecated Do not use, warn the compiler.
     */
    @Override @Deprecated
    public final String put(String key, String value) {
        throw new UnsupportedOperationException();
    }

    /**
     * <b>Unsupported</b>: {@link Configurations} instances are unmodifiable.
     *
     * @deprecated Do not use, warn the compiler.
     */
    @Override @Deprecated
    public final String remove(Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * <b>Unsupported</b>: {@link Configurations} instances are unmodifiable.
     *
     * @deprecated Do not use, warn the compiler.
     */
    @Override @Deprecated
    public final void putAll(Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException();
    }

    /**
     * <b>Unsupported</b>: {@link Configurations} instances are unmodifiable.
     *
     * @deprecated Do not use, warn the compiler.
     */
    @Override @Deprecated
    public final void clear() {
        throw new UnsupportedOperationException();
    }
}
