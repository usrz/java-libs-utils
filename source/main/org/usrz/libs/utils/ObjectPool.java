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
package org.usrz.libs.utils;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An extremely simple <em>object pool</em> constructing objects on
 * {@linkplain #borrowObject() borrow} and pooling them up to a maximum of
 * <em>maxSize</em> instances on {@linkplain #returnObject(Object) return}.
 * <p>
 * Note that when this pool is empty, it will <em>always</em> call the
 * configured {@link Supplier} to create new instances.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <T> The type of objects stored by this {@link ObjectPool}
 */
public class ObjectPool<T> {

    private final LinkedBlockingDeque<T> deque;
    private final Supplier<T> supplier;
    private final Predicate<T> predicate;

    /**
     * Create an {@link ObjectPool} containing up to the specified maxmum
     * number of instances.
     * <p>
     * Objects {@linkplain #returnObject(Object) returned} will be added back
     * to the pool if <em>not null</em>.
     *
     * @param maxSize The maximum number of instances to keep around.
     * @param supplier A {@link Supplier} to create new instances on borrow.
     */
    public ObjectPool(int maxSize, Supplier<T> supplier) {
        this(maxSize, supplier, notNull());
    }

    /**
     * Create an {@link ObjectPool} containing up to the specified maxmum
     * number of instances.
     * <p>
     * Objects {@linkplain #returnObject(Object) returned} will be added back
     * to the pool if <em>not null</em> and the specified {@link Predicate}
     * returns <em>true</em>.
     *
     * @param maxSize The maximum number of instances to keep around.
     * @param supplier A {@link Supplier} to create new instances on borrow.
     * @param supplier A {@link Predicate} to check returned instances.
     */
    public ObjectPool(int maxSize, Supplier<T> supplier, Predicate<T> predicate) {
        if (maxSize < 1) throw new IllegalArgumentException("Invalid size " + maxSize);
        this.supplier = Objects.requireNonNull(supplier, "Null supplier");
        this.predicate = ObjectPool.<T>notNull().and(predicate);
        this.deque = new LinkedBlockingDeque<T>(maxSize);
    }

    /**
     * Borrow an object instance from the pool or create a new instance if one
     * is not currently available.
     */
    public T borrowObject() {
        final T instance = deque.pollFirst();
        if (instance != null) return instance;
        return supplier.get();
    }

    /**
     * Return an object instance to the pool if it does not violate capacity
     * constraints.
     */
    public void returnObject(T instance) {
        if (predicate.test(instance)) deque.offerLast(instance);
    }

    /* ====================================================================== */

    private static final <T> Predicate<T> notNull() {
        return (instance) -> instance != null;
    }
}
