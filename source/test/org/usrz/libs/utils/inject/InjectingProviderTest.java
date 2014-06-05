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
package org.usrz.libs.utils.inject;

import javax.inject.Inject;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.utils.Strings;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class InjectingProviderTest extends AbstractTest {

    @Test
    public void testInjectingProvider() {
        final String string = Strings.random(32);
        final Object object = new Object();

        final TestObject test = Guice.createInjector((binder) -> {
            binder.bind(String.class).toInstance(string);
            binder.bind(Object.class).toInstance(object);
            binder.bind(TestObject.class).toProvider(new InjectingProvider<TestObject>() {
                @Override protected TestObject get(Injector injector) throws Exception {
                    return new TestObject(injector.getInstance(String.class));
                }
            });
        }).getInstance(TestObject.class);

        assertSame(test.string, string, "Wrong string");
        assertSame(test.object, object, "Wrong object");

    }

    public static class TestObject {

        private final String string;
        @Inject private Object object;

        private TestObject(String string) {
            this.string = string;
        }
    }

}
