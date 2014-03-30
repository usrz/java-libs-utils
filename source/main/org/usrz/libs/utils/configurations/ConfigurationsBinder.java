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

import java.util.Map.Entry;
import java.util.Objects;

import org.usrz.libs.utils.inject.AnnotatedBindingBuilderWrapper;
import org.usrz.libs.utils.inject.BinderWrapper;
import org.usrz.libs.utils.inject.LinkedBindingBuilderWrapper;
import org.usrz.libs.utils.inject.ScopedBindingBuilderWrapper;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.PrivateBinder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Names;

public class ConfigurationsBinder extends BinderWrapper {

    public ConfigurationsBinder(Binder binder) {
        super(binder);
    }

    /* ====================================================================== */

    @Override
    public final <T> ConfigurableAnnotatedBindingBuilder<T> bind(Class<T> type) {
        return new ConfigurableAnnotatedBindingBuilderImpl<T>(newPrivateBinder(), type);
    }

    @Override
    public final <T> ConfigurableAnnotatedBindingBuilder<T> bind(TypeLiteral<T> type) {
        return new ConfigurableAnnotatedBindingBuilderImpl<T>(newPrivateBinder(), type);
    }

    @Override
    public final <T> ConfigurableLinkedBindingBuilder<T> bind(Key<T> key) {
        return new ConfigurableLinkedBindingBuilderImpl<T>(newPrivateBinder(), key);
    }

    /* ====================================================================== */

    private static void bindConfigurations(PrivateBinder binder, Configurations configurations) {
        binder.bind(Configurations.class).toInstance(Objects.requireNonNull(configurations, "Null configurations"));
        for (Entry<String, String> entry: configurations.entrySet()) {
            binder.bindConstant().annotatedWith(Names.named(entry.getKey())).to(entry.getValue());
        }
    }

    /* ====================================================================== */

    private static class ConfigurableAnnotatedBindingBuilderImpl<T>
    extends AnnotatedBindingBuilderWrapper<T,
                                           ConfigurableLinkedBindingBuilder<T>,
                                           ConfigurableScopedBindingBuilder>
    implements ConfigurableAnnotatedBindingBuilder<T> {

        private final PrivateBinder binder;

        private ConfigurableAnnotatedBindingBuilderImpl(PrivateBinder binder, Class<T> type) {
            super(binder.bind(type));
            binder.expose(type);
            this.binder = binder;
        }

        private ConfigurableAnnotatedBindingBuilderImpl(PrivateBinder binder, TypeLiteral<T> type) {
            super(binder.bind(type));
            binder.expose(type);
            this.binder = binder;
        }

        @Override
        public AnnotatedBindingBuilder<T> withConfigurations(Configurations configurations) {
            bindConfigurations(binder, configurations);
            return builder;
        }

        @Override
        protected ConfigurableLinkedBindingBuilder<T> newLinkedBindingBuilder(LinkedBindingBuilder<T> builder) {
            return new ConfigurableLinkedBindingBuilderImpl<T>(binder, builder);
        }

        @Override
        protected ConfigurableScopedBindingBuilder newScopedBindingBuilder(ScopedBindingBuilder builder) {
            return new ConfigurableScopedBindingBuilderImpl(binder, builder);
        }

    }

    /* ====================================================================== */

    private static class ConfigurableLinkedBindingBuilderImpl<T>
    extends LinkedBindingBuilderWrapper<T, ConfigurableScopedBindingBuilder>
    implements ConfigurableLinkedBindingBuilder<T> {

        private final PrivateBinder binder;

        private ConfigurableLinkedBindingBuilderImpl(PrivateBinder binder, Key<T> key) {
            super(binder.bind(key));
            binder.expose(key);
            this.binder = binder;
        }

        private ConfigurableLinkedBindingBuilderImpl(PrivateBinder binder, LinkedBindingBuilder<T> builder) {
            super(builder);
            this.binder = binder;
        }

        @Override
        public LinkedBindingBuilder<T> withConfigurations(Configurations configurations) {
            bindConfigurations(binder, configurations);
            return builder;
        }

        @Override
        protected ConfigurableScopedBindingBuilder newScopedBindingBuilder(ScopedBindingBuilder builder) {
            return new ConfigurableScopedBindingBuilderImpl(binder, builder);
        }

    }

    /* ====================================================================== */

    private static class ConfigurableScopedBindingBuilderImpl
    extends ScopedBindingBuilderWrapper
    implements ConfigurableScopedBindingBuilder {

        private final PrivateBinder binder;

        private ConfigurableScopedBindingBuilderImpl(PrivateBinder binder, ScopedBindingBuilder builder) {
            super(builder);
            this.binder = binder;
        }

        @Override
        public ScopedBindingBuilder withConfigurations(Configurations configurations) {
            bindConfigurations(binder, configurations);
            return builder;
        }

    }
}
