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
package org.usrz.libs.utils.caches;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import com.google.common.cache.CacheBuilder;

/**
 * A wrapper around Google's <em>Guava Caches</em>.
 *
 * <p>At time of writing the specification looks somehow similar to:</p>
 *
 * <ul>
 * <li><code>concurrencyLevel=[integer]</code>: Guides the allowed concurrency among update operations<br>
 *                                              (see {@link CacheBuilder#concurrencyLevel(int)}).</li>
 * <li><code>initialCapacity=[integer]</code>: Sets the minimum total size for the internal hash tables<br>
 *                                              (see {@link CacheBuilder#initialCapacity(int)}).</li>
 * <li><code>maximumSize=[long</code>]: Specifies the maximum number of entries the cache may contain<br>
 *                                      (see {@link CacheBuilder#maximumSize(long)}).</li>
 * <li><code>maximumWeight=[long]</code>: Specifies the maximum weight of entries the cache may contain<br>
 *                                        (see {@link CacheBuilder#maximumWeight(long)}).</li>
 * <li><code>expireAfterAccess=[duration]</code>: Specifies that each entry should be automatically removed from the cache once a fixed duration has
 *                                                elapsed after the entry's creation, the most recent replacement of its value, or its last access<br>
 *                                                (see {@link CacheBuilder#expireAfterAccess(long, java.util.concurrent.TimeUnit)}).</li>
 * <li><code>expireAfterWrite=[duration]</code>: Specifies that each entry should be automatically removed from the cache once a fixed duration has
 *                                               elapsed after the entry's creation, or the most recent replacement of its value<br>
 *                                               (see {@link CacheBuilder#expireAfterWrite(long, java.util.concurrent.TimeUnit)}).</li>
 * <li><code>weakKeys</code>: Specifies that each key (not value) stored in the cache should be wrapped in a {@link WeakReference}<br>
 *                            (by default, strong references are used, see {@link CacheBuilder#weakKeys()}).</li>
 * <li><code>softValues</code>: Specifies that each value (not key) stored in the cache should be wrapped in a {@link SoftReference}<br>
 *                              (by default, strong references are used, see {@link CacheBuilder#softValues()}).</li>
 * <li><code>weakValues</code>: Specifies that each value (not key) stored in the cache should be wrapped in a {@link WeakReference}<br>
 *                              (by default, strong references are used, see {@link CacheBuilder#weakValues()}).</li>
 * </ul>
 *
 * <p>Durations are represented by an integer, followed by one of
 * "<code>d</code>", "<code>h</code>", "<code>m</code>", or "<code>s</code>",
 * representing days, hours, minutes, or seconds respectively.</p>
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class GuavaCache<K, V> implements Cache<K, V> {

    private com.google.common.cache.Cache<K, V> cache;

    /**
     * Create a new {@link GuavaCache} instance given a building specification.
     */
    public GuavaCache(String specification) {
        CacheBuilder.from(specification).build();
    }

    @Override
    public V fetch(K key) {
        return this.cache.getIfPresent(key);
    }

    @Override
    public void store(K key, V value) {
        if (key == null) throw new NullPointerException("Null key");
        if (value == null) this.invalidate(key);
        else this.cache.put(key, value);
    }

    @Override
    public void invalidate(K key) {
        if (key != null) cache.invalidate(key);
    }

}
