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

import static org.usrz.libs.configurations.Configurations.EMPTY_CONFIGURATIONS;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Provider;
import javax.inject.Qualifier;

import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.utils.Check;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * A simple provider which can be <em>configured</em>
 * <p>
 * By default, the {@link Configurations} {@linkplain #configurations accessible
 * from this} are gathered from an {@link Injector} keyed without any
 * {@link Qualifier} {@link Annotation}.
 * <p>
 * Concrete implementations of this class <b>must</b> override either the
 * {@link #get(Injector, Configurations) or the {@link #get(Configurations)}
 * method.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <T> The type of instances managed by this {@link Provider}.
 * @param <P> The actual type of this provider.
 */
public abstract class ConfigurableProvider<T>
extends InjectingProvider<T> {

    private final Key<Configurations> key;
    private Configurations configurations;

    /* ====================================================================== */

    /**
     * Create a new {@link ConfigurableProvider} instance.
     */
    protected ConfigurableProvider(String name) {
        this(Names.named(name));
    }

    /**
     * Create a new {@link ConfigurableProvider} instance.
     */
    protected ConfigurableProvider(String name, boolean singleton) {
        this(Names.named(name), singleton);
    }

    /**
     * Create a new {@link ConfigurableProvider} instance.
     */
    protected ConfigurableProvider(Annotation annotation) {
        super();
        key = Key.get(Configurations.class, annotation);
        configurations = null;
    }

    /**
     * Create a new {@link ConfigurableProvider} instance.
     */
    protected ConfigurableProvider(Annotation annotation, boolean singleton) {
        super(singleton);
        key = Key.get(Configurations.class, annotation);
        configurations = null;
    }

    /**
     * Create a new {@link ConfigurableProvider} instance.
     */
    protected ConfigurableProvider(Class<? extends Annotation> annotation) {
        super();
        key = Key.get(Configurations.class, annotation);
        configurations = null;
    }

    /**
     * Create a new {@link ConfigurableProvider} instance.
     */
    protected ConfigurableProvider(Class<? extends Annotation> annotation, boolean singleton) {
        super(singleton);
        key = Key.get(Configurations.class, annotation);
        configurations = null;
    }

    /**
     * Create a new {@link ConfigurableProvider} instance.
     */
    protected ConfigurableProvider(Configurations configurations) {
        super();
        key = null;
        this.configurations = Check.notNull(configurations, "Null configurations");
    }

    /**
     * Create a new {@link ConfigurableProvider} instance.
     */
    protected ConfigurableProvider(Configurations configurations, boolean singleton) {
        super(singleton);
        key = null;
        this.configurations = Check.notNull(configurations, "Null configurations");
    }

    /* ====================================================================== */

    protected final <X> Key<X> key(Class<X> type) {
        return key == null ? Key.get(type) : key.ofType(type);
    }

    protected final <X> Key<X> key(TypeLiteral<X> type) {
        return key == null ? Key.get(type) : key.ofType(type);
    }

    protected final Key<?> key(Type type) {
        return key == null ? Key.get(type) : key.ofType(type);
    }

    /* ====================================================================== */

    @Override
    public final T get(Injector injector)
    throws Exception {

        /* Get the configurations */
        if (this.configurations == null) {
            final Binding<Configurations> binding = injector.getExistingBinding(key);
            configurations = binding == null ? EMPTY_CONFIGURATIONS :
                             injector.getInstance(binding.getKey());
        }

        /* Get our instance */
        return this.get(injector, configurations);
    }

    /**
     * Create an instance of the object to provide using the specified
     * {@link Injector} and {@link Configurations}.
     * <p>
     * Concrete implementations of this class <b>must</b> either override this
     * or the {@link #get(Configurations)} method.
     */
    protected T get(Injector injector, Configurations configurations)
    throws Exception {
        return get(configurations);
    }

    /**
     * Create an instance of the object to provide using the specified
     * {@link Configurations}.
     * <p>
     * Concrete implementations of this class <b>must</b> either override this
     * or the {@link #get(Injector, Configurations)} method.
     */
    protected T get(Configurations configurations)
    throws Exception {
        throw new UnsupportedOperationException("Method not implemented");
    }

}
