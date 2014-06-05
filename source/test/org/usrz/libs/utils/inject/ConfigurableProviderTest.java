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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;

import org.testng.annotations.Test;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.ConfigurationsBuilder;
import org.usrz.libs.testing.AbstractTest;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class ConfigurableProviderTest extends AbstractTest {

    @Test
    public void testUnannotatedOverridden() {
        final TestObject instance = Guice.createInjector(new TestModule(),
                (binder) -> binder.bind(TestObject.class).toProvider(new TestProvider(new ConfigurationsBuilder().put("foo", "override").build()))
            ).getInstance(TestObject.class);
        assertEquals(instance.getString(), "override");
    }

    @Test
    public void testAnnotationType() {
        final TestObject instance = Guice.createInjector(new TestModule(),
                (binder) -> binder.bind(TestObject.class).toProvider(new TestProvider(TestAnnotation.class))
            ).getInstance(TestObject.class);
        assertEquals(instance.getString(), "withAnnotationType");
    }

    @Test
    public void testAnnotationTypeUnbound() {
        final TestObject instance = Guice.createInjector(new TestModule(),
                (binder) -> binder.bind(TestObject.class).toProvider(new TestProvider(Named.class))
            ).getInstance(TestObject.class);
        assertEquals(instance.getString(), "defaultValue"); // not explicitly bound, empty config
    }

    @Test
    public void testAnnotation() {
        final TestObject instance = Guice.createInjector(new TestModule(),
                (binder) -> binder.bind(TestObject.class).toProvider(new TestProvider("named"))
            ).getInstance(TestObject.class);
        assertEquals(instance.getString(), "withAnnotation");
    }

    @Test
    public void testAnnotationUnbound() {
        final TestObject instance = Guice.createInjector(new TestModule(),
                (binder) -> binder.bind(TestObject.class).toProvider(new TestProvider("blah"))
            ).getInstance(TestObject.class);
        assertEquals(instance.getString(), "defaultValue"); // not explicitly bound, empty config
    }

    @Test
    public void testUnbound() {
        final TestObject instance = Guice.createInjector(
                (binder) -> binder.bind(TestObject.class).toProvider(new TestProvider("blah"))
            ).getInstance(TestObject.class);
        assertEquals(instance.getString(), "defaultValue");
    }

    @Qualifier
    @Retention(RUNTIME)
    public static @interface TestAnnotation {}

    public static class TestModule implements Module {

        @Override
        public void configure(Binder binder) {
            binder.bind(Configurations.class).annotatedWith(Names.named("named")).toInstance(new ConfigurationsBuilder().put("foo", "withAnnotation").build());
            binder.bind(Configurations.class).annotatedWith(TestAnnotation.class).toInstance(new ConfigurationsBuilder().put("foo", "withAnnotationType").build());
            binder.bind(Configurations.class).toInstance(new ConfigurationsBuilder().put("foo", "unannotated").build());
        }
    }

    public static class TestProvider extends ConfigurableProvider<TestObject> {

        private TestProvider(String name) {
            super(name);
        }

        private TestProvider(Annotation annotation) {
            super(annotation);
        }

        private TestProvider(Class<? extends Annotation> annotation) {
            super(annotation);
        }

        private TestProvider(Configurations configurations) {
            super(configurations);
        }

        @Override
        protected TestObject get(Configurations configurations) {
            return new TestObject(configurations);
        }

    }

    public static class TestObject {

        private final String string;
        private boolean injected = false;

        private TestObject(Configurations configurations) {
            string = configurations.get("foo", "defaultValue");
        }

        @Inject
        private void injectMe(Injector injector) {
            injected = true;
        }

        public String getString() {
            if (injected) return string;
            throw new IllegalStateException("Not injected");
        }
    }
}
