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

import java.util.concurrent.ConcurrentHashMap;

/**
 * A trivial {@link Cache} backed by a {@link ConcurrentHashMap}.
 * <p>
 * There are no limits imposed to the number of elements this cache can
 * contain, henceforth it should be used in development/testing scenarios only.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class SimpleCache<K, V> implements Cache<K, V> {

    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();

    @Override
    public V fetch(K key) {
        return map.get(key);
    }

    @Override
    public void store(K key, V value) {
        if (key == null) throw new NullPointerException("Null key");
        if (value == null) invalidate(key);
        else map.put(key, value);
    }

    @Override
    public void invalidate(K key) {
        map.remove(key);
    }

}
