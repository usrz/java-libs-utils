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
package org.usrz.libs.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility class to build {@link Map}s.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class MapBuilder<K, V> {

    private final HashMap<K, V> map = new HashMap<>();

    /**
     * Create a new {@link MapBuilder} instance.
     */
    public MapBuilder() {
        /* Nothing to do */
    }

    /* ====================================================================== */

    /**
     * Associate a <em>key</em> with a <em>value</em>.
     *
     * @see Map#put(Object, Object)
     */
    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    /**
     * Associate a <em>key</em> with a <em>value</em> if no such mapping
     * was already defined.
     *
     * @see Map#putIfAbsent(Object, Object)
     */
    public MapBuilder<K, V> putIfAbsent(K key, V value) {
        map.putIfAbsent(key, value);
        return this;
    }

    /**
     * Copy all mappings from the specified {@link Map}.
     *
     * @see Map#putAll(Map)
     */
    public MapBuilder<K, V> putAll(Map<? extends K,? extends V> mappings) {
        map.putAll(mappings);
        return this;
    }

    /* ====================================================================== */

    /**
     * Return a {@link HashMap} of the mappings.
     */
    public HashMap<K, V> map() {
        return map;
    }

    /**
     * Return an {@linkplain Collections#unmodifiableMap(Map) unmodifiable}
     * {@link Map} of the mappings.
     */
    public Map<K, V> unmodifiableMap() {
        return Collections.unmodifiableMap(map);
    }

    /**
     * Return a {@link ConcurrentHashMap} of the mappings.
     */
    public ConcurrentHashMap<K, V> concurrentHashMap() {
        return new ConcurrentHashMap<>(map);
    }

    /**
     * Return a {@link TreeMap} of the mappings.
     */
    public TreeMap<K, V> treeMap() {
        return new TreeMap<>(map);
    }

}
