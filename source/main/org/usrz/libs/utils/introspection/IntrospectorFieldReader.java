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
package org.usrz.libs.utils.introspection;

import java.lang.reflect.Field;

/**
 * An {@link IntrospectorReader} using {@link Field}s
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
class IntrospectorFieldReader extends IntrospectorReader {

    private final Field field;

    IntrospectorFieldReader(Field field) {
        super(field.getType());
        this.field = field;
        field.setAccessible(true);
    }

    @Override
    Object read(Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Exception accessing field " + field, exception);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + field + "]";
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (object == null) return false;
        try {
            return ((IntrospectorFieldReader) object).field.equals(field);
        } catch (ClassCastException exception) {
            return false;
        }
    }
}
