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
package org.usrz.libs.utils.beans;

import java.util.Map;

/**
 * A simple interface implemented by all classes created by a
 * {@link MapperBuilder} to retrieve the various setter properties.
 *
 * <p>Note that the returned {@link Map} is <b>not</b> a copy, henceforth
 * any change made on the {@link Map} will be reflected in the getter
 * values.</p>
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public interface Mapper {

    /**
     * Get the {@link Map} containing all the properties of a <em>bean</em>
     * created by a {@link MapperBuilder}.
     */
    public Map<String, Object> mappedProperties();

}
