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

import static java.lang.Long.MAX_VALUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class ConvertingIterableFuture<T, F> implements IterableFuture<T> {

    protected final IterableFuture<F> future;

    protected ConvertingIterableFuture(IterableFuture<F> future) {
        if (future == null) throw new NullPointerException("Null future");
        this.future = future;
    }

    @Override
    public boolean hasNext(long timeout, TimeUnit unit)
    throws InterruptedException, TimeoutException {
        return future.hasNext(timeout, unit);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public final Iterator<T> iterator() {
        return this;
    }

    @Override
    public final Iterator<T> get() {
        return this;
    }

    @Override
    public final Iterator<T> get(long timeout, TimeUnit unit) {
        return this;
    }

    @Override
    public final boolean hasNext()
    throws UncheckedInterruptedException {
        try {
            return this.hasNext(MAX_VALUE, MILLISECONDS);
        } catch (InterruptedException exception) {
            throw new UncheckedInterruptedException(exception);
        } catch (TimeoutException exception) {
            /* This should never happen, we wait forever... */
            throw new UncheckedTimeoutException(exception);
        }
    }

    @Override
    public final T next()
    throws UncheckedExecutionException,
           UncheckedInterruptedException {
        try {
            return this.next(MAX_VALUE, MILLISECONDS);
        } catch (ExecutionException exception) {
            throw new UncheckedExecutionException(exception);
        } catch (InterruptedException exception) {
            throw new UncheckedInterruptedException(exception);
        } catch (TimeoutException exception) {
            /* This should never happen, we wait forever... */
            throw new UncheckedTimeoutException(exception);
        }
    }

    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }

}
