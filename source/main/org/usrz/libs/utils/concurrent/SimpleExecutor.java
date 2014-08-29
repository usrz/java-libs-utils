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

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.ProvidedBy;

@ProvidedBy(SimpleExecutorProvider.class)
public class SimpleExecutor {

    private final ExecutorService executor;
    private final Executor notifier;
    private final String name;

    protected SimpleExecutor(String name, ExecutorService executor, Executor notifier) {
        this.executor = Objects.requireNonNull(executor, "Null executor");
        this.notifier = Objects.requireNonNull(notifier, "Null notifier");
        this.name = name == null ? "Unknown" : name;
    }

    public String getName() {
        return name;
    }

    public <T> NotifyingFuture<?> run(Runnable runnable) {
        return call(() -> { runnable.run(); return null; });
    }

    public <T> NotifyingFuture<T> call(Callable<T> callable) {
        final SettableFuture<T> settableFuture = SettableFuture.create();

        final Future<T> executingFuture = executor.submit(() -> {
            try {
                final T result = callable.call();
                settableFuture.set(result);
                return result;
            } catch (Throwable throwable) {
                settableFuture.setException(throwable);
                throw new Exception(throwable);
            }
        });

        return new NotifyingFuture<T>() {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                if (executingFuture.cancel(mayInterruptIfRunning)) {
                    settableFuture.cancel(mayInterruptIfRunning);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean isCancelled() {
                return settableFuture.isCancelled();
            }

            @Override
            public boolean isDone() {
                return settableFuture.isCancelled();
            }

            @Override
            public T get()
            throws InterruptedException, ExecutionException {
                return settableFuture.get();
            }

            @Override
            public T get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
                return settableFuture.get(timeout, unit);
            }

            @Override
            public NotifyingFuture<T> withConsumer(Consumer<Future<T>> consumer) {
                settableFuture.addListener(() -> consumer.accept(settableFuture), notifier);
                return this;
            }

        };
    }
}
