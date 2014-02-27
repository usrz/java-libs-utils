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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class QueueingFuture<T> extends SettableFuture<Iterator<T>>
implements IterableFuture<T>, Puttable<T> {

    private final LinkedBlockingQueue<Reference> queue = new LinkedBlockingQueue<>();
    private final AtomicReference<Reference> last = new AtomicReference<>();

    /* ====================================================================== */

    public QueueingFuture() {
        super();
    }

    /* ====================================================================== */

    @Override
    public boolean hasNext(long timeout, TimeUnit unit)
    throws InterruptedException, TimeoutException {
        if (last.get() != null) return true;

        final Reference reference = queue.poll(timeout, unit);
        if (reference == null) throw new TimeoutException();
        if (last.compareAndSet(null, reference)) {
            return end != reference;
        } else {
            throw new ConcurrentModificationException();
        }
    }

    @Override
    public T next(long timeout, TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException {
        if (hasNext(timeout, unit)) {
            final Reference reference = last.getAndSet(null);
            if (reference == null) {
                throw new ConcurrentModificationException();
            } else {
                return reference.get();
            }
        } else {
            throw new NoSuchElementException();
        }
    }

    /* ====================================================================== */

    @Override
    public QueueingFuture<T> notify(Future<?> future) {
        super.notify(future);
        return this;
    }

    @Override
    public Iterator<T> get(long timeout, TimeUnit unit) {
        return this;
    }

    @Override
    public boolean put(T instance) {
        if (isDone()) return false;
        return queue.add(new Reference(instance));
    }

    @Override
    public boolean close() {
        if (isDone()) return false;
        queue.add(end);
        return super.set(this);
    }

    @Override
    public boolean fail(Throwable throwable) {
        if (super.fail(throwable)) {
            this.queue.add(new ErrorReference(throwable));
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (super.cancel(mayInterruptIfRunning)) {
            this.queue.add(new ErrorReference(new CancellationException("Cancelled")));
            return true;
        } else {
            return false;
        }
    }

    /* ====================================================================== */

    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @Override
    public boolean hasNext()
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
    public T next()
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
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /* ====================================================================== */

    private class Reference {

        private final T reference;

        private Reference(T reference) {
            this.reference = reference;
        }

        public T get()
        throws ExecutionException  {
            return reference;
        }
    }

    private class ErrorReference extends Reference {

        private final ExecutionException exception;

        private ErrorReference(Throwable throwable) {
            super(null);
            this.exception = throwable instanceof ExecutionException ?
                    (ExecutionException) throwable :
                    new ExecutionException(throwable);
        }

        @Override
        public T get()
        throws ExecutionException {
            throw exception;
        }
    }

    private final Reference end = new Reference(null) {

        @Override
        public T get() {
            throw new NoSuchElementException();
        }
    };

}
