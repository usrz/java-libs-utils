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

/**
 * A <em>trivial</em> interface defining a component capable of caching
 * instances of <em>key-value</em> mappings.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <K> The type of keys in this {@link Cache}.
 * @param <V> The type ov values in this {@link Cache}.
 */
public interface Cache<K, V> {

    /**
     * Return the cached value associated with the given key or <em>null</em>.
     */
    public V fetch(K key);

    /**
     * Associated the given value with the specified key.
     * <p>
     * If the specified value is <em>null</em> this call is equivalent to
     * calling {@link #invalidate(Object)}.
     *
     * @throws NullPointerException If the specified key was <em>null</em>.
     */
    public void store(K key, V value);

    /**
     * Invalidate the mapping associated with the given key.
     */
    public void invalidate(K key);

}
