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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.usrz.libs.logging.Log;
import org.usrz.libs.utils.configurations.Configuration;
import org.usrz.libs.utils.configurations.Configurations;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;

@SuppressWarnings("restriction")
public class SimpleExecutorProvider implements Provider<SimpleExecutor> {

    private Configurations configurations = Configurations.EMPTY_CONFIGURATIONS;
    private final AtomicInteger groupNumber = new AtomicInteger();

    protected SimpleExecutorProvider() {
        /* Nothing to do */
    }

    @Inject(optional = true)
    private void setConfigurations(@Configuration(SimpleExecutorProvider.class) Configurations configurations) {
        this.configurations = configurations;
    }

    @Override
    public SimpleExecutor get() {
        final int corePoolSize = configurations.get("corePoolSize", 0);
        if (corePoolSize < 0) throw new ProvisionException("Invalid corePoolSize " + corePoolSize);

        final int maximumPoolSize = configurations.get("maximumPoolSize", Integer.MAX_VALUE);
        if (maximumPoolSize < 1) throw new ProvisionException("Invalid maximumPoolSize " + maximumPoolSize);

        final int keepAliveTime = configurations.get("keeAliveTime", 60);
        if (keepAliveTime < 0) throw new ProvisionException("Invalid keepAliveTime " + keepAliveTime);

        final int queueSize = configurations.get("queueSize", Integer.MAX_VALUE);
        if (queueSize < 1) throw new ProvisionException("Invalid queueSize " + queueSize);

        final int threadPriority = configurations.get("threadPriority", NORM_PRIORITY);
        if ((threadPriority < MIN_PRIORITY) || (threadPriority > MAX_PRIORITY))
            throw new ProvisionException("Invalid threadPriority " + threadPriority);

        final String groupName = SimpleExecutor.class.getSimpleName() + "_" + groupNumber.incrementAndGet();
        final ThreadGroup group = new ThreadGroup(groupName);

        final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueSize);
        return new SimpleExecutor(new ThreadPoolExecutor(
                corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, queue,
                new ThreadFactory() {

                    private final AtomicInteger threadNumber = new AtomicInteger();

                    @Override
                    public Thread newThread(Runnable runnable) {
                        final Thread thread = new Thread(group, runnable, groupName + "@" + threadNumber.incrementAndGet());
                        thread.setPriority(threadPriority);
                        return thread;
                    }

                }, new RejectedExecutionHandler() {

                    private final Log log = new Log(SimpleExecutor.class);

                    @Override
                    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
                        log.warn("Unable to execute runnable %s", runnable);
                        throw new RejectedExecutionException("Unable to execute runnable");
                    }

                }));
    }
}
