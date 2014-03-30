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

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ConfigurationsBinderTest extends AbstractTest {

    @Test
    public void testConfigurationsBinder() {
        final Configurations configA = new ConfigurationsBuilder().put("foo", "bar").build();
        final Configurations configB = new ConfigurationsBuilder().put("foo", "baz").build();

        final Injector injector = Guice.createInjector(new ConfigurationsModule() {

            @Override
            protected void configure() {
                this.bind(ClassA.class).withConfigurations(configA);
                this.bind(ClassB.class).withConfigurations(configB);
            }

        });

        final ClassA instanceA = injector.getInstance(ClassA.class);
        final ClassB instanceB = injector.getInstance(ClassB.class);

        assertEquals(instanceA.getConfigurations().requireString("foo"), "bar");
        assertEquals(instanceB.getConfigurations().requireString("foo"), "baz");
        assertEquals(instanceA.getFooString(), "bar");
        assertEquals(instanceB.getFooString(), "baz");
    }

    public static final class ClassA extends MyClass {

        @Inject
        protected ClassA(Configurations configurations, @Named("foo") String foo) {
            super(configurations, foo);
        }

    }

    public static final class ClassB extends MyClass {

        @Inject
        protected ClassB(Configurations configurations, @Named("foo") String foo) {
            super(configurations, foo);
        }

    }

    public static class MyClass {

        private final Configurations configurations;
        private final String foo;

        protected MyClass(Configurations configurations, String foo) {
            this.configurations = Objects.requireNonNull(configurations, "Null configurations for " + this.getClass().getSimpleName());
            this.foo = Objects.requireNonNull(foo, "Null \"foo\" string");
        }

        public Configurations getConfigurations() {
            return configurations;
        }

        public String getFooString() {
            return foo;
        }
    }
}
