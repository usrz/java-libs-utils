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

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple utility class allowing to create instances of {@link Annotation}s.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class Annotations {

    /**
     * Create a new {@link Annotation} instance of the specified <em>type</em>.
     *
     * @param type The {@link Annotation} type to create.
     * @return A non-null {@link Annotation} instance of the specified type
     * @throws NullPointerException If <code>type</code> was <b>null</b>.
     * @throws IllegalArgumentException If the <code>type</code> specified could
     *                      not be instantiated (because the {@link Annotation}
     *                      is wrong or might require specific arguments).
     */
    public static <T extends Annotation> T newInstance(Class<T> type)
    throws NullPointerException, IllegalArgumentException {
        return newInstance(type, Collections.emptyMap());
    }

    /**
     * Create a new {@link Annotation} instance of the specified <em>type</em>,
     * specifying the attributes (as <code>methodName</code>, <em>value</em>
     * mappings) to use.
     *
     * @param type The {@link Annotation} type to create.
     * @return A non-null {@link Annotation} instance of the specified type
     * @throws NullPointerException If <code>type</code> was <b>null</b>.
     * @throws IllegalArgumentException If the <code>type</code> specified could
     *                      not be instantiated (because the {@link Annotation}
     *                      is wrong or might require specific arguments).
     */
    public static <T extends Annotation> T newInstance(Class<T> type, Map<String, Object> attributes)
    throws NullPointerException, IllegalArgumentException {

        /* Basic checks */
        Check.notNull(type, "Null annotation type to construct");
        if (! Annotation.class.isAssignableFrom(type))
            throw new IllegalArgumentException("Class " + type.getName() + " is not an annotation");
        if (attributes == null) attributes = Collections.emptyMap();


        /* Retention policy check */
        final Retention retention = type.getAnnotation(Retention.class);
        if (retention == null) {
            throw new IllegalArgumentException("Annotation " + type.getName() + " is not annotated with @Retention");
        } else {
            final RetentionPolicy policy = retention.value();
            if (policy == null) { /* Should never happen */
                throw new IllegalArgumentException("Annotation " + type.getName() + " does not speciy a @Retention policy");
            } else if (!RetentionPolicy.RUNTIME.equals(policy)) {
                throw new IllegalArgumentException("Annotation " + type.getName() + " specifies the wrong \"" + policy.name() + "\" @Retention policy");
            }
        }

        /* Get all the annotation methods in one giant map! */
        final Set<Method> methods = new HashSet<>();
        methods.addAll(Arrays.asList(type.getMethods()));
        methods.addAll(Arrays.asList(type.getDeclaredMethods()));

        /* Prepare a map of default values */
        final Map<String, Object> defaultValues = new HashMap<>();

        /* Iterate through each discovered method to see what we have */
        for (Method method: methods) {

            /* toString, hashCode, annotationType and equals are special */
            if (isKnownMethod(method)) continue;

            /* Check the method signature, no arguments, non-void return type (better be safe than sorry) */
            if (!isAnnotationMethod(method)) {
                throw new IllegalArgumentException("Method " + method + " is not a valid annotation attribute");
            }

            /* Figure out the actual value */
            final String attribute = method.getName();
            final Class<?> attributeType = mapPrimitiveType(method.getReturnType());
            final Object defaultValue = method.getDefaultValue();

            if (attributes.containsKey(attribute)) {
                final Object specifiedValue = attributes.get(attribute);
                if (attributeType.isInstance(specifiedValue)) {
                    defaultValues.put(attribute, specifiedValue);
                } else {
                    final String specifiedType = specifiedValue == null ? "null" : specifiedValue.getClass().getName();
                    throw new IllegalArgumentException("Method " + method + " requires value of type " + attributeType.getName() + " but specified " + specifiedType);
                }

            } else if (defaultValue != null) {
                defaultValues.put(attribute, defaultValue);
            } else {
                throw new IllegalArgumentException("Method " + method + " does not define a default value");
            }
        }

        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {

                /* Annotation.equals(): use reflectivity to simplify */
                if (isEquals(method)) {
                    final Object object = args[0];
                    if (object == null) return false;
                    if (object == proxy) return true;
                    return object.equals(proxy);
                }

                /* Annotation.toString(): build with all attributes */
                if (isToString(method)) {

                    /* Build up a comma separated list of attributes */
                    final StringBuilder builder = new StringBuilder();
                    for (Map.Entry<String, Object> entry: defaultValues.entrySet()){
                        builder.append(", ").append(entry.getKey())
                               .append("=") .append(Annotations.toString(entry.getValue()));
                    }

                    /* If we have attributes, surround by parenthesis */
                    if (builder.length() > 1) {
                        builder.delete(0, 2).insert(0, "(").append(")");
                    } else {
                        builder.append("()");
                    }

                    /* Return the standard string representation */
                    return "@" + type.getName() + builder;
                }

                /* Annotation.hashCode(): computed adding all attributes */
                if (isHashCode(method)) {
                    int hashCode = 0;
                    for (Map.Entry<String, Object> entry: defaultValues.entrySet()){
                        hashCode += (127 * entry.getKey().hashCode())
                                  ^ Annotations.hashCode(entry.getValue());

                    }
                    return hashCode;
                }

                /* Annotation.annotationType: simply return the type */
                if (isAnnotationType(method)) {
                    return type;
                }

                /* Anything else must be an annotation attribute */
                if (isAnnotationMethod(method)) {
                    if (defaultValues.containsKey(method.getName())) {
                        return defaultValues.get(method.getName());
                    } else {
                        throw new IllegalStateException("No default value for " + method);
                    }
                } else {
                    throw new UnsupportedOperationException("Unknown method " + method);
                }
            }

        }));
    }

    /* ====================================================================== */
    /* METHOD CHECKIING                                                       */
    /* ====================================================================== */

    private static boolean isEquals(Method method) {
        return method.getName().equals("equals")
            && method.getParameterCount() == 1
            && method.getParameterTypes()[0].equals(Object.class)
            && boolean.class.equals(method.getReturnType());
    }

    private static boolean isToString(Method method) {
        return method.getName().equals("toString")
            && method.getParameterCount() == 0
            && String.class.equals(method.getReturnType());
    }

    private static boolean isHashCode(Method method) {
        return method.getName().equals("hashCode")
            && method.getParameterCount() == 0
            && int.class.equals(method.getReturnType());
    }

    private static boolean isAnnotationType(Method method) {
        return method.getName().equals("annotationType")
            && method.getParameterCount() == 0
            && Class.class.equals(method.getReturnType());
    }

    private static boolean isKnownMethod(Method method) {
        return isEquals(method)
            || isToString(method)
            || isHashCode(method)
            || isAnnotationType(method);
    }

    private static boolean isAnnotationMethod(Method method) {
        return method.getParameterCount() == 0
            && (! void.class.equals(method.getReturnType()));
    }

    /* ====================================================================== */
    /* TYPE CONVERSION AND TYPE VALUES (for primitives and arrays, mostly)    */
    /* ====================================================================== */

    private static final Class<?> mapPrimitiveType(Class<?> type) {
        if (type.equals(boolean.class)) {
            return Boolean.class;
        } else if (type.equals(byte.class)) {
            return Byte.class;
        } else if (type.equals(char.class)) {
            return Character.class;
        } else if (type.equals(double.class)) {
            return Double.class;
        } else if (type.equals(float.class)) {
            return Float.class;
        } else if (type.equals(int.class)) {
            return Integer.class;
        } else if (type.equals(long.class)) {
            return Long.class;
        } else if (type.equals(short.class)) {
            return Short.class;
        } else {
            return type;
        }
    }

    private static final int hashCode(Object value) {
        if (value == null) {
            return 0;
        } else if (value.getClass().isArray()) {
            if (value.getClass().getComponentType().equals(boolean.class)) {
                return Arrays.hashCode((boolean[]) value);
            } else if (value.getClass().getComponentType().equals(byte.class)) {
                return Arrays.hashCode((byte[]) value);
            } else if (value.getClass().getComponentType().equals(char.class)) {
                return Arrays.hashCode((char[]) value);
            } else if (value.getClass().getComponentType().equals(double.class)) {
                return Arrays.hashCode((double[]) value);
            } else if (value.getClass().getComponentType().equals(float.class)) {
                return Arrays.hashCode((float[]) value);
            } else if (value.getClass().getComponentType().equals(int.class)) {
                return Arrays.hashCode((int[]) value);
            } else if (value.getClass().getComponentType().equals(long.class)) {
                return Arrays.hashCode((long[]) value);
            } else if (value.getClass().getComponentType().equals(short.class)) {
                return Arrays.hashCode((short[]) value);
            } else {
                return Arrays.hashCode((Object[]) value);
            }
        } else {
            return value.hashCode();
        }
    }

    private static final String toString(Object value) {
        if (value == null) {
            return "null";
        } else if (value.getClass().isArray()) {
            if (value.getClass().getComponentType().equals(boolean.class)) {
                return Arrays.toString((boolean[]) value);
            } else if (value.getClass().getComponentType().equals(byte.class)) {
                return Arrays.toString((byte[]) value);
            } else if (value.getClass().getComponentType().equals(char.class)) {
                return Arrays.toString((char[]) value);
            } else if (value.getClass().getComponentType().equals(double.class)) {
                return Arrays.toString((double[]) value);
            } else if (value.getClass().getComponentType().equals(float.class)) {
                return Arrays.toString((float[]) value);
            } else if (value.getClass().getComponentType().equals(int.class)) {
                return Arrays.toString((int[]) value);
            } else if (value.getClass().getComponentType().equals(long.class)) {
                return Arrays.toString((long[]) value);
            } else if (value.getClass().getComponentType().equals(short.class)) {
                return Arrays.toString((short[]) value);
            } else {
                return Arrays.toString((Object[]) value);
            }
        } else {
            return value.toString();
        }
    }
}
