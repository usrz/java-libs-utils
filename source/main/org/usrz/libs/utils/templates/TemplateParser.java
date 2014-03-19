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
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A static parser for simple {@link Template}s.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public final class TemplateParser {

    private static final Pattern START = Pattern.compile("[@?!]\\{");

    private TemplateParser() {
        throw new IllegalStateException("Do not construct");
    }

    /* ====================================================================== */

    /**
     * Define a simple {@linkplain Template#merge(Map) mergeable} component
     * which can be parsed out of a template.
     *
     * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
     */
    public static interface Token {

        public void merge(Map<String, String> context, Writer writer)
        throws IOException;

    }

    /* ====================================================================== */

    /**
     * Parse the specified {@link String} as a {@link List} of {@link Token}s
     * to be used while {@linkplain Template#merge(Map) merging} templates.
     */
    public static final List<Token> parse(Templates templates, URI uri, String template) {
        final List<Token> tokens = new ArrayList<>();

        int position = 0;
        while (position < template.length()) {
            final Matcher matcher = START.matcher(template);
            if (matcher.find(position)) {
                final int start = matcher.start();

                if (start > position) {
                    tokens.add(new StringToken(template.substring(position, start)));
                }

                final int end = template.indexOf('}', start + 2);
                if (end < 0) throw new IllegalArgumentException("Unterminated expression");

                final String content = template.substring(start + 2, end);
                switch (template.charAt(start)) {
                    case '@': tokens.add(new IncludeToken(content, templates, uri)); break;
                    case '?': tokens.add(new ContextToken(content, false)); break;
                    case '!': tokens.add(new ContextToken(content, true)); break;
                    default: throw new IllegalStateException("Invalid character '" + content.charAt(start) + "'");
                }

                position = end + 1;
            } else {
                tokens.add(new StringToken(template.substring(position)));
                break;
            }
        }

        return tokens;
    }

    /* ====================================================================== */

    private static class ContextToken implements Token {

        private final String key;
        private final boolean optional;

        protected ContextToken(String key, boolean optional) {
            this.key = key;
            this.optional = optional;
        }

        @Override
        public void merge(Map<String, String> context, Writer writer)
        throws IOException {
            final Object value = context.get(key);
            if (value != null) {
                writer.write(value.toString());
                return;
            }

            if (value == null) {
                if (optional) return;
                if (context.containsKey(key)) return;
                throw new IllegalStateException("No value for context key \"" + key + "\"");
            }
        }
    }

    /* ====================================================================== */

    private static class IncludeToken  implements Token {

        private final Templates templates;
        private final String template;
        private final URI uri;

        protected IncludeToken(String template, Templates templates, URI uri) {
            this.templates = templates;
            this.template = template;
            this.uri = uri;
        }

        @Override
        public void merge(Map<String, String> context, Writer writer)
        throws IOException {
            /* String templates */
            if (uri == null) {
                templates.getTemplate(template).merge(context, writer);
            } else {
                final URI location = uri.resolve(template);
                templates.getTemplate(location).merge(context, writer);
            }
        }
    }

    /* ====================================================================== */

    private static class StringToken implements Token {

        private final String value;

        protected StringToken(String value) {
            this.value = value;
        }

        @Override
        public void merge(Map<String, String> context, Writer writer)
        throws IOException {
            writer.write(value);
        }

    }

}
