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

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.testing.IO;

public class JsonConfigurationsTest extends AbstractTest {

    @Test
    public void testJson()
    throws Exception {
        final Configurations configurations = new ConfigurationsBuilder()
                        .put("server.applications.0.name", "first")
                        .put("server.applications.0.value", 1)
                        .put("server.applications.1.name", "second")
                        .put("server.applications.1.value", 2)
                        .put("server.applications.2.name", "third")
                        .put("server.applications.2.value", 3)
                        .put("server.count.0", 1)
                        .put("server.count.1", 2)
                        .put("server.count.2", 3)
                        .put("server.dotted.key.foo", "bar")
                        .put("server.dotted.key.value", "some value")
                        .put("server.listener.name", "listname")
                        .put("server.listener.port", 8080)
                        .put("server.listener.secure", true)
                        .put("server.message", "hello world")
                        .put("mongo.foo", "bar")
                        .build();

        final Configurations json = new JsonConfigurations(IO.resource("test.json"));
        json.list(System.err);

        assertEquals(configurations, json);
    }

    @Test(expectedExceptions = ConfigurationsException.class,
          expectedExceptionsMessageRegExp = "^Invalid key name \\\"test.a wrong key\\\".*")
    public void testJsonWrongKey()
    throws Exception {
        new ResourceConfigurations("wrongkey.json").list(System.err);
        fail("Exception not thrown");
    }
}
