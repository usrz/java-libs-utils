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
package org.usrz.libs.utils;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.usrz.libs.utils.Check.notNull;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.util.UUID;

import javax.inject.Qualifier;

import org.usrz.libs.configurations.Configurations;

import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;

public final class Injections {

    private Injections() {
        throw new IllegalStateException("Do not construct");
    }

    /* ====================================================================== */

    private static <T> Key<T> key(TypeLiteral<T> type, Class<? extends Annotation> annotationType, Annotation annotation) {
        if (annotation != null) return Key.get(type, annotation);
        if (annotationType != null) return Key.get(type, annotationType);
        return Key.get(type);
    }

    /* ---------------------------------------------------------------------- */

    public static <T> T getInstance(Injector injector, Class<T> type, Class<? extends Annotation> annotationType) {
        return getInstance(injector, TypeLiteral.get(type), annotationType, false);
    }

    public static <T> T getInstance(Injector injector, Class<T> type, Annotation annotation) {
        return getInstance(injector, TypeLiteral.get(type), annotation, false);
    }

    public static <T> T getInstance(Injector injector, Class<T> type, Class<? extends Annotation> annotationType, boolean optional) {
        return getInstance(injector, TypeLiteral.get(type), annotationType, optional);
    }

    public static <T> T getInstance(Injector injector, Class<T> type, Annotation annotation, boolean optional) {
        return getInstance(injector, TypeLiteral.get(type), annotation, optional);
    }

    public static <T> T getInstance(Injector injector, TypeLiteral<T> type, Class<? extends Annotation> annotationType) {
        return getInstance(injector, key(type, annotationType, null), false);
    }

    public static <T> T getInstance(Injector injector, TypeLiteral<T> type, Annotation annotation) {
        return getInstance(injector, key(type, null, annotation), false);
    }

    public static <T> T getInstance(Injector injector, TypeLiteral<T> type, Class<? extends Annotation> annotationType, boolean optional) {
        return getInstance(injector, key(type, annotationType, null), optional);
    }

    public static <T> T getInstance(Injector injector, TypeLiteral<T> type, Annotation annotation, boolean optional) {
        return getInstance(injector, key(type, null, annotation), optional);
    }

    public static <T> T getInstance(Injector injector, Key<T> key) {
        return getInstance(injector, notNull(key, "Null key"), false);
    }

    public static <T> T getInstance(Injector injector, Key<T> key, boolean optional) {

        /* Fully annotated? */
        Binding<T> binding = injector.getExistingBinding(key);
        if (binding != null) return injector.getInstance(binding.getKey());

        /* Try to look up without attributes */
        if (key.hasAttributes()) {
            binding = injector.getExistingBinding(key.withoutAttributes());
            if (binding != null) return injector.getInstance(binding.getKey());
        }

        /* Do we have an annotation type? */
        if (key.getAnnotationType() != null) {
            binding = injector.getExistingBinding(Key.get(key.getTypeLiteral()));
            if (binding != null) return injector.getInstance(binding.getKey());
        }

        /*
         * Nothing found... Here we can do a couple of things, either fail
         * with the original key (that's what was asked of us) or try to create
         * a dynamic binding (we need to do so without annotations). Choice
         * is, "let's do both": try to create a dynamic binding or (if we
         * fail) wrap the exception and re-throw.
         */
        try {
            return optional ? null : injector.getInstance(Key.get(key.getTypeLiteral()));
        } catch (Exception exception) {
            throw new ProvisionException("Unable to find or create binding for " + key, exception);
        }

    }

    /* ====================================================================== */

    public static void configure(Binder binder, Configurations configurations) {
        binder.skipSources(Injections.class);
        binder.bind(Configurations.class).toInstance(configurations);
    }

    public static void configure(Binder binder, Annotation annotation, Configurations configurations) {
        binder.skipSources(Injections.class);
        binder.bind(Configurations.class).annotatedWith(annotation).toInstance(configurations);
    }

    public static void configure(Binder binder, Class<? extends Annotation> annotationType, Configurations configurations) {
        binder.skipSources(Injections.class);
        binder.bind(Configurations.class).annotatedWith(annotationType).toInstance(configurations);
    }

    /* ====================================================================== */

    public static Annotation unique() {
        return new UniqueImpl();
    }

    /* ---------------------------------------------------------------------- */

    @Qualifier
    @Documented
    @Retention(RUNTIME)
    private static @interface Unique {
        String value();
    }

    /* ---------------------------------------------------------------------- */

    @SuppressWarnings("all")
    private static class UniqueImpl implements Unique {

        private final String value;

        public UniqueImpl() {
            value = UUID.randomUUID().toString();
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public int hashCode() {
            return (127 * "value".hashCode()) ^ value.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) return true;
            if (object == null) return false;
            try {
                return value.equals(((Unique) object).value());
            } catch (ClassCastException exception) {
                return false;
            }
        }

        @Override
        public String toString() {
            return "@" + Unique.class.getName() + "(value=" + value + ")";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Unique.class;
        }
    }
}
