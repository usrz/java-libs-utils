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

import java.lang.reflect.Constructor;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;

public abstract class LinkedBindingBuilderWrapper<T,
                                                  S extends ScopedBindingBuilder>
extends ScopedBindingBuilderWrapper
implements LinkedBindingBuilder<T> {

    protected final LinkedBindingBuilder<T> builder;

    protected LinkedBindingBuilderWrapper(LinkedBindingBuilder<T> builder) {
        super(builder);
        this.builder = builder;
    }

    /* ====================================================================== */

    protected abstract S newScopedBindingBuilder(ScopedBindingBuilder builder);

    /* ====================================================================== */

    @Override
    public final S to(Class<? extends T> implementation) {
        return newScopedBindingBuilder(builder.to(implementation));
    }

    @Override
    public final S to(TypeLiteral<? extends T> implementation) {
        return newScopedBindingBuilder(builder.to(implementation));
    }

    @Override
    public final S to(Key<? extends T> targetKey) {
        return newScopedBindingBuilder(builder.to(targetKey));
    }

    @Override
    public final void toInstance(T instance) {
        builder.toInstance(instance);
    }

    @Override
    public final S toProvider(Provider<? extends T> provider) {
        return newScopedBindingBuilder(builder.toProvider(provider));
    }

    @Override
    public final S toProvider(Class<? extends javax.inject.Provider<? extends T>> providerType) {
        return newScopedBindingBuilder(builder.toProvider(providerType));
    }

    @Override
    public final S toProvider(TypeLiteral<? extends javax.inject.Provider<? extends T>> providerType) {
        return newScopedBindingBuilder(builder.toProvider(providerType));
    }

    @Override
    public final S toProvider(Key<? extends javax.inject.Provider<? extends T>> providerKey) {
        return newScopedBindingBuilder(builder.toProvider(providerKey));
    }

    @Override
    public final <X extends T> S toConstructor(Constructor<X> constructor) {
        return newScopedBindingBuilder(builder.toConstructor(constructor));
    }

    @Override
    public final <X extends T> S toConstructor(Constructor<X> constructor, TypeLiteral<? extends X> type) {
        return newScopedBindingBuilder(builder.toConstructor(constructor, type));
    }

}
