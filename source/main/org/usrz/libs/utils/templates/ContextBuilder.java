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
package org.usrz.libs.utils.templates;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A simple <em>builder</em> to create {@link Map}s suitable for
 * {@linkplain Template#merge(Map) merging} {@link Template}s.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class ContextBuilder {

    private final Map<String, String> context = new HashMap<>();

    /**
     * Default constructor.
     */
    public ContextBuilder() {
        /* Nothing to do */
    }

    /**
     * Single mapping constructor.
     *
     * This is equivalent to <code>new ContextBuilder().with(k, v);</code>.
     */
    public ContextBuilder(String key, Object value) {
        with(key, value);
    }

    /**
     * Associate a specified <em>value</em> to the given <em>key</em>.
     */
    public ContextBuilder with(String key, Object value) {
        if (key == null) throw new NullPointerException("Null key");
        if (key.length() == 0) throw new IllegalArgumentException("Empty key");
        context.put(key, value.toString());
        return this;
    }

    /**
     * Copy all the mappings from the specified {@link Map}.
     */
    public ContextBuilder with(Map<String, ?> map) {
        for (Entry<String, ?> entry: map.entrySet())
            this.with(entry.getKey(), entry.getValue().toString());
        return this;
    }

    /**
     * Return a {@link Map} suitable for
     * {@linkplain Template#merge(Map) merging} {@link Template}s.
     */
    public Map<String, String> build() {
        return context;
    }

    /**
     * Merge the specified {link Template} with the key/value mappings
     * contained by this instance.
     */
    public String merge(Template template)
    throws IOException {
        return template.merge(context);
    }

    /**
     * Merge the specified {link Template} with the key/value mappings
     * contained by this instance.
     */
    public OutputStream merge(Template template, OutputStream output)
    throws IOException {
        return template.merge(context, output);
    }

    /**
     * Merge the specified {link Template} with the key/value mappings
     * contained by this instance.
     */
    public Writer merge(Template template, Writer writer)
    throws IOException {
        return template.merge(context, writer);
    }
}
