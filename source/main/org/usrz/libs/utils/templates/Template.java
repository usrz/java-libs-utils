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
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.usrz.libs.utils.templates.TemplateParser.Token;

/**
 * An immensely simple template.
 *
 * <p>Templates accept only three directives:</p>
 *
 * <ul>
 * <li><code><b>!{foo}</b></code>: merge the value associated with the key
 *     <code><em>foo</em></code> from the context. If the specified key is not
 *     present in the context, an exception will be thrown.</li>
 * <li><code><b>?{foo}</b></code>: optionally merge the value associated with
 *     the key <code><em>foo</em></code> from the context. If the specified key
 *     is not present in the context, this directive will be ignored.</li>
 * <li><code><b>@{foo}</b></code>: merge the sub-template identifed by the
 *     {@link URI} <code><em>foo</em></code>.</li>
 * </ul>
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class Template {

    private static final Charset UTF8 = Charset.forName("UTF8");

    private final URI uri;
    private final long lastModified;
    private final List<Token> tokens;

    protected Template(URI uri, long lastModified, List<Token> tokens) {
        this.lastModified = lastModified;
        this.tokens = tokens;
        this.uri = uri;
    }

    /**
     * Return the {@link URI} of this template or <em>null</em> if this instance
     * was {@linkplain Templates#create(String) created from a string}.
     */
    public URI getURI() {
        return uri;
    }

    /**
     * Return the last modification date of this template (if known) or zero.
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * Merge this template with the mappings specified in the given context
     * and return a {@link String}.
     */
    public String merge(Map<String, String> context)
    throws IOException {
        final StringWriter writer = new StringWriter();
        this.merge(context, writer);
        return writer.toString();
    }

    /**
     * Merge this template with the mappings specified in the given context
     * into the specified {@link OutputStream}.
     *
     * <p>Note that the encoding used will <em>always</em> be <em>UTF8</em>.</p>
     */
    public OutputStream  merge(Map<String, String> context, OutputStream output)
    throws IOException {
        final OutputStreamWriter writer = new OutputStreamWriter(output, UTF8);
        this.merge(context, writer);
        return output;
    }

    /**
     * Merge this template with the mappings specified in the given context
     * into the specified {@link Writer}.
     */
    public Writer merge(Map<String, String> context, Writer writer)
    throws IOException {
        for (Token token: tokens) token.merge(context, writer);
        writer.flush();
        return writer;
    }
}
