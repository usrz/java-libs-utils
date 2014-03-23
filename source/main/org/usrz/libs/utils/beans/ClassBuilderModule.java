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
package org.usrz.libs.utils.beans;

import javassist.ClassPool;

import javax.inject.Inject;

import org.usrz.libs.utils.inject.ModuleSupport;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

/**
 * A simple <em>Google Guice</em> {@link Module} providing a way to inject
 * dynamically constructed bean classes.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public abstract class ClassBuilderModule extends ModuleSupport {

    /**
     * Create a new {@link ClassBuilderModule} instance.
     */
    protected ClassBuilderModule() {
        /* Nothing to do */
    }

    /**
     * Configure the provided {@link Binder}.
     */
    @Override
    public final void configure(Binder binder) {
        binder.bind(ClassPool.class).toInstance(ClassPool.getDefault());
        binder.bind(BeanBuilder.class);
        binder.bind(MapperBuilder.class);
        super.configure(binder);
    }

    /**
     * Define the mapping for the specified {@link Class} {@link TypeLiteral}
     *
     * @return A {@link ClassBuilderBindingBuilder} used to specify the
     *         parameters for class construction.
     */
    public final <C> ClassBuilderBindingBuilder<C> bindType(TypeLiteral<Class<C>> type) {
        if (type == null) throw new NullPointerException("Null type");
        final Key<Class<C>> key = Key.get(type);
        return new ClassBuilderBindingBuilder<C>(key);
    }

    /* ====================================================================== */

    /**
     * A simple builder to specify now concrete implementation of classes
     * are defined.
     *
     * @see ClassBuilder#newClass(Class, Class...)
     * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
     */
    public class ClassBuilderBindingBuilder<C> {

        /* Our key for injection */
        private final Key<Class<C>> key;

        /* Constructor with key */
        private ClassBuilderBindingBuilder(Key<Class<C>> key) {
            this.key = key;
        }

        /**
         * Bind the generation of the concrete class to a {@link BeanBuilder}.
         *
         * @see BeanBuilder#newClass(Class, Class...)
         */
        public void toBean(Class<?> abstractClass, Class<?>... interfaces) {
            binder().bind(key).toProvider(new BeanClassProvider<C>(abstractClass, interfaces));
        }

        /**
         * Bind the generation of the concrete class to a {@link MapperBuilder}.
         *
         * @see MapperBuilder#newClass(Class, Class...)
         */
        public void toMapperBean(Class<?> abstractClass, Class<?>... interfaces) {
            binder().bind(key).toProvider(new MapperClassProvider<C>(abstractClass, interfaces));
        }

    }

    /* ====================================================================== */

    /**
     * An abstract provider building concrete classes.
     */
    private abstract class ClassProvider<C> implements Provider<Class<C>> {

        private final Class<?> abstractClass;
        private final Class<?>[] interfaces;
        private Class<C> type;

        protected ClassProvider(Class<?> abstractClass, Class<?>... interfaces) {
            this.abstractClass = abstractClass;
            this.interfaces = interfaces;
        }

        protected void initialize(ClassBuilder builder) {
            this.type = builder.newClass(abstractClass, interfaces);
        }

        @Override
        public final Class<C> get() {
            if (type != null) return type;
            throw new IllegalStateException("No type constructed (never injected?)");
        }
    }

    /**
     * A provider building concrete classes using a {@link BeanBuilder}.
     */
    private class BeanClassProvider<C> extends ClassProvider<C> {

        private BeanClassProvider(Class<?> abstractClass, Class<?>... interfaces) {
            super(abstractClass, interfaces);
        }

        @Inject
        private void setBeanBuilder(BeanBuilder builder) {
            initialize(builder);
        }
    }

    /**
     * A provider building concrete classes using a {@link MapperBuilder}.
     */
    private class MapperClassProvider<C> extends ClassProvider<C> {

        private MapperClassProvider(Class<?> abstractClass, Class<?>... interfaces) {
            super(abstractClass, interfaces);
        }

        @Inject
        private void setMapperBuilder(MapperBuilder builder) {
            initialize(builder);
        }
    }

}
