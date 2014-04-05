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

import static org.usrz.libs.utils.Check.notNull;

import java.util.Iterator;
import java.util.function.Function;

public class ConvertIterator<T, R>
implements Iterator<R> {

    private final Iterator<T> iterator;
    private final Function<T, R> function;

    public ConvertIterator(Iterator<T> iterator, Function<T, R> function) {
        this.iterator = notNull(iterator, "Null iterator");
        this.function = notNull(function, "Null function");
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public R next() {
        return function.apply(iterator.next());
    }

    @Override
    public void remove() {
        iterator.remove();
    }
}
