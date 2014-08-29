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

import static java.lang.Thread.MAX_PRIORITY;
import static java.lang.Thread.MIN_PRIORITY;
import static java.lang.Thread.NORM_PRIORITY;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.logging.Log;
import org.usrz.libs.utils.inject.ConfigurableProvider;

public class SimpleExecutorProvider extends ConfigurableProvider<SimpleExecutor> {

    public static final String CORE_POOL_SIZE = "core_pool_size";
    public static final String MAXIMUM_POOL_SIZE = "maximum_pool_size";
    public static final String KEEP_ALIVE_TIME = "keep_alive_time";
    public static final String QUEUE_SIZE = "queue_size";
    public static final String THREAD_PRIORITY = "thread_priority";
    public static final String EXECUTOR_NAME = "executor_name";
    public static final String NOTIFIER_THREADS = "notifier_threads";

    private static final Log log = new Log(SimpleExecutor.class);

    public SimpleExecutorProvider(String name) {
        super(name, true);
    }

    public SimpleExecutorProvider(Annotation annotation) {
        super(annotation, true);
    }

    public SimpleExecutorProvider(Class<? extends Annotation> annotation) {
        super(annotation, true);
    }

    public SimpleExecutorProvider(Configurations configurations) {
        super(configurations, true);
    }

    @Override
    public SimpleExecutor get(Configurations configurations) {
        return create(configurations);
    }

    public static final SimpleExecutor create(Configurations configurations) {
        final String executorName  = configurations.get(EXECUTOR_NAME, String.format("%s@%04x", SimpleExecutor.class.getSimpleName(), new Random().nextInt()));

        final int corePoolSize    = configurations.validate(CORE_POOL_SIZE,    0,                      (int value) -> value >= 0);
        final int maximumPoolSize = configurations.validate(MAXIMUM_POOL_SIZE, Integer.MAX_VALUE,      (int value) -> value >= 1);
        final int notifierThreads = configurations.validate(NOTIFIER_THREADS,  0,                      (int value) -> value >= 0);
        final int queueSize       = configurations.validate(QUEUE_SIZE,        Integer.MAX_VALUE,      (int value) -> value >= 1);
        final int threadPriority  = configurations.validate(THREAD_PRIORITY,   NORM_PRIORITY,          (int value) -> (value >= MIN_PRIORITY) && (value <= MAX_PRIORITY));
        final Duration keepAlive  = configurations.validate(KEEP_ALIVE_TIME,   Duration.ofSeconds(60), (Duration value) -> value.getNano() >= 0);

        log.debug("Executor[%s]  core pool size: %d threads",        executorName, corePoolSize);
        log.debug("Executor[%s]   max pool size: %d threads",        executorName, maximumPoolSize);
        log.debug("Executor[%s]       notifiers: %d threads",        executorName, notifierThreads);
        log.debug("Executor[%s]      queue size: %d tasks",          executorName, queueSize);
        log.debug("Executor[%s] thread priority: %d (%d > %d > %d)", executorName, threadPriority, MIN_PRIORITY, NORM_PRIORITY, MAX_PRIORITY);
        log.debug("Executor[%s]      keep alive: %d ms",             executorName, keepAlive.toMillis());

        /* How to notify completion */
        final Executor notifier;
        if (notifierThreads == 0) {
            notifier = ((runnable) -> runnable.run());
        } else {
            final ThreadGroup group = new ThreadGroup(executorName + "[Notifier]");
            final ThreadFactory factory = new SimpleThreadFactory(group, threadPriority);
            notifier = Executors.newFixedThreadPool(notifierThreads, factory);
        }

        /* Our main execution executor */
        final ThreadGroup group = new ThreadGroup(executorName);
        final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueSize);
        final ThreadFactory factory = new SimpleThreadFactory(group, threadPriority);
        final RejectedExecutionHandler handler = new SimpleRejectedExecutionHandler(executorName);
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize,
                                                                   maximumPoolSize,
                                                                   keepAlive.toNanos(),
                                                                   NANOSECONDS,
                                                                   queue,
                                                                   factory,
                                                                   handler);

        /* Done, create the executor */
        return new SimpleExecutor(executorName, executor, notifier);
    }

    /* ====================================================================== */

    private static class SimpleThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger(0);
        private final ThreadGroup group;
        private final int priority;

        private SimpleThreadFactory(ThreadGroup group, int priority) {
            this.group = group;
            this.priority = priority;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            final int threadNumber = this.threadNumber.incrementAndGet();
            final String name = String.format("%s-%d", group.getName(), threadNumber);
            final Thread thread = new Thread(group, runnable, name);
            thread.setPriority(priority);
            return thread;
        }

    }

    /* ====================================================================== */

    private static class SimpleRejectedExecutionHandler implements RejectedExecutionHandler {

        private final String executorName;

        private SimpleRejectedExecutionHandler(String executorName) {
            this.executorName = executorName;
        }

        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
            final String message = String.format("Executor[%s]: unable to execute %s", executorName, runnable);
            log.warn(message);
            throw new RejectedExecutionException(message);
        }

    }

}
