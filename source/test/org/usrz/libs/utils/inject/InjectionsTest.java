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

import java.net.URL;

import javax.inject.Named;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.utils.inject.Injections;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.name.Names;

public class InjectionsTest extends AbstractTest {

    @Test
    public void testInjectionsAnnotated() {
        final Injector injector = Guice.createInjector((binder) -> {
            binder.bind(String.class).annotatedWith(Names.named("foo")).toInstance("annotationInstance");
            binder.bind(String.class).annotatedWith(Named.class).toInstance("annotationType");
        });

        assertEquals(Injections.getInstance(injector, String.class, Names.named("foo")), "annotationInstance");
        assertEquals(Injections.getInstance(injector, String.class, Named.class), "annotationType");
    }

    @Test
    public void testInjectionsNotAnnotated() {
        final Injector injector = Guice.createInjector((binder) -> {
            binder.bind(String.class).toInstance("value");
        });

        assertEquals(Injections.getInstance(injector, String.class, Names.named("foo")), "value");
        assertEquals(Injections.getInstance(injector, String.class, Named.class), "value");
    }

    @Test
    public void testInjectionsOptional() {
        final Injector injector = Guice.createInjector();
        assertNull(Injections.getInstance(injector, String.class, Names.named("foo"), true));
        assertNull(Injections.getInstance(injector, String.class, Named.class, true));
    }

    @Test(expectedExceptions=ProvisionException.class,
          expectedExceptionsMessageRegExp="(?s).*Unable to find or create binding.*Named\\(value=foo\\).*")
    public void testInjectionsRequired1() {
        final Injector injector = Guice.createInjector();
        Injections.getInstance(injector, URL.class, Names.named("foo"), false);
        fail("No exception");
    }

    @Test(expectedExceptions=ProvisionException.class,
          expectedExceptionsMessageRegExp="(?s).*Unable to find or create binding.*Named.*")
    public void testInjectionsRequired2() {
        final Injector injector = Guice.createInjector();
        Injections.getInstance(injector, URL.class, Named.class, false);
        fail("No exception");
    }

    @Test(expectedExceptions=ProvisionException.class,
          expectedExceptionsMessageRegExp="(?s).*Unable to find or create binding.*Named\\(value=foo\\).*")
      public void testInjectionsRequired3() {
          final Injector injector = Guice.createInjector();
          Injections.getInstance(injector, URL.class, Names.named("foo"));
          fail("No exception");
      }

      @Test(expectedExceptions=ProvisionException.class,
          expectedExceptionsMessageRegExp="(?s).*Unable to find or create binding.*Named.*")
      public void testInjectionsRequired4() {
          final Injector injector = Guice.createInjector();
          Injections.getInstance(injector, URL.class, Named.class);
          fail("No exception");
      }
}
