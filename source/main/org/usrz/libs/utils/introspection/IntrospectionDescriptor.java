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

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * An {@link IntrospectionDescriptor} is a container of all
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <T>
 */
public class IntrospectionDescriptor<T> {

    private final Class<T> type;
    private final HashMap<IntrospectorKey, IntrospectedProperty<T>> properties = new HashMap<>();
    private final HashMap<Class<? extends Annotation>, Map<Annotation, Set<IntrospectedProperty<T>>>> byAnnotation = new HashMap<>();
    private boolean sealed = false;

    /* ====================================================================== */

    IntrospectionDescriptor(Class<T> type) {
        this.type = type;
    }

    IntrospectionDescriptor<T> seal() {
        for (Entry<IntrospectorKey, ReadersAndWriters> entry: readersAndWriters.entrySet()) {
            final IntrospectorKey key = entry.getKey();
            final ReadersAndWriters value = entry.getValue();
            final IntrospectedProperty<T> property = new IntrospectedProperty<T>(entry.getKey(), value.readers, value.writers, this);
            properties.put(key, property);

            final Annotation annotation = key.getAnnotation();
            if (annotation == null) continue;
            final Class<? extends Annotation> annotationClass = annotation.annotationType();

            Map<Annotation, Set<IntrospectedProperty<T>>> map = byAnnotation.get(annotationClass);
            if (map == null) byAnnotation.put(annotationClass, map = new HashMap<Annotation, Set<IntrospectedProperty<T>>>());
            Set<IntrospectedProperty<T>> set = map.get(annotation);
            if (set == null) map.put(annotation, set = new HashSet<IntrospectedProperty<T>>());
            set.add(property);
        }

        for (Entry<Class<? extends Annotation>, Map<Annotation, Set<IntrospectedProperty<T>>>> entry: byAnnotation.entrySet()) {
            for (Entry<Annotation, Set<IntrospectedProperty<T>>> entry2: entry.getValue().entrySet())
                entry2.setValue(Collections.unmodifiableSet(entry2.getValue()));
            entry.setValue(Collections.unmodifiableMap(entry.getValue()));
        }

        sealed = true;
        return this;
    }

    /* ====================================================================== */

    /**
     * Return the type which this {@link IntrospectionDescriptor} describes.
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Returns <b>true</b> if this {@link IntrospectionDescriptor} contains
     * the {@linkplain IntrospectedProperty property} identified by the
     * specified {@linkplain IntrospectedProperty#getName() name} and <b>no</b>
     * {@linkplain IntrospectedProperty#getAnnotation() annotation}.
     */
    public boolean hasProperty(String name) {
        return this.hasProperty(name, null);
    }

    /**
     * Returns <b>true</b> if this {@link IntrospectionDescriptor} contains
     * the {@linkplain IntrospectedProperty#getName() <b>null</b>-named}
     * {@linkplain IntrospectedProperty property} associated with the specified
     * {@linkplain IntrospectedProperty#getAnnotation() annotation}.
     */
    public boolean hasProperty(Annotation annotation) {
        return this.hasProperty(null, annotation);
    }

    /**
     * Returns <b>true</b> if this {@link IntrospectionDescriptor} contains
     * the {@linkplain IntrospectedProperty property} identified by the
     * specified {@linkplain IntrospectedProperty#getName() name} and specified
     * {@linkplain IntrospectedProperty#getAnnotation() annotation}.
     */
    public boolean hasProperty(String name, Annotation annotation) {
        if (! sealed) throw new IllegalStateException("Not sealed");
        return properties.containsKey(new IntrospectorKey(name, annotation));
    }

    /**
     * Returns the {@linkplain IntrospectedProperty property} identified by the
     * specified {@linkplain IntrospectedProperty#getName() name} and <b>no</b>
     * {@linkplain IntrospectedProperty#getAnnotation() annotation}.
     *
     * @throws IntrospectionException If the given property could not be found.
     */
    public IntrospectedProperty<T> getProperty(String name)
    throws IntrospectionException {
        return this.getProperty(name, null);
    }

    /**
     * Returns the @linkplain IntrospectedProperty#getName() <b>null</b>-named}
     * {@linkplain IntrospectedProperty property} identified by the specified
     * {@linkplain IntrospectedProperty#getAnnotation() annotation}.
     *
     * @throws IntrospectionException If the given property could not be found.
     */
    public IntrospectedProperty<T> getProperty(Annotation annotation)
    throws IntrospectionException {
        return this.getProperty(null, annotation);
    }

