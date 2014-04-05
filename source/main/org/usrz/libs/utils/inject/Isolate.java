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
import java.lang.reflect.Constructor;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import org.usrz.libs.utils.configurations.Configurations;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Names;

public class Isolate<T>
implements AnnotatedBindingBuilder<T>,
           LinkedBindingBuilder<T>,
           ScopedBindingBuilder {

    private Key<T> key;

    private Key<? extends T> target;
    private T instance;
    private Provider<? extends T> provider;
    private Key<? extends javax.inject.Provider<? extends T>> providerKey;
    private Entry<Constructor<T>, TypeLiteral<? extends T>> constructor;

    private boolean scopingEagerSingleton = false;
    private Class<? extends Annotation> scopingAnnotation;
    private Scope scopingScope;

    private final List<Module> modules = new ArrayList<>();

    /* ====================================================================== */

    public Isolate(Class<T> type) {
        this.key = Key.get(Objects.requireNonNull(type, "Null class"));
    }

    public Isolate(TypeLiteral<T> type) {
        this.key = Key.get(Objects.requireNonNull(type, "Null type literal"));
    }

    public Isolate(Key<T> key) {
        this.key = Objects.requireNonNull(key, "Null key");
    }

    /* ====================================================================== */

    @Override
    public LinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotation) {
        key = Key.get(key.getTypeLiteral(), Objects.requireNonNull(annotation, "Null annotation"));
        return this;
    }

    @Override
    public LinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
        key = Key.get(key.getTypeLiteral(), Objects.requireNonNull(annotation, "Null annotation"));
        return this;
    }

    /* ====================================================================== */

    @Override
    public void toInstance(T instance) {
        this.instance = Objects.requireNonNull(instance, "Null instance");
    }

    /* ====================================================================== */

    @Override
    public ScopedBindingBuilder to(Class<? extends T> implementation) {
        return this.to(Key.get(Objects.requireNonNull(implementation, "Null class")));
    }

    @Override
    public ScopedBindingBuilder to(TypeLiteral<? extends T> implementation) {
        return this.to(Key.get(Objects.requireNonNull(implementation, "Null class")));
    }

    @Override
    public ScopedBindingBuilder to(Key<? extends T> targetKey) {
        this.target = Objects.requireNonNull(targetKey, "Null key");
        return this;
    }

    /* ====================================================================== */

    @Override
    public ScopedBindingBuilder toProvider(Provider<? extends T> provider) {
        this.provider = Objects.requireNonNull(provider, "Null provider");
        return this;
    }

    /* ====================================================================== */

    @Override
    public ScopedBindingBuilder toProvider(Class<? extends javax.inject.Provider<? extends T>> providerType) {
        return this.toProvider(Key.get(Objects.requireNonNull(providerType, "Null class")));
    }

    @Override
    public ScopedBindingBuilder toProvider(TypeLiteral<? extends javax.inject.Provider<? extends T>> providerType) {
        return this.toProvider(Key.get(Objects.requireNonNull(providerType, "Null type literal")));
    }

    @Override
    public ScopedBindingBuilder toProvider(Key<? extends javax.inject.Provider<? extends T>> providerKey) {
        this.providerKey = Objects.requireNonNull(providerKey, "Null key");
        return this;
    }

    /* ====================================================================== */

    @Override
    public <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor) {
        return toConstructor(constructor, TypeLiteral.get(constructor.getDeclaringClass()));
    }

    @Override @SuppressWarnings("unchecked")
    public <S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor, TypeLiteral<? extends S> type) {
        this.constructor = new SimpleImmutableEntry<>((Constructor<T>) constructor, type);
        return this;
    }

    /* ====================================================================== */

    @Override
    public void asEagerSingleton() {
        this.scopingEagerSingleton = true;
    }

    @Override
    public void in(Class<? extends Annotation> annotation) {
        this.scopingAnnotation = Objects.requireNonNull(annotation, "Null class");
    }

    @Override
    public void in(Scope scope) {
        this.scopingScope = Objects.requireNonNull(scope, "Null scope");
    }

    /* ====================================================================== */

    public Isolate<T> install(Module... modules) {
        for (Module module: Objects.requireNonNull(modules, "Null modules"))
            this.modules.add(Objects.requireNonNull(module, "Null module"));
        return this;
    }

    /* ====================================================================== */

    public Key<T> getKey() {
        return this.key;
    }

    private Key<T> getIsolatedKey() {
        return Key.get(this.key.getTypeLiteral(), Names.named("ISOLATED"));
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {

            /* Install all isolated modules */
            //for (Module module: modules) install(module);

            /* Bind the key */
            final Key<T> key = getIsolatedKey();
            System.err.println("BINDING -> " + key);
            final LinkedBindingBuilder<T> linked = bind(key);

            /* Bind the target */
            final ScopedBindingBuilder scoped;
            if (instance != null) {
                linked.toInstance(instance);
                scoped = linked;
            } else if (target != null) {
                scoped = linked.to(target);
            } else if (provider != null) {
                scoped = linked.toProvider(provider);
            } else if (providerKey != null) {
                scoped = linked.toProvider(providerKey);
            } else if (constructor != null) {
                scoped = linked.toConstructor(constructor.getKey(),
                                              constructor.getValue());
            } else {
                scoped = linked;
            }

            /* Binding scope */
            if (scopingEagerSingleton == true) {
                scoped.asEagerSingleton();
            } else if (scopingAnnotation != null) {
                scoped.in(scopingAnnotation);
            } else if (scopingScope != null) {
                scoped.in(scopingScope);
            }

        }};
    }

    public Provider<T> getProvider() {
        return new InjectorProvider<T>() {

            @Override
            protected T get(Injector injector) {
                final Key<T> key = getIsolatedKey();
                System.err.println("FETCHING1 -> " + key);
                try {
                    final Injector child = injector.createChildInjector(new AbstractModule() {
                        @Override
                        public void configure() {
                            this.bind(Configurations.class).toInstance(Configurations.EMPTY_CONFIGURATIONS);
                            this.bind(key); //.to(null);
                        }
                    });
                } catch (RuntimeException exception) {
                    System.err.println("FETCHING2 -> " + key);
                    exception.printStackTrace();
                    throw exception;
                }
                return null;
                //return child.getInstance(key);
            }

        };
    }

    /* ====================================================================== */

    @Retention(RUNTIME)
    @BindingAnnotation
    private @interface Isolated {}

}
