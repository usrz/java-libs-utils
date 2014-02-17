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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstanceBuilder {

    private static final Logger logger = Logger.getLogger(InstanceBuilder.class.getName());

    /* ====================================================================== */

    private InstanceBuilder() {
        throw new IllegalStateException("Do not construct");
    }

    /* ====================================================================== */

    public static <T> T newInstance(Class<?> concreteClass) {
        return newInstance(concreteClass, (Object[]) null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<?> concreteClass, Object... parameters) {

        final Class<?>[] classes = new Class<?>[parameters == null ? 0 : parameters.length];
        if (parameters == null) parameters = new Object[0];
        for (int x = 0; x < parameters.length; x++) {
            if (parameters[x] != null) classes[x] = parameters[x].getClass();
        }

        try {
            final Constructor<?> constructor = concreteClass.getConstructor(classes);
            if (logger.isLoggable(Level.FINE))
                logger.fine("Creating new instance of " + concreteClass.getName() + " with " + constructor);
            constructor.setAccessible(true);
            return (T) constructor.newInstance(parameters);

        } catch (NoSuchMethodException exception) {
            // No constructor found, upwards and onwards!
        } catch (InvocationTargetException exception) {
            final Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw new IllegalStateException("Constructor of " + concreteClass.getName() + " threw an exception", exception);
        } catch (InstantiationException  | IllegalAccessException exception) {
            throw new IllegalStateException("Unable to create instance of " + concreteClass.getName(), exception);
        }

        try {
            for (Constructor<?> constructor: concreteClass.getDeclaredConstructors()) {
                if (isAssignable(constructor.getParameterTypes(), parameters)) {
                    if (logger.isLoggable(Level.FINE))
                        logger.fine("Creating new instance of " + concreteClass.getName() + " with " + constructor);
                    constructor.setAccessible(true);
                    return (T) constructor.newInstance(parameters);
                }
            }

            throw new IllegalStateException("No constructor for " + concreteClass.getName() + " found with specified parameters");

        } catch (InvocationTargetException exception) {
            final Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw new IllegalStateException("Constructor of " + concreteClass.getName() + " threw an exception", exception);
        } catch (InstantiationException | IllegalAccessException exception) {
            throw new IllegalStateException("Unable to create instance of " + concreteClass.getName(), exception);
        }
    }

    /* ====================================================================== */

    private static boolean isAssignable(Class<?>[] types, Object[] parameters) {
        if (types.length != parameters.length) return false;
        for (int x = 0; x < types.length; x ++) {
            if (parameters[x] == null) continue;
            if (types[x].isInstance(parameters[x])) continue;
            return false;
        }
        return true;
    }

}
