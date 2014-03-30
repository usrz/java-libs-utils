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

import javax.inject.Named;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ProvidedBy;

@SuppressWarnings("restriction")
public class ConfigurationsBinderTestWithProviders extends AbstractTest {

    @Test
    public void testConfigurationsBinder() {
        final Configurations configA = new ConfigurationsBuilder().put("foo", "bar").build();
        final Configurations configB = new ConfigurationsBuilder().put("foo", "baz").build();

        final Injector injector = Guice.createInjector(new ConfigurationsModule() {

            @Override
            protected void configure() {
                this.bind(ClassA.class).withConfigurations(configA).asEagerSingleton();;
                this.bind(ClassB.class).withConfigurations(configB).asEagerSingleton();;
            }

        });

        final ClassA instanceA = injector.getInstance(ClassA.class);
        final ClassB instanceB = injector.getInstance(ClassB.class);

        assertEquals(instanceA.getFooString(), "bar");
        assertEquals(instanceB.getFooString(), "baz");
    }

    @Test
    public void testConfigurationsBinderWithNoParams() {
        final Injector injector = Guice.createInjector(new ConfigurationsModule() {

            @Override
            protected void configure() {
                this.bind(ClassA.class); // the annotation determines
                this.bind(ClassB.class); // the class of the provider
            }

        });

        final ClassA instanceA = injector.getInstance(ClassA.class);
        final ClassB instanceB = injector.getInstance(ClassB.class);

        assertEquals(instanceA.getFooString(), null);
        assertEquals(instanceB.getFooString(), null);

    }

    /* ====================================================================== */

    @ProvidedBy(ProviderA.class)
    public static final class ClassA {

        private final String foo;

        private ClassA(String foo) {
            this.foo = foo;
        }

        public String getFooString() {
            return foo;
        }
    }

    public static final class ProviderA extends ConfiguredProvider<ClassA> {

        private String foo = null;

        @Inject(optional=true)
        private void setFooString(@Named("foo") String foo) {
            this.foo = Objects.requireNonNull(foo, "Null foo for ClassA");
        }

        @Override
        public ClassA get() {
            return new ClassA(foo);
        }
    }

    /* ====================================================================== */

    @ProvidedBy(ProviderB.class)
    public static final class ClassB {

        private final String foo;

        private ClassB(String foo) {
            this.foo = foo;
        }

        public String getFooString() {
            return foo;
        }
    }

    public static final class ProviderB extends ConfiguredProvider<ClassB> {

        private String foo = null;

        @Inject(optional=true)
        private void setFooString(@Named("foo") String foo) {
            this.foo = Objects.requireNonNull(foo, "Null foo for ClassB");
        }

        @Override
        public ClassB get() {
            return new ClassB(foo);
        }
    }

}
