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
package org.usrz.libs.utils.caches;

import java.lang.reflect.Type;

import org.usrz.libs.utils.inject.ModuleSupport;

import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.util.Types;

public abstract class CacheModule extends ModuleSupport {

    public <K, V> CacheBuilder<K, V> bind(Class<K> key, Class<V> value) {
        final Type type = Types.newParameterizedType(Cache.class, key, value);
        @SuppressWarnings("unchecked")
        final TypeLiteral<Cache<K, V>> literal = (TypeLiteral<Cache<K, V>>) TypeLiteral.get(type);
        return new CacheBuilder<K, V>(binder().bind(literal), key, value);
    }

    public class CacheBuilder<K, V> {

        private final AnnotatedBindingBuilder<Cache<K, V>> builder;
        private final Class<K> key;
        private final Class<V> value;

        private CacheBuilder(AnnotatedBindingBuilder<Cache<K, V>> builder, Class<K> key, Class<V> value) {
            this.builder = builder;
            this.key = key;
            this.value = value;
        }

        public <C extends Cache<?, ?>> void to(Class<C> cache) {
            final Type type = Types.newParameterizedType(cache, key, value);
            @SuppressWarnings("unchecked")
            final TypeLiteral<? extends Cache<K, V>> literal = (TypeLiteral<? extends Cache<K, V>>) TypeLiteral.get(type);
            builder.to(literal);
        }
    }

}
