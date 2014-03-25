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

import static org.usrz.libs.utils.concurrent.Immediate.immediate;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.usrz.libs.logging.Log;

import com.google.common.util.concurrent.ForwardingFuture;
import com.google.common.util.concurrent.SettableFuture;

@Singleton
public class SimpleExecutor {

    private static final Log log = new Log();
    private final ExecutorService executor;

    @Inject
    private SimpleExecutor() {
        this(Executors.newCachedThreadPool());
        log.info("Constructed unbound %s", this);
    }

    protected SimpleExecutor(ExecutorService executor) {
        this.executor = Objects.requireNonNull(executor);
    }

    public <T> NotifyingFuture<?> run(Runnable runnable) {
        return call(() -> { runnable.run(); return null; });
    }

    public <T> NotifyingFuture<T> call(Callable<T> callable) {
        return delegate(() -> immediate(callable.call()));
    }

    public <T> NotifyingFuture<T> delegate(Callable<Delegate<T>> delegator) {
        final SettableFuture<T> future = SettableFuture.create();

        return new DelegatingFuture<T>(future, executor.submit(() -> {
            try {
                delegator.call().withConsumer((delegate) -> {
                    try {
                        future.set(delegate.get());
                    } catch (Throwable throwable) {
                        future.setException(throwable);
                    }
                });

            } catch (Throwable throwable) {
                future.setException(throwable);
            }
        }));
    }

    private class DelegatingFuture<T> extends ForwardingFuture<T> implements NotifyingFuture<T> {

        private final SettableFuture<T> future;
        private final Future<?> task;

        private DelegatingFuture(SettableFuture<T> future, Future<?> task) {
            this.future = future;
            this.task = task;
        }

        @Override
        protected Future<T> delegate() {
            return future;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            try {
                return super.cancel(mayInterruptIfRunning);
            } finally {
                task.cancel(mayInterruptIfRunning);
            }
        }

        @Override
        public NotifyingFuture<T> withConsumer(Consumer<Future<T>> consumer) {
            future.addListener(() -> consumer.accept(future), executor);
            return this;
        }

    }
}
