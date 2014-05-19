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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class QueuedIteratorTest extends AbstractTest {

    public void testQueuedIterator(int loopsize, int concurrency, int queuesize)
    throws Exception {
        final AtomicInteger count = new AtomicInteger();
        final AtomicInteger exceptions = new AtomicInteger();
        final QueuedIterator<Integer> iterator = new QueuedIterator<>(queuesize);

        final Thread poller = new Thread(() -> {
            log.trace("Started puller");
            try {
                while (iterator.hasNext()) {
                    iterator.next();
                    count.incrementAndGet();
                }
                log.trace("Completed puller");
            } catch (Throwable throwable) {
                log.error(throwable, "Puller failed");
                exceptions.incrementAndGet();
            }
        });

        final Thread[] pushers = new Thread[concurrency];
        for (int x = 0; x < concurrency; x ++) {
            final int y = x;
            pushers[x] = new Thread(() -> {
                log.trace("Started pusher %d", y);
                try {
                    for (int c = 0; c < loopsize; c ++) iterator.accept(c);
                    log.trace("Completed pusher %d", y);
                } catch (Throwable throwable) {
                    log.error(throwable, "Pusher %d failed", y);
                    exceptions.incrementAndGet();
                }
            });
        }

        poller.start();
        for (Thread pusher: pushers) pusher.start();
        for (Thread pusher: pushers) pusher.join(10000);
        iterator.completed();
        poller.join(5000);

        assertFalse(poller.isAlive(), "Poller is still alive");

        assertEquals(exceptions.get(), 0, "Exceptions detected");
        assertEquals(count.get(), concurrency * loopsize, "Wrong number of results");
        iterator.close();

    }

    @Test
    public void testQueuedIterator()
    throws Exception {
//        this.testQueuedIterator(10000, 20, Integer.MAX_VALUE);
//        this.testQueuedIterator(10000, 20, 10);
//        this.testQueuedIterator(10000, 20, 1);
    }

    @Test
    public void testQueuedIteratorWithException()
    throws Exception {
        final RuntimeException exception = new RuntimeException();
        final QueuedIterator<Integer> iterator = new QueuedIterator<>();
        for (int x = 0; x < 100; x ++) iterator.accept(x);
        iterator.failed(exception);

        int count = 0;
        try {
            while (iterator.hasNext()) {
                assertEquals(iterator.next(), Integer.valueOf(count ++), "Invalid value");
            }
            fail("Exception not thrown");
        } catch (RuntimeException thrown) {
            assertSame(thrown, exception, "Exception differs");
        }
        assertEquals(count, 100, "Invalid count");
        iterator.close();
    }

    @Test
    public void testQueuedIteratorWithTimeout()
    throws Exception {
        final QueuedIterator<Integer> iterator = new QueuedIterator<>(1, SECONDS);
        for (int x = 0; x < 100; x ++) iterator.accept(x);

        final AtomicReference<Throwable> exception = new AtomicReference<>();
        final AtomicInteger count = new AtomicInteger();
        final Thread poller = new Thread(() -> {
            try {
                while (iterator.hasNext()) {
                    assertEquals(iterator.next(), Integer.valueOf(count.getAndIncrement()), "Invalid value");
                }
            } catch (Throwable throwable) {
                exception.set(throwable);
                log.trace(throwable, "Got an exception!");
            }
        });
        poller.start();

        poller.join(200);
        assertTrue(poller.isAlive(), "Poller already exited");
        assertEquals(count.get(), 100, "Invalid count received");

        poller.join(1000);
        assertFalse(poller.isAlive(), "Poller never exited");
        assertEquals(count.get(), 100, "Invalid count received");
        assertNotNull(exception.get(), "Exception is null");
        assertSame(exception.get().getClass(), IllegalStateException.class, "Invalid exception class");
        assertEquals(exception.get().getMessage(), "Timeout", "Invalid exception message");
        iterator.close();
    }
}

