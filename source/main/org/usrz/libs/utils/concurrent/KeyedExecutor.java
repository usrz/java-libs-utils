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

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.usrz.libs.logging.Log;
import org.usrz.libs.utils.Check;

public class KeyedExecutor<K> {

    private final ConcurrentHashMap<K, NotifyingFuture<?>> futures = new ConcurrentHashMap<>();
    private final SimpleExecutor executor;
    private final Log log = new Log();
    private final String name;

    public KeyedExecutor(SimpleExecutor executor) {
        this.executor = Check.notNull(executor, "Null executor");
        this.name = executor.getName();
    }

    public <T> NotifyingFuture<?> run(K key, Runnable runnable) {
        return call(key, () -> { runnable.run(); return null; });
    }

    @SuppressWarnings("unchecked")
    public <T> NotifyingFuture<T> call(K key, Callable<T> callable) {

        /* Check if we already have a future without synchronizing */
        NotifyingFuture<T> future = (NotifyingFuture<T>) futures.get(key);
        if (future != null) {
            log.trace("Executor[%s]: queueing (1) for key %s to existing future %s: %s", name, key, future, callable);
            return future;
        }

        synchronized (this) {
            /* Recheck synchronized */
            future = (NotifyingFuture<T>) futures.get(key);
            if (future != null) {
                log.trace("Executor[%s]: queueing (2) for key %s to existing future %s: %s", name, key, future, callable);
                return future;
            }

            /* Create a new future and be notified when we're done */
            future = executor.call(callable).withConsumer((f) -> futures.remove(key));
            log.trace("Executor[%s]: key %s created a new future %s: %s", name, key, future, callable);
            futures.put(key, future);
            return future;
        }
    }

}
