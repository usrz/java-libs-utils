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

public class ObjectPool<T> {

    private final LinkedBlockingDeque<T> deque;
    private final Supplier<T> supplier;
    private final Predicate<T> predicate;

    public ObjectPool(int maxSize, Supplier<T> supplier) {
        this(maxSize, supplier, (instance) -> { return instance != null; });
    }

    public ObjectPool(int maxSize, Supplier<T> supplier, Predicate<T> predicate) {
        if (maxSize < 1) throw new IllegalArgumentException("Invalid size " + maxSize);
        this.supplier = Objects.requireNonNull(supplier, "Null supplier");
        this.predicate = Objects.requireNonNull(predicate, "Null predicate");
        this.deque = new LinkedBlockingDeque<T>(maxSize);
    }

    public T borrowObject() {
        final T instance = deque.pollFirst();
        if (instance != null) return instance;
        return supplier.get();
    }

    public void returnObject(T instance) {
        if (predicate.test(instance)) deque.offerLast(instance);
    }

}
