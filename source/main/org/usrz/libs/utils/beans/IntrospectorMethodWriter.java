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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An {@link IntrospectorWriter} using {@link Method}s
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
class IntrospectorMethodWriter extends IntrospectorWriter {

    private final Method method;

    IntrospectorMethodWriter(Method method) {
        super(method.getParameterTypes()[0]);
        this.method = method;
        method.setAccessible(true);
    }

    @Override
    void write(Object instance, Object value) {
        if ((value == null) && isPrimitive())
            throw new NullPointerException("Null value for primitive " + method.getParameterTypes()[0]);
        try {
            method.invoke(instance, value);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Exception accessing method " + method, exception);
        } catch (InvocationTargetException exception) {
            final Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw new IllegalStateException("Method " + method + " threw an exception", exception);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + method + "]";
    }

}
