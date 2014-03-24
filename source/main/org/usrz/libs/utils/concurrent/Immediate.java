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
package org.usrz.libs.utils.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class Immediate<T> implements NotifyingFuture<T> {

    private final T instance;

    private Immediate(T instance) {
        this.instance = instance;
    }

    public static final <T> Immediate<T> immediate(T instance) {
        return new Immediate<T>(instance);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public T get() {
        return instance;
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
        return instance;
    }

    @Override
    public Immediate<T> withConsumer(Consumer<Future<T>> consumer) {
        consumer.accept(this);
        return this;
    }

}
