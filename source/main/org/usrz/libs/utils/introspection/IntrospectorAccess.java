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

/**
 * Base class for {@link IntrospectorReader}s and {@link IntrospectorWriter}s
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
abstract class IntrospectorAccess {

    private final Class<?> type;
    private final boolean primitive;

    IntrospectorAccess(Class<?> type) {
        /* NULLs or VOIDs are bad */
        if (type == null) throw new NullPointerException("Null type");
        if (type.equals(void.class) || type.equals(Void.class))
            throw new IllegalArgumentException("Void type");

        /* Access the autoboxed class if this is a primitive */
        primitive = type.isPrimitive();
        this.type = type.equals(boolean.class) ? Boolean.class   :
                    type.equals(byte.class)    ? Byte.class      :
                    type.equals(short.class)   ? Short.class     :
                    type.equals(int.class)     ? Integer.class   :
                    type.equals(long.class)    ? Long.class      :
                    type.equals(float.class)   ? Float.class     :
                    type.equals(double.class)  ? Double.class    :
                    type.equals(char.class)    ? Character.class :
                    type;
    }

    final Class<?> getType() {
        return type;
    }

    final boolean isPrimitive() {
        return primitive;
    }

}
