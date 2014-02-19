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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private final HashMap<Class<? extends Annotation>, Map<Annotation, IntrospectedProperty<T>>> byAnnotation = new HashMap<>();
    private boolean sealed = false;

    /* ====================================================================== */

    IntrospectionDescriptor(Class<T> type) {
        this.type = type;
    }

    IntrospectionDescriptor<T> seal() {
        for (Map.Entry<IntrospectorKey, ReadersAndWriters> entry: readersAndWriters.entrySet()) {
            final IntrospectorKey key = entry.getKey();
            final ReadersAndWriters value = entry.getValue();
            final IntrospectedProperty<T> property = new IntrospectedProperty<T>(entry.getKey(), value.readers, value.writers, this);
            properties.put(key, property);

            final Annotation annotation = key.getAnnotation();
            if (annotation == null) continue;
            final Class<? extends Annotation> annotationClass = annotation.annotationType();

            Map<Annotation, IntrospectedProperty<T>> properties = byAnnotation.get(annotationClass);
            if (properties == null) byAnnotation.put(annotationClass, properties = new HashMap<Annotation, IntrospectedProperty<T>>());
            properties.put(key.getAnnotation(), property);

            System.err.println(byAnnotation);
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
    public <A extends Annotation> Map<A, IntrospectedProperty<T>> getProperties(Class<A> annotationClass) {
        if (! sealed) throw new IllegalStateException("Not sealed");

        @SuppressWarnings("unchecked")
        final Map<A, IntrospectedProperty<T>> properties = (Map<A, IntrospectedProperty<T>>) byAnnotation.get(annotationClass);
        return properties != null ? Collections.unmodifiableMap(properties) : Collections.<A, IntrospectedProperty<T>>emptyMap();
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

        /* Iterate through our readers, and only keep one per type */
        for (IntrospectorReader current: access.readers)
            if (current.getType().equals(reader.getType())) return false;

        /* We're lucky, we can add */
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

        /* Iterate through our readers, and only keep one per type */
        for (IntrospectorWriter current: access.writers)
            if (current.getType().equals(writer.getType())) return false;

        /* We're lucky, we can add */
        access.writers.add(writer);
        return true;
    }

    protected void describe() {
        System.err.println(type.getName());
        for (Map.Entry<IntrospectorKey, ReadersAndWriters> entry: readersAndWriters.entrySet()) {
            System.err.println("  " + entry.getKey());
            for (IntrospectorReader reader: entry.getValue().readers)
                System.err.println("  <<<  " + reader);
            for (IntrospectorWriter writer: entry.getValue().writers)
                System.err.println("  >>>  " + writer);
        }
    }

    /* ====================================================================== */

    private static final class ReadersAndWriters {

        private final List<IntrospectorReader> readers = new ArrayList<>();
        private final List<IntrospectorWriter> writers = new ArrayList<>();

    }
}
