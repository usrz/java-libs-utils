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

import java.util.function.Function;

import javax.inject.Provider;

import org.usrz.libs.utils.Check;

import com.google.inject.Injector;
import com.google.inject.ProvisionException;

/**
 * A {@link Provider} requiring an {@link Injector} to create instances.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
@SuppressWarnings("restriction")
public abstract class InjectingProvider<T>
implements javax.inject.Provider<T>,
           com.google.inject.Provider<T> {

    private final boolean singleton;
    private Injector injector;
    private T instance;

    /**
     * Create a new {@link InjectingProvider} providing <em>singleton</em>
     * instances of objects.
     */
    protected InjectingProvider() {
        this(true);
    }

    /**
     * Create a new {@link InjectingProvider} instance.
     * <p>
     * If {@code singleton} is <em>true</em> (the default behavior) the
     * {@link #get(Injector)} method will only be invoked once, and the instance
     * it returns will be provided to all caller of {@link #get()}.
     * <p>
     * If {@code singleton} is <em>false</em> the {@link #get(Injector)} method
     * will only be invoked every time {@link #get()} is called.
     */
    protected InjectingProvider(boolean singleton) {
        this.singleton = singleton;
    }

    @javax.inject.Inject @com.google.inject.Inject
    private void setInjector(Injector injector) {
        this.injector = injector;
    }

    @Override
    public final T get() {
        if (this.instance != null) return instance;

        if (injector == null) throw new ProvisionException("Injector not available in " + this.getClass().getName());
        try {
            final T instance = this.get(injector);
            if (instance == null) {
                throw new ProvisionException("Null instance returned by " + this.getClass().getName());
            } else if (singleton) {
                this.instance = instance;
            }
            injector.injectMembers(instance);
            return instance;
        } catch (Exception exception) {
            throw new ProvisionException("Exception providing instance in " + this.getClass().getName(), exception);
        }
    }

    /**
     * Create an instance of the object to provide.
     */
    protected abstract T get(Injector injector)
    throws Exception;

    /* ====================================================================== */

    /**
     * Create a new {@link InjectingProvider} from the specified function.
     * <p>
     * The specified {@link Function} will be called only <em>once</em> and
     * its returned value will be used as a <em>singleton</em>.
     */
    public static final <T> Provider<T> from(final Function<Injector, T> function) {
        return from(true, function);
    }

    /**
     * Create a new {@link InjectingProvider} from the specified function.
     * <p>
     * As with the {@linkplain #InjectingProvider(boolean) constructor}, the
     * {@code singleton} flag will determine if the {@link Function} is invoked
     * only <em>once</em> or each time an instance is required.
     */
    public static final <T> Provider<T> from(boolean singleton, final Function<Injector, T> function) {
        Check.notNull(function, "Null creation function");
        return new InjectingProvider<T>(singleton) {
            @Override public T get(Injector injector) { return function.apply(injector); }
        };
    }

}