    /**
     * Returns the {@linkplain IntrospectedProperty property} identified by the
     * specified {@linkplain IntrospectedProperty#getName() name} and specified
     * {@linkplain IntrospectedProperty#getAnnotation() annotation}.
     *
     * @throws IntrospectionException If the given property could not be found.
     */
    public IntrospectedProperty<T> getProperty(String name, Annotation annotation)
    throws IntrospectionException {
        final IntrospectorKey key = new IntrospectorKey(name, annotation);
        if (! sealed) throw new IllegalStateException("Not sealed");
        final IntrospectedProperty<T> introspected = properties.get(key);
        if (introspected != null) return introspected;

        final String message = "Class " + this.getType().getName() + " does not define property " + key.description();
        throw new IntrospectionException(message);
    }

    /* ====================================================================== */

    /**
     * Return a <em>immutable</em> {@link Set} of all known
     * {@linkplain IntrospectedProperty properties}.
     */
    public Set<IntrospectedProperty<T>> getProperties() {
        final Set<IntrospectedProperty<T>> set = new HashSet<>();
        set.addAll(this.properties.values());
        return Collections.unmodifiableSet(set);
    }

    /**
     * Return a <em>immutable</em> {@link Map} of all known
     * {@linkplain IntrospectedProperty properties} associated with the
     * specified {@linkplain Annotation annotation class}, keyed by
     * {@link Annotation} instance.
     */
    public <A extends Annotation> Map<A, Set<IntrospectedProperty<T>>> getProperties(Class<A> annotationClass) {
        if (! sealed) throw new IllegalStateException("Not sealed");

        @SuppressWarnings("unchecked")
        final Map<A, Set<IntrospectedProperty<T>>> properties = (Map<A, Set<IntrospectedProperty<T>>>) byAnnotation.get(annotationClass);
        return properties == null ? Collections.<A, Set<IntrospectedProperty<T>>>emptyMap() : properties;
    }

    /* ====================================================================== */
    /* BEFORE SEALING THINGS LOOK AS FOLLOWS                                  */
    /* ====================================================================== */

    private final Map<IntrospectorKey, ReadersAndWriters> readersAndWriters = new HashMap<>();

    boolean addReader(String property, Annotation annotation, IntrospectorReader reader, boolean addOnlyIfEmpty) {

        /* Find the key, if null simply bail out */
        final IntrospectorKey key = new IntrospectorKey(property, annotation);
        if (key.isNull()) return false;

        /* Get (or create) our list of readers and writers */
        ReadersAndWriters access = readersAndWriters.get(key);
        if (access == null) readersAndWriters.put(key, access = new ReadersAndWriters());

        /* If empty, add, otherwise, conditionally add */
        if (access.readers.isEmpty()) return access.readers.add(reader);
        if (addOnlyIfEmpty) return false;

        /* We're lucky, we can add */
        if (access.readers.contains(reader)) return false;
        access.readers.add(reader);
        return true;
    }

    boolean addWriter(String property, Annotation annotation, IntrospectorWriter writer, boolean addOnlyIfEmpty) {

        /* Find the key, if null simply bail out */
        final IntrospectorKey key = new IntrospectorKey(property, annotation);
        if (key.isNull()) return false;

        /* Get (or create) our list of readers and writers */
        ReadersAndWriters access = readersAndWriters.get(key);
        if (access == null) readersAndWriters.put(key, access = new ReadersAndWriters());

        /* If empty, add, otherwise, conditionally add */
        if (access.writers.isEmpty()) return access.writers.add(writer);
        if (addOnlyIfEmpty) return false;

        /* We're lucky, we can add */
        if (access.writers.contains(writer)) return false;
        access.writers.add(writer);
        return true;
    }

    void describe(PrintStream output) {
        output.println(type.getName());
        for (Entry<IntrospectorKey, ReadersAndWriters> entry: readersAndWriters.entrySet()) {
            output.println("  " + entry.getKey());
            for (IntrospectorReader reader: entry.getValue().readers)
                output.println("    <--  " + reader);
            for (IntrospectorWriter writer: entry.getValue().writers)
                output.println("    -->  " + writer);
        }
    }

    /* ====================================================================== */

    private static final class ReadersAndWriters {

        private final List<IntrospectorReader> readers = new ArrayList<>();
        private final List<IntrospectorWriter> writers = new ArrayList<>();

    }
}
