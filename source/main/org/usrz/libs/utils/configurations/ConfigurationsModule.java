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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import org.usrz.libs.utils.inject.AbstractModuleSupport;
import org.usrz.libs.utils.inject.AnnotatedBindingBuilderWrapper;
import org.usrz.libs.utils.inject.Isolate;
import org.usrz.libs.utils.inject.LinkedBindingBuilderWrapper;
import org.usrz.libs.utils.inject.ScopedBindingBuilderWrapper;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Names;

public abstract class ConfigurationsModule extends AbstractModuleSupport<Binder> {

    private final List<Isolate<?>> isolates = new ArrayList<>();

    protected ConfigurationsModule() {
        super((binder) -> binder);
    }

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        for (Isolate<?> isolate: isolates) bindIsolate(binder, isolate);

    }
    private <T> void bindIsolate(Binder binder, Isolate<T> isolate) {
        final Key<T> key = isolate.getKey();
        final Provider<T> provider = isolate.getProvider();
        binder.requestInjection(provider);
        binder.bind(key).toProvider(provider);
    }

    @Override
    protected final <T> ConfigurableAnnotatedBindingBuilder<T> bind(Class<T> type) {
        return new ConfigurableAnnotatedBindingBuilderImpl<>(record(new Isolate<>(type)));
    }

    @Override
    protected final <T> ConfigurableAnnotatedBindingBuilder<T> bind(TypeLiteral<T> type) {
        return new ConfigurableAnnotatedBindingBuilderImpl<>(record(new Isolate<>(type)));
    }

    @Override
    protected final <T> ConfigurableLinkedBindingBuilder<T> bind(Key<T> key) {
        return new ConfigurableLinkedBindingBuilderImpl<>(record(new Isolate<>(key)));
    }

    private <T> Isolate<T> record(Isolate<T> isolate) {
        isolates.add(isolate);
        return isolate;
    }

    /* ====================================================================== */

    private static class Configurator implements Module {

        private final Configurations configurations;

        private Configurator(Configurations configurations) {
            this.configurations = Objects.requireNonNull(configurations, "Null configurations");
        }

        @Override
        public void configure(Binder binder) {
            binder.bind(Configurations.class).toInstance(configurations);
            for (Entry<String, String> entry: configurations.entrySet()) {
                binder.bindConstant().annotatedWith(Names.named(entry.getKey())).to(entry.getValue());
            }
        }

    }

    /* ====================================================================== */

    private static class ConfigurableAnnotatedBindingBuilderImpl<T>
    extends AnnotatedBindingBuilderWrapper<T,
                                           ConfigurableLinkedBindingBuilder<T>,
                                           ConfigurableScopedBindingBuilder>
    implements ConfigurableAnnotatedBindingBuilder<T> {

        private final Isolate<T> isolate;

        private ConfigurableAnnotatedBindingBuilderImpl(Isolate<T> isolate) {
            super(isolate);
            this.isolate = isolate;
        }

        @Override
        public AnnotatedBindingBuilder<T> withConfigurations(Configurations configurations) {
            isolate.install(new Configurator(configurations));
            return isolate;
        }

        @Override
        protected ConfigurableLinkedBindingBuilder<T> newLinkedBindingBuilder(LinkedBindingBuilder<T> builder) {
            return new ConfigurableLinkedBindingBuilderImpl<T>(isolate);
        }

        @Override
        protected ConfigurableScopedBindingBuilder newScopedBindingBuilder(ScopedBindingBuilder builder) {
            return new ConfigurableScopedBindingBuilderImpl(isolate);
        }

    }

    /* ====================================================================== */

    private static class ConfigurableLinkedBindingBuilderImpl<T>
    extends LinkedBindingBuilderWrapper<T, ConfigurableScopedBindingBuilder>
    implements ConfigurableLinkedBindingBuilder<T> {

        private final Isolate<T> isolate;

        private ConfigurableLinkedBindingBuilderImpl(Isolate<T> isolate) {
            super(isolate);
            this.isolate = isolate;
        }

        @Override
        public LinkedBindingBuilder<T> withConfigurations(Configurations configurations) {
            isolate.install(new Configurator(configurations));
            return isolate;
        }

        @Override
        protected ConfigurableScopedBindingBuilder newScopedBindingBuilder(ScopedBindingBuilder builder) {
            return new ConfigurableScopedBindingBuilderImpl(isolate);
        }

    }

    /* ====================================================================== */

    private static class ConfigurableScopedBindingBuilderImpl
    extends ScopedBindingBuilderWrapper
    implements ConfigurableScopedBindingBuilder {

        private final Isolate<?> isolate;

        private ConfigurableScopedBindingBuilderImpl(Isolate<?> isolate) {
            super(isolate);
            this.isolate = isolate;
        }

        @Override
        public ScopedBindingBuilder withConfigurations(Configurations configurations) {
            isolate.install(new Configurator(configurations));
            return isolate;
        }

    }

}
