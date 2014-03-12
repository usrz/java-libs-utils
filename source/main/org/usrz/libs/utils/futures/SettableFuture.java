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
package org.usrz.libs.utils.futures;

import static java.lang.Integer.MAX_VALUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettableFuture<T> implements Future<T> {

    private final AtomicReference<Result> result = new AtomicReference<>(null);
    private final List<Future<?>> futures = new ArrayList<>();
    private final Semaphore semaphore = new Semaphore(-1);

    public SettableFuture() {
        /* Nothing to do here */
    }

    public boolean fail(Throwable throwable) {
        final Result result = new Result(null, throwable, false);
        if (this.result.compareAndSet(null, result)) {
            semaphore.release(MAX_VALUE);
            notifyFutures(result, true);
            return true;
        } else {
            return false;
        }
    }

    public boolean set(T object) {
        final Result result = new Result(object, null, false);
        if (this.result.compareAndSet(null, result)) {
            semaphore.release(MAX_VALUE);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        final Result result = new Result(null, null, true);
        if (this.result.compareAndSet(null, result)) {
            semaphore.release(MAX_VALUE);
            notifyFutures(result, mayInterruptIfRunning);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isCancelled() {
        final Result result = this.result.get();
        return result == null ? false : result.cancelled;
    }

    @Override
    public boolean isDone() {
        return this.result.get() != null;
    }

    @Override
    public T get()
    throws InterruptedException, ExecutionException {
        try {
            return this.get(MAX_VALUE, MILLISECONDS);
        } catch (TimeoutException exception) {
            throw new UncheckedTimeoutException(exception);
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException {
        if (this.semaphore.tryAcquire(timeout, unit)) {
            final Result result = this.result.get();
            if (result == null) {
                /* This should never happen, but anyhow... */
                throw new IllegalStateException("No result");
            } else {
                if (result.cancelled) throw new CancellationException("Cancelled");
                if (result.exception != null) throw result.exception;
                return result.result;
            }
        } else {
            throw new TimeoutException();
        }
    }

    /* ====================================================================== */

    public SettableFuture<T> notify(Future<?> future) {
        synchronized (this.futures) {
            final Result result = this.result.get();
            if (result != null) {
                this.notifyFuture(result, future, false);
            } else {
                this.futures.add(future);
            }
            return this;
        }
    }

    private void notifyFutures(Result result, boolean mayInterruptIfRunning) {
        synchronized (this.futures) {
            for (Future<?> future: futures) {
                notifyFuture(result, future, mayInterruptIfRunning);
            }
        }
    }

    private void notifyFuture(Result result, Future<?> future, boolean mayInterruptIfRunning) {
        try {
            if (result.cancelled) {
                future.cancel(mayInterruptIfRunning);
            } else if (result.exception != null) {
                if (future instanceof SettableFuture) {
                    ((SettableFuture<?>) future).fail(result.exception);
                } else {
                    future.cancel(mayInterruptIfRunning);
                }
            }
        } catch (Throwable throwable) {
            final String message = String.format("Exception notifying future %s" + future);
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, message, throwable);
        }
    }

    /* ====================================================================== */

    private final class Result {
        private final ExecutionException exception;
        private final boolean cancelled;
        private final T result;

        private Result(T result, Throwable throwable, boolean cancelled) {
            this.result = result;
            this.exception = throwable == null ? null :
                                 throwable instanceof ExecutionException ?
                                     (ExecutionException) throwable :
                                     new ExecutionException(throwable);
            this.cancelled = cancelled;
        }
    }
}
