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

import java.lang.reflect.Field;

/**
 * An {@link IntrospectorWriter} using {@link Field}s
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
class IntrospectorFieldWriter extends IntrospectorWriter {

    private final Field field;

    IntrospectorFieldWriter(Field field) {
        super(field.getType());
        this.field = field;
        field.setAccessible(true);
    }

    @Override
    void write(Object instance, Object value) {
        if ((value == null) && isPrimitive())
            throw new NullPointerException("Null value for primitive " + field.getType());
        try {
            field.set(instance, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Exception accessing field " + field, exception);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + field + "]";
    }

}
