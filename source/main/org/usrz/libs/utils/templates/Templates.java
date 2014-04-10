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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.usrz.libs.logging.Log;
import org.usrz.libs.utils.templates.TemplateParser.Token;

/**
 * A manager and cache for {@link Template}s.
 *
 * <p>When resolving templates, {@link URI}s will always be resolved against
 * a base location specified at construction and <b>must</b> be relative to
 * it.</p>
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class Templates {

    private static final Charset UTF8 = Charset.forName("UTF8");
    private static final Log log = new Log();
    private final ConcurrentHashMap<URI, Template> cache = new ConcurrentHashMap<>();
    private final URI base;

    /**
     * Create a {@link Templates} instance rooted in the current directory.
     */
    public Templates() {
        this(".");
    }

    /**
     * Create a {@link Templates} instance rooted at the specified location.
     */
    public Templates(String base) {
        this(URI.create(base));
    }

    /**
     * Create a {@link Templates} instance rooted at the specified location.
     */
    public Templates(File base) {
        this(base.toURI());
    }

    /**
     * Create a {@link Templates} instance rooted at the specified location.
     */
    public Templates(URL base) {
        this(URI.create(base.toString()));
    }

    /**
     * Create a {@link Templates} instance rooted at the specified location.
     */
    public Templates(URI base) {
        final String userDir = System.getProperty("user.dir");
        try {
            this.base = new File(userDir).getCanonicalFile().toURI().resolve(base);
            log.info("Templates base URI: %s", this.base);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to get canonical path for \"user.dir\" " + userDir, exception);
        }
    }

    /* ====================================================================== */

    /**
     * Create a {@link Template} from a {@link String}.
     *
     * <p>The returned template will not be cached.</p>
     */
    public Template create(String template) {
        return new Template(null, 0, TemplateParser.parse(this, null, template));
    }

    /**
     * Create or return a cached copy of a {@link Template} parsed from the
     * specified location.
     */
    public Template getTemplate(String location)
    throws IOException  {
        return getTemplate(URI.create(location));
    }

    /**
     * Create or return a cached copy of a {@link Template} parsed from the
     * specified location.
     */
    public Template getTemplate(File location)
    throws IOException  {
        return getTemplate(location.toURI());
    }

    /**
     * Create or return a cached copy of a {@link Template} parsed from the
     * specified location.
     */
    public Template getTemplate(URL location)
    throws IOException {
        return getTemplate(URI.create(location.toString()));
    }

    /**
     * Create or return a cached copy of a {@link Template} parsed from the
     * specified location.
     */
    public Template getTemplate(URI location)
    throws IOException {
        final URI uri = base.resolve(location);
        if (base.relativize(uri).isAbsolute()) {
            throw new IllegalArgumentException("Template URI \"" + uri + "\" must be relative to \"" + base + "\"");
        }

        final URL url = uri.toURL();
        final URLConnection connection = url.openConnection();
        final long lastModified = connection.getHeaderFieldDate("Last-Modified", 0);

        if (lastModified <= 0) log.warn("No \"Last-Modified\" for template \"%s\"", location);

        final Template cached = cache.get(uri);
        if (cached == null) {
            log.info("Template \"%s\" never cached", uri);
        } else if (cached.getLastModified() < lastModified) {
            log.info("Template \"%s\" modified", uri);
        } else {
            log.trace("Returning cached template \"%s\"", uri);
            return cached;
        }

        final InputStream input = connection.getInputStream();
        final Reader reader = new InputStreamReader(input, UTF8);

        try {
            final StringBuilder builder = new StringBuilder();
            final char[] buffer = new char[4096];
            int read = -1;
            while ((read = reader.read(buffer)) >= 0) {
                if (read > 0) builder.append(buffer, 0, read);
            }

            final List<Token> tokens = TemplateParser.parse(this, uri, builder.toString());

            final Template template = new Template(uri, lastModified, tokens);
            cache.put(uri, template);
            return template;
        } finally {
            try {
                reader.close();
            } finally {
                input.close();
            }
        }
    }
}
