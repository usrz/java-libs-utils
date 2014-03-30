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
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
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
        final ConfigurationsProvider provider = new ConfigurationsProvider(newPrivateBinder());
        return new ConfigurableAnnotatedBindingBuilderImpl<T>(provider, type);
    }

    @Override
    public final <T> ConfigurableAnnotatedBindingBuilder<T> bind(TypeLiteral<T> type) {
        final ConfigurationsProvider provider = new ConfigurationsProvider(newPrivateBinder());
        return new ConfigurableAnnotatedBindingBuilderImpl<T>(provider, type);
    }

    @Override
    public final <T> ConfigurableLinkedBindingBuilder<T> bind(Key<T> key) {
        final ConfigurationsProvider provider = new ConfigurationsProvider(newPrivateBinder());
        return new ConfigurableLinkedBindingBuilderImpl<T>(provider, key);
    }

    /* ====================================================================== */

    private static void bindConfigurations(ConfigurationsProvider provider, Configurations configurations) {
        if (provider.configurations != Configurations.EMPTY_CONFIGURATIONS)
            throw new ProvisionException("Configurations already bound");

        provider.configurations = Objects.requireNonNull(configurations, "Null configurations");
        for (Entry<String, String> entry: configurations.entrySet()) {
            provider.binder.bindConstant().annotatedWith(Names.named(entry.getKey())).to(entry.getValue());
        }
    }

    /* ====================================================================== */

    private static class ConfigurationsProvider implements Provider<Configurations> {

        private Configurations configurations = Configurations.EMPTY_CONFIGURATIONS;
        private final PrivateBinder binder;

        private ConfigurationsProvider(PrivateBinder binder) {
            binder.bind(Configurations.class).toProvider(this);
            this.binder = binder;
        }

        @Override
        public Configurations get() {
            return configurations;
        }

    }

    /* ====================================================================== */

    private static class ConfigurableAnnotatedBindingBuilderImpl<T>
    extends AnnotatedBindingBuilderWrapper<T,
                                           ConfigurableLinkedBindingBuilder<T>,
                                           ConfigurableScopedBindingBuilder>
    implements ConfigurableAnnotatedBindingBuilder<T> {

        private final ConfigurationsProvider provider;

        private ConfigurableAnnotatedBindingBuilderImpl(ConfigurationsProvider provider, Class<T> type) {
            super(provider.binder.bind(type));
            provider.binder.expose(type);
            this.provider = provider;
        }

        private ConfigurableAnnotatedBindingBuilderImpl(ConfigurationsProvider provider, TypeLiteral<T> type) {
            super(provider.binder.bind(type));
            provider.binder.expose(type);
            this.provider = provider;
        }

        @Override
        public AnnotatedBindingBuilder<T> withConfigurations(Configurations configurations) {
            bindConfigurations(provider, configurations);
            return builder;
        }

        @Override
        protected ConfigurableLinkedBindingBuilder<T> newLinkedBindingBuilder(LinkedBindingBuilder<T> builder) {
            return new ConfigurableLinkedBindingBuilderImpl<T>(provider, builder);
        }

        @Override
        protected ConfigurableScopedBindingBuilder newScopedBindingBuilder(ScopedBindingBuilder builder) {
            return new ConfigurableScopedBindingBuilderImpl(provider, builder);
        }

    }

    /* ====================================================================== */

    private static class ConfigurableLinkedBindingBuilderImpl<T>
    extends LinkedBindingBuilderWrapper<T, ConfigurableScopedBindingBuilder>
    implements ConfigurableLinkedBindingBuilder<T> {

        private final ConfigurationsProvider provider;

        private ConfigurableLinkedBindingBuilderImpl(ConfigurationsProvider provider, Key<T> key) {
            super(provider.binder.bind(key));
            provider.binder.expose(key);
            this.provider = provider;
        }

        private ConfigurableLinkedBindingBuilderImpl(ConfigurationsProvider provider, LinkedBindingBuilder<T> builder) {
            super(builder);
            this.provider = provider;
        }

        @Override
        public LinkedBindingBuilder<T> withConfigurations(Configurations configurations) {
            bindConfigurations(provider, configurations);
            return builder;
        }

        @Override
        protected ConfigurableScopedBindingBuilder newScopedBindingBuilder(ScopedBindingBuilder builder) {
            return new ConfigurableScopedBindingBuilderImpl(provider, builder);
        }

    }

    /* ====================================================================== */

    private static class ConfigurableScopedBindingBuilderImpl
    extends ScopedBindingBuilderWrapper
    implements ConfigurableScopedBindingBuilder {

        private final ConfigurationsProvider provider;

        private ConfigurableScopedBindingBuilderImpl(ConfigurationsProvider provider, ScopedBindingBuilder builder) {
            super(builder);
            this.provider = provider;
        }

        @Override
        public ScopedBindingBuilder withConfigurations(Configurations configurations) {
            bindConfigurations(provider, configurations);
            return builder;
        }

    }
}
