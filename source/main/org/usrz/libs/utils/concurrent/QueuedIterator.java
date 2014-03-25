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

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class QueuedIterator<T> implements Iterator<T>, Acceptor<T> {

    private final long timeout;
    private final BlockingQueue<Optional<T>> queue;
    private final AtomicReference<Throwable> failure = new AtomicReference<>();
    private final AtomicReference<Optional<T>> last = new AtomicReference<>();
    private final Optional<T> end = Optional.empty(); // throws NoSuchElement

    /* ====================================================================== */

    public QueuedIterator() {
        this(new LinkedBlockingQueue<>(), 10, SECONDS);
    }

    public QueuedIterator(int capacity) {
        this(new LinkedBlockingQueue<>(capacity), 10, SECONDS);
    }

    public QueuedIterator(long timeout, TimeUnit unit) {
        this(new LinkedBlockingQueue<>(), timeout, unit);
    }

    public QueuedIterator(int capacity, long timeout, TimeUnit unit) {
        this(new LinkedBlockingQueue<>(capacity), timeout, unit);
    }

    protected QueuedIterator(BlockingQueue<Optional<T>> queue, long timeout, TimeUnit unit) {
        this.queue = Objects.requireNonNull(queue, "Null queue");
        this.timeout = NANOSECONDS.convert(timeout, unit);
    }

    /* ====================================================================== */

    @Override
    public boolean hasNext() {
        if (last.get() != null) return true;

        try {

            /* Get our reference and check if it's our end */
            final Optional<T> reference = queue.poll(timeout, NANOSECONDS);
            if (reference == null) throw new IllegalStateException("Timeout");
            if (last.compareAndSet(null, reference)) {
                /* If we still have elements, continue */
                if (reference != end) return true;

                /* Check for failures at the end of our queue*/
                if (failure.get() == null) return false;

                /* If failed, throw the exception instead of returning */
                final Throwable throwable = failure.get();
                if (throwable instanceof RuntimeException) throw (RuntimeException) throwable;
                if (throwable instanceof Error) throw (Error) throwable;
                throw new IllegalStateException("Exception accessing data", throwable);
            }

            /* Two methods executing "hasNext()" at the same time */
            throw new ConcurrentModificationException();

        } catch (InterruptedException exception) {
            throw new IllegalStateException("Interrupted", exception);
        }
    }

    @Override
    public T next() {
        /* "hasNext()" will throw an exception if needed */
        if (!hasNext()) throw new NoSuchElementException();

        /* Get and return our reference */
        final Optional<T> reference = last.getAndSet(null);
        if (reference != null) return reference.get();

        /* Two methods executing "next()" at the same time */
        throw new ConcurrentModificationException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /* ====================================================================== */

    @Override
    public void accept(T instance) {
        try {
            if (queue.offer(Optional.ofNullable(instance), timeout, NANOSECONDS)) return;
            throw new IllegalStateException("Timeout");
        } catch (InterruptedException exception) {
            throw new IllegalStateException("Interrupted", exception);
        }
    }

    @Override
    public void completed() {
        try {
            if (queue.offer(end, timeout, NANOSECONDS)) return;
            throw new IllegalStateException("Timeout");
        } catch (InterruptedException exception) {
            throw new IllegalStateException("Interrupted", exception);
        }
    }

    @Override
    public void failed(Throwable throwable) {
        this.failure.set(Objects.requireNonNull(throwable, "Null throwable"));
        this.completed();
    }

}