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

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.usrz.libs.utils.configurations.Configurations;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ExecutorProvider implements Provider<ExecutorService> {

    private ExecutorService executor = null;
    private final String prefix;

    public ExecutorProvider(String prefix) {
        if (prefix == null) throw new NullPointerException("Prefix must be specified");
        this.prefix = prefix;
    }

    @Inject
    public void setConfigurations(Configurations configurations)
    throws IOException {

        /* Get our configs, with defaults */
        final Configurations stripped = configurations.strip(prefix);
        final int minThreads = stripped.get("minThreads", 0);
        final int maxThreads = stripped.get("maxThreads", 50);
        final int timeToLive = stripped.get("threadTTL", 60);

        /* Thread group and factory */
        final ThreadGroup group = new ThreadGroup("MongoExecutor");
        final ThreadFactory factory = new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(group, runnable, "MongoExecutor[" + count.getAndIncrement() + "]");
              }
        };

        /* Create our thread pool executor */
        executor = new ThreadPoolExecutor(minThreads, maxThreads, timeToLive, TimeUnit.SECONDS,
                                          new SynchronousQueue<Runnable>(), factory);
    }

    @Override
    public ExecutorService get() {
        if (executor == null) throw new IllegalStateException("Missing service, not injected?");
        return executor;
    }

}
