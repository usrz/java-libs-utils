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

import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.PROTECTED;
import static java.lang.reflect.Modifier.PUBLIC;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link Introspector} provides a way to discover, read and write object
 * properties (through <em>getters</em>, <em>setters</em> or <em>fields</em>)
 * and, unlike other more established frameworks, supports introspection of
 * <em>annotated</em> properties.
 *
 * <p>{@link IntrospectionDescriptor}s are <em>cached</em> by instances of
 * {@link Introspector}s on a <em>per-type</em> basis, but different
 * {@link Introspector} instances have separate caches.</p>
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class Introspector {

    private static final int ACCESS_MASK = PUBLIC ^ PROTECTED ^ PRIVATE;
    private static final int[] ACCESS_ORDER = { PROTECTED, 0, PRIVATE }; // 0 -> PACKAGE

    /* Our cache */
    private final ConcurrentHashMap<Class<?>, IntrospectionDescriptor<?>> cache;

    /**
     * Create a new {@link Introspector} instance.
     */
    public Introspector() {
        cache = new ConcurrentHashMap<>();
    }

    /**
     * Return an {@link IntrospectionDescriptor} for the specified
     * {@link Class}.
     *
     * <p>For any {@link Class} at least the {@link Object#getClass()} will
     * result in a {@link IntrospectedProperty} to be generated.</p>
     */
    public <T> IntrospectionDescriptor<T> getDescriptor(Class<T> type) {
        @SuppressWarnings("unchecked")
        final IntrospectionDescriptor<T> cached = (IntrospectionDescriptor<T>) cache.get(type);
        if (cached != null) return cached;

        final IntrospectionDescriptor<T> descriptor = new IntrospectionDescriptor<T>(type);
        describeMethods(type, descriptor, type.getMethods(), PUBLIC, false);
        describeFields (type, descriptor, type.getFields(),  PUBLIC, false);

        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            final Method[] methods = c.getDeclaredMethods();
            final Field[]  fields =  c.getDeclaredFields();
            for (int access: ACCESS_ORDER) {
                describeMethods(c, descriptor, methods, access, true);
                describeFields (c, descriptor, fields,  access, true);
            }
        }

        cache.putIfAbsent(type, descriptor.seal());
        return descriptor;
    }

    /* ====================================================================== */
    /* METHODS AND FIELDS DISCOVERY                                           */
    /* ====================================================================== */

    private <T> IntrospectionDescriptor<T> describeMethods(Class<?> clazz, IntrospectionDescriptor<T> descriptor, Method[] methods, int access, boolean addOnlyIfEmpty) {
        for (Method method: methods) {

            /* Don't deal with static/abstract/non-public methods */
            if (shouldSkip(method.getModifiers(), access));

            /* Check the method */
            if ((method.getParameterTypes().length == 0) &&
                (!method.getReturnType().equals(Void.class)) &&
                (!method.getReturnType().equals(void.class))) {

                /* This could be a potential getter */
                final String property = propertyNameFromMethod(method);
                final Set<Annotation> annotations = findMethodAnnotations(clazz, method);
                if (notIntrospected(annotations)) continue;


                /* Remember the method with *all* its annotations */
                descriptor.addReader(property, null, new IntrospectorMethodReader(method), false);
                for (Annotation annotation: annotations) {
                    descriptor.addReader(property, annotation, new IntrospectorMethodReader(method), false);
                }

            } else if ((method.getParameterTypes().length == 1) &&
                       (!method.getParameterTypes()[0].equals(Void.class)) &&
                       (!method.getParameterTypes()[0].equals(void.class))) {

                /* This could be a potential setter */
                final String property = propertyNameFromMethod(method);
                final Set<Annotation> annotations = findMethodAnnotations(clazz, method);
                if (notIntrospected(annotations)) continue;

                /* Remember the method with *all* its annotations */
                descriptor.addWriter(property, null, new IntrospectorMethodWriter(method), false);
                for (Annotation annotation: annotations) {
                    descriptor.addWriter(property, annotation, new IntrospectorMethodWriter(method), false);
                }
            }
        }

        /* Done, return descriptor */
        return descriptor;
    }

    private <T> IntrospectionDescriptor<T> describeFields(Class<?> clazz, IntrospectionDescriptor<T> descriptor, Field[] fields, int access, boolean addOnlyIfEmpty) {
        for (Field field: fields) {

            /* Don't deal with static/abstract/non-public fields */
            final int modifiers = field.getModifiers();
            if (shouldSkip(modifiers, Modifier.PUBLIC));
            final boolean notFinal = !Modifier.isFinal(modifiers);

            /* Ignore void types */
            if (field.getType().equals(Void.class) ||
                field.getType().equals(void.class)) continue;

            /* Check if this field is annotated with @NotIntrospected */
            final Annotation[] annotations = field.getAnnotations();
            if (notIntrospected(annotations)) continue;

            /* Remember the field with *all* its annotations */
            final String name = field.getName();
            /*         */ descriptor.addReader(name, null, new IntrospectorFieldReader(field), true);
            if (notFinal) descriptor.addWriter(name, null, new IntrospectorFieldWriter(field), true);
            for (Annotation annotation: annotations) {
                /*         */ descriptor.addReader(name, annotation, new IntrospectorFieldReader(field), true);
                if (notFinal) descriptor.addWriter(name, annotation, new IntrospectorFieldWriter(field), true);
            }
        }

        /* Return the descriptor we were given */
        return descriptor;
    }

    /* ====================================================================== */
    /* UTILITY METHODS                                                        */
    /* ====================================================================== */

    private static boolean notIntrospected(Annotation[] annotations) {
        return notIntrospected(Arrays.asList(annotations));
    }

    private static boolean notIntrospected(Collection<? extends Annotation> annotations) {
        for (Annotation annotation: annotations)
            if (NotIntrospected.class.isAssignableFrom(annotation.annotationType()))
                return true;
        return false;
    }

    private static boolean shouldSkip(int modifiers, int access) {
        if (Modifier.isAbstract(modifiers)) return true;
        if (Modifier.isStatic(modifiers)) return true;
        return (modifiers & ACCESS_MASK) != access;
    }

    private static String propertyNameFromMethod(Method method) {
        final String name = method.getName();

        if ((name.startsWith("get") || name.startsWith("set")) && (name.length() > 3))
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);

        if (name.startsWith("is") && (name.length() > 2))
            return Character.toLowerCase(name.charAt(2)) + name.substring(3);

        return null;
    }

    private static Set<Annotation> findMethodAnnotations(Class<?> type, Method method) {
        return findMethodAnnotations(type, method.getName(), method.getParameterTypes());
    }

    private static Set<Annotation> findMethodAnnotations(Class<?> type, String name, Class<?>[] parameterTypes) {

        /* Meh, don't consider object or "null" */
        if ((type == null) || type.equals(Object.class)) return Collections.emptySet();

        /* Get the annotations of the declared method */
        final Set<Annotation> annotations = new HashSet<>();
        try {
            final Method method = type.getDeclaredMethod(name, parameterTypes);
            annotations.addAll(Arrays.asList(method.getAnnotations()));
        } catch (NoSuchMethodException exception) {
            /* No such method here, go on with interfaces and superclasses */
        }

        /* Superclass and interfaces */
        annotations.addAll(findMethodAnnotations(type.getSuperclass(), name, parameterTypes));
        for (Class<?> interfaceType: type.getInterfaces()) {
            annotations.addAll(findMethodAnnotations(interfaceType, name, parameterTypes));
        }

        /* Return all the collected annotations */
        return annotations;
    }
}
