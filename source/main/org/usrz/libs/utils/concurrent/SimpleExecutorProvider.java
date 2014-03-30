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

import javax.inject.Named;

import org.usrz.libs.logging.Log;
import org.usrz.libs.utils.configurations.Configurations;
import org.usrz.libs.utils.configurations.ConfiguredProvider;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;

@SuppressWarnings("restriction")
public class SimpleExecutorProvider extends ConfiguredProvider<SimpleExecutor> {

    public static final String CORE_POOL_SIZE = "corePoolSize";
    public static final String MAXIMUM_POOL_SIZE = "maximumPoolSize";
    public static final String KEEP_ALIVE_TIME = "keepAliveTime";
    public static final String QUEUE_SIZE = "queueSize";
    public static final String THREAD_PRIORITY = "threadPriority";
    public static final String EXECUTOR_NAME = "executorName";

    private int corePoolSize = 0;
    private int maximumPoolSize = Integer.MAX_VALUE;
    private int keepAliveTime = 60;
    private int queueSize = Integer.MAX_VALUE;
    private int threadPriority = NORM_PRIORITY;
    private String executorName = String.format("%s@%04x", SimpleExecutor.class.getSimpleName(), new Random().nextInt());

    protected SimpleExecutorProvider() {
        /* Nothing to do */
    }

    @Inject(optional=true)
    private void setCorePoolSize(@Named(CORE_POOL_SIZE) int corePoolSize) {
        if (corePoolSize < 0) throw new ProvisionException("Invalid corePoolSize " + corePoolSize);
        this.corePoolSize = corePoolSize;
    }

    @Inject(optional=true)
    private void setMaximumPoolSize(@Named(MAXIMUM_POOL_SIZE) int maximumPoolSize) {
        if (maximumPoolSize < 1) throw new ProvisionException("Invalid maximumPoolSize " + maximumPoolSize);
        this.maximumPoolSize = maximumPoolSize;
    }

    @Inject(optional=true)
    private void setKeepAliveTime(@Named(KEEP_ALIVE_TIME) int keepAliveTime) {
        if (keepAliveTime < 0) throw new ProvisionException("Invalid keepAliveTime " + keepAliveTime);
        this.keepAliveTime = keepAliveTime;
    }

    @Inject(optional=true)
    private void setQueueSize(@Named(QUEUE_SIZE) int queueSize) {
        if (queueSize < 1) throw new ProvisionException("Invalid queueSize " + queueSize);
        this.queueSize = queueSize;
    }

    @Inject(optional=true)
    private void setThreadPriority(@Named(THREAD_PRIORITY) int threadPriority) {
        if ((threadPriority < MIN_PRIORITY) || (threadPriority > MAX_PRIORITY))
            throw new ProvisionException("Invalid threadPriority " + threadPriority);
        this.threadPriority = threadPriority;
    }

    @Inject(optional=true)
    private void setExecutorName(@Named(EXECUTOR_NAME) String executorName) {

        if ((executorName == null) || (executorName.length() == 0))
            throw new ProvisionException("Invalid executorName " + executorName);
        this.executorName = executorName;
    }

    //@Inject
    private void setConfigurations(Configurations configs) {
        System.err.println("CONFIGURATIONS " + configs);
        System.err.println("FOO");
    }

    @Inject
    private void setInjector(Injector injector) {
        System.err.println("INJECTOR IS " + injector);
        System.err.println("FOO");
    }

    @Override
    public SimpleExecutor get() {
        System.err.println(executorName);

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
