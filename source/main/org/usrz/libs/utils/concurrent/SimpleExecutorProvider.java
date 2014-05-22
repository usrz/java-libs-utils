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

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.usrz.libs.configurations.ConfigurableProvider;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.logging.Log;

public class SimpleExecutorProvider extends ConfigurableProvider<SimpleExecutor, SimpleExecutorProvider> {

    public static final String CORE_POOL_SIZE = "core_pool_size";
    public static final String MAXIMUM_POOL_SIZE = "maximum_pool_size";
    public static final String KEEP_ALIVE_TIME = "keep_alive_time";
    public static final String QUEUE_SIZE = "queue_size";
    public static final String THREAD_PRIORITY = "thread_priority";
    public static final String EXECUTOR_NAME = "executor_name";

    public SimpleExecutorProvider() {
        /* Nothing to do */
    }

    public SimpleExecutorProvider(Configurations configurations) {
        super();
        with(configurations);
    }

    @Override
    protected SimpleExecutor get(Configurations configurations) {
        final String executorName  = configurations.get(EXECUTOR_NAME, String.format("%s@%04x", SimpleExecutor.class.getSimpleName(), new Random().nextInt()));

        final int corePoolSize     = configurations.validate(CORE_POOL_SIZE,    0,                 (int value) -> value >= 0);
        final int maximumPoolSize  = configurations.validate(MAXIMUM_POOL_SIZE, Integer.MAX_VALUE, (int value) -> value >= 1);
        final int keepAliveTime    = configurations.validate(KEEP_ALIVE_TIME,   60,                (int value) -> value >= 0);
        final int queueSize        = configurations.validate(QUEUE_SIZE,        Integer.MAX_VALUE, (int value) -> value >= 1);
        final int threadPriority   = configurations.validate(THREAD_PRIORITY,   NORM_PRIORITY,     (int value) -> (value >= MIN_PRIORITY) && (value <= MAX_PRIORITY));

        final ThreadGroup group = new ThreadGroup(executorName);
        final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueSize);

        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                            corePoolSize,
                            maximumPoolSize,
                            keepAliveTime, TimeUnit.SECONDS,
                            queue,
                            new SimpleThreadFactory(group, threadPriority),
                            new SimpleRejectedExecutionHandler(executorName));
        return new SimpleExecutor(executor);
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

        private static final Log log = new Log(SimpleExecutor.class);
        private final String executorName;

        private SimpleRejectedExecutionHandler(String executorName) {
            this.executorName = executorName;
        }

        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
            final String message = String.format("Executor %s unable to execute %s", executorName, runnable);
            log.warn(message);
            throw new RejectedExecutionException(message);
        }

    }
}
