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
import java.util.Map;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class TemplateParserTest extends AbstractTest {

    @Test
    public void testParse()
    throws IOException {
        final Map<String, String> context = new ContextBuilder("bar", "value").build();
        final Templates templates = new Templates(getClass().getResource("include.txt"));

        assertEquals(templates.create("foobar").merge(context), "foobar");

        assertEquals(templates.create("foo!{bar}baz").merge(context), "foovaluebaz");
        assertEquals(templates.create("foo?{bar}baz").merge(context), "foovaluebaz");
        assertEquals(templates.create("foo@{include.txt}baz").merge(context), "fooincludedbaz");

        assertEquals(templates.create("!{bar}").merge(context), "value");
        assertEquals(templates.create("?{bar}").merge(context), "value");
        assertEquals(templates.create("@{include.txt}").merge(context), "included");

        assertEquals(templates.create("foo!{bar}").merge(context), "foovalue");
        assertEquals(templates.create("foo?{bar}").merge(context), "foovalue");
        assertEquals(templates.create("foo@{include.txt}").merge(context), "fooincluded");

        assertEquals(templates.create("!{bar}baz").merge(context), "valuebaz");
        assertEquals(templates.create("?{bar}baz").merge(context), "valuebaz");
        assertEquals(templates.create("@{include.txt}baz").merge(context), "includedbaz");
    }
}
