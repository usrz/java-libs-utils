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

public interface LinkedBindingBuilderWithTypes<T, S extends ScopedBindingBuilder>
extends LinkedBindingBuilder<T> {

    @Override
    public S to(Class<? extends T> implementation);

    @Override
    public S to(TypeLiteral<? extends T> implementation);

    @Override
    public S to(Key<? extends T> targetKey);

    @Override
    public void toInstance(T instance);

    @Override
    public S toProvider(Provider<? extends T> provider);

    @Override
    public S toProvider(Class<? extends javax.inject.Provider<? extends T>> providerType);

    @Override
    public S toProvider(TypeLiteral<? extends javax.inject.Provider<? extends T>> providerType);

    @Override
    public S toProvider(Key<? extends javax.inject.Provider<? extends T>> providerKey);

    @Override
    public <X extends T> S toConstructor(Constructor<X> constructor);

    @Override
    public <X extends T> S toConstructor(Constructor<X> constructor, TypeLiteral<? extends X> type);

}
