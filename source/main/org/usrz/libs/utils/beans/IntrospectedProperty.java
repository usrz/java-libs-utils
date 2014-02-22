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
import java.util.List;

/**
 * An <em>introspected</em> property as represented in an
 * {@link IntrospectionDescriptor}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <T>
 */
public class IntrospectedProperty<T> {

    private final IntrospectorKey key;
    private final List<IntrospectorReader> readers;
    private final List<IntrospectorWriter> writers;
    private final IntrospectionDescriptor<T> descriptor;

    IntrospectedProperty(IntrospectorKey key, List<IntrospectorReader> readers, List<IntrospectorWriter> writers, IntrospectionDescriptor<T> descriptor) {
        this.key = key;
        this.readers = readers;
        this.writers = writers;
        this.descriptor = descriptor;
    }

    /* ====================================================================== */

    /**
     * Return the {@link IntrospectionDescriptor} associated with this.
     */
    public IntrospectionDescriptor<T> getDescriptor() {
        return this.descriptor;
    }

    /**
     * Return the <em>name</em> of the property represented by this instance.
     *
     * <p>The name is either derived from the getter/setter methods or a
     * field name (standard convention apply, <code>getValue()</code> will
     * be represented by the <code>value</code> name).</p>
     *
     * <p>The name <em>could</em> be <b>null</b> if this property is defined
     * only by an {@linkplain #getAnnotation() annotation}, for example:</p>
     *
     * <pre>
     * public class MyClass {
     *
     *   &#64;MyAnnotation
     *   public Object annotatedValueGetter() { ... }
     *
     *   &#64;MyAnnotation
     *   public void annotatedValueSetter(Object object) { ... }
     *
     * }
     * </pre>
     *
     * <p>will create an {@link IntrospectedProperty} instance with <b>null</b>
     * name and <code>MyAnnotation</code> {@link #getAnnotation() annotation}.</p>
     */
    public String getName() {
        return key.getName();
    }

    /**
     * Return the <em>annotation</em> of the property represented by this
     * instance.
     *
     * <p>Annotations are either directly-attached to fields, or methods.
     * In the case of methods, all super-classes and interfaces are searched
     * to see if the method is annotated elsewhere.</p>
     */
    public Annotation getAnnotation() {
        return key.getAnnotation();
    }

    /**
     * Returns <b>true</b> if this property can be read from.
     */
    public boolean canRead() {
        return (readers != null) && (readers.size() > 0);
    }

    /**
     * Returns <b>true</b> if this property can be written to.
     */
    public boolean canWrite() {
        return (writers != null) && (writers.size() > 0);
    }

    /* ====================================================================== */
    /* READING                                                                */
    /* ====================================================================== */

    public Object[] readAll(T instance)
    throws IntrospectionException {
        final Object[] values = new Object[this.readers.size()];

        for (int x = 0; x < readers.size(); x ++) try {
            values[x] = readers.get(x).read(instance);
        } catch (IntrospectionException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IntrospectionException("Exception reading %s", this, exception);
        }

        return values;
    }

    public Object read(T instance)
    throws IntrospectionException {
        return read(instance, Object.class);
    }

    public <V> V read(T instance, Class<V> asType)
    throws IntrospectionException {
        try {
            @SuppressWarnings("unchecked")
            final V value = (V) readValue(instance, asType);
            return value;
        } catch (IntrospectionException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IntrospectionException("Exception reading %s", this, exception);
        }
    }

    private Object readValue(T instance, Class<?> asType) {
        if (!canRead()) throw new IntrospectionException("Property %s can not be read", this);

        /* Check for null or "void" types */
        if (asType == null) throw new IntrospectionException("Property %s can not read null type", this);
        if (asType.equals(void.class) || asType.equals(Void.class))
            throw new IntrospectionException("Property %s can not read void type", this);

        /* Get the Result, if any */
        final Result result = readResult(instance, asType);
        if (result != null) return result.value;

        /* Nothing matches, we need to fail */
        final StringBuilder builder = new StringBuilder("Unable to convert from \"");
        String start = "";
        for (IntrospectorReader reader: this.readers) {
            builder.append(start).append(reader.getType().getName()).append('"');
            start = ", \"";
        }
        builder.append(" to type \"").append(asType.getName()).append('"');
        throw new IntrospectionException(builder.append(" reading %s").toString(), this);
    }

    private Result readResult(T instance, Class<?> asType) {

        /* Check for primitives */
        if (asType.equals(boolean.class)) return readResult(instance, Boolean.class);
        if (asType.equals(byte.class))    return readResult(instance, Byte.class);
        if (asType.equals(short.class))   return readResult(instance, Short.class);
        if (asType.equals(int.class))     return readResult(instance, Integer.class);
        if (asType.equals(long.class))    return readResult(instance, Long.class);
        if (asType.equals(float.class))   return readResult(instance, Float.class);
        if (asType.equals(double.class))  return readResult(instance, Double.class);
        if (asType.equals(char.class))    return readResult(instance, Character.class);

        /* Check if we can directly return the value */
        final Result straightResult = readOrCast(instance, asType);
        if (straightResult != null) return straightResult;

        /* Number conversion (make sure we don't get into an infinite loop) */
        if (Number.class.isAssignableFrom(asType) && (!asType.equals(Number.class))) {
            final Result result = readResult(instance, Number.class);
            if (result == null) return null;
            if (result.value == null) return NULL_RESULT;
            final Number number = (Number) result.value;
            return asType.equals(Byte.class)    ? new Result(Byte   .valueOf(number.byteValue()))   :
                   asType.equals(Short.class)   ? new Result(Short  .valueOf(number.shortValue()))  :
                   asType.equals(Integer.class) ? new Result(Integer.valueOf(number.intValue()))    :
                   asType.equals(Long.class)    ? new Result(Long   .valueOf(number.longValue()))   :
                   asType.equals(Float.class)   ? new Result(Float  .valueOf(number.floatValue()))  :
                   asType.equals(Double.class)  ? new Result(Double .valueOf(number.doubleValue())) :
                   null;
        }

        /* Boolean strings */
        else if (asType.equals(Boolean.class)) {
            final Result result = readResult(instance, String.class);
            if (result != null) {
                final String string = (String) result.value;
                if (string == null) return NULL_RESULT; // be kind, null string, null boolean
                final String booleanString = string.trim().toLowerCase();
                return "true" .equals(booleanString) ? TRUE_RESULT  :
                       "false".equals(booleanString) ? FALSE_RESULT :
                       null;
            }
        }

        /* Strings conversion */
        else if (asType.equals(String.class)) {
            final Result result = readResult(instance, Object.class);
            return result == null ? null :
                   result.value == null ? NULL_RESULT :
                   new Result(result.value.toString());
        }

        /* Nothing we can do, unfortunately */
        return null;
    }

    private Result readOrCast(T instance, Class<?> asType) {

        /*
         * Direct matching *FIRST*, in case two setters specify the same
         * property but one takes (for example) Long, and another Number
         * (and we're writing a "Long")
         */
        for (IntrospectorReader reader: this.readers) {
            final Class<?> type = reader.getType();
            if (type.equals(asType)) {
                return new Result(reader.read(instance));
            }
        }

        /* Assignability matching *AFTER* */
        for (IntrospectorReader reader: this.readers) {
            final Class<?> type = reader.getType();
            if (asType.isAssignableFrom(type)) {
                return new Result(reader.read(instance));
            }
        }

        /* Nope, nada, zero */
        return null;
    }

    /* ====================================================================== */
    /* WRITING                                                                */
    /* ====================================================================== */

    public void write(T instance, Object value)
    throws IntrospectionException {
        try {
            writeValue(instance, value);
        } catch (IntrospectionException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IntrospectionException("Exception writing %s", this, exception);
        }
    }

    private void writeValue(T instance, Object value) {
        if (!canWrite()) throw new IntrospectionException("Property %s can not be written", this);

        /* Nulls are relatively easy */
        if (value == null) {
            writeNull(instance);
            return;
        }

        /* Can we write directly or cast? */
        if (writeOrCast(instance, value)) return;

        /* Primitives matching, auto-boxed from their Object class */
        if (writePrimitive(instance, value)) return;

        /* Number conversion */
        if (writeNumber(instance, value)) return;

        /* String assignment */
        final String string = value.toString();
        final String normalized = string.toLowerCase().trim();

        /* Boolean from string? */
        if (normalized.equals("true") || normalized.equals("false")) {
            final Boolean booleanValue = Boolean.valueOf(normalized);
            if (writeOrCast(instance, booleanValue)) return;
            if (writePrimitive(instance, booleanValue)) return;
        }

        /* Number from string? */
        try { if (writeNumber(instance, Long.parseLong(normalized))) return;
        } catch (NumberFormatException exception1) {
        try { if (writeNumber(instance, Double.parseDouble(normalized))) return;
        } catch (NumberFormatException exception2) { /* Nada */ } }

        /* String value */
        if (writeOrCast(instance, string)) return;

        /* Give up */
        throw cannotWrite(value.getClass());
    }

    private void writeNull(T instance) {
        for (IntrospectorWriter writer: this.writers) {
            final Class<?> type = writer.getType();
            if (type.isPrimitive()) continue;
            writer.write(instance, null);
            return;
        }
        throw cannotWrite(null);
    }

    private boolean writeOrCast(T instance, Object value) {

        /*
         * Direct matching *FIRST*, in case two setters specify the same
         * property but one takes (for example) Long, and another Number
         * (and we're writing a "Long")
         */
        for (IntrospectorWriter writer: this.writers) {
            final Class<?> type = writer.getType();
            if (type.equals(value.getClass())) {
                writer.write(instance, value);
                return true;
            }
        }

        /* Assignability matching *AFTER* */
        for (IntrospectorWriter writer: this.writers) {
            final Class<?> type = writer.getType();
            if (type.isInstance(value)) {
                writer.write(instance, value);
                return true;
            }
        }

        /* Nope, nada, zero */
        return false;
    }

    private boolean writePrimitive(T instance, Object value) {
        /* Primitives matching, autoboxed from their Object representation */
        final Class<?> primitive = getPrimitiveType(value.getClass());
        if (primitive == null) return false;
        return writeOrCast(instance, value);
    }

    private boolean writeNumber(T instance, Object value) {
        if (!(value instanceof Number)) return false;

        final Number number = (Number) value;
        for (IntrospectorWriter writer: this.writers) {
            final Class<?> type = writer.getType();
            if (type.equals(long.class) || type.equals(Long.class)) {
                writer.write(instance, number.longValue());
                return true;
            }
            if (type.equals(int.class) || type.equals(Integer.class)) {
                writer.write(instance, number.intValue());
                return true;
            }
            if (type.equals(short.class) || type.equals(Short.class)) {
                writer.write(instance, number.shortValue());
                return true;
            }
            if (type.equals(byte.class) || type.equals(Byte.class)) {
                writer.write(instance, number.byteValue());
                return true;
            }
            if (type.equals(double.class) || type.equals(Double.class)) {
                writer.write(instance, number.doubleValue());
                return true;
            }
            if (type.equals(float.class) || type.equals(Float.class)) {
                writer.write(instance, number.floatValue());
                return true;
            }
        }

        return false;
    }

    private IntrospectionException cannotWrite(Class<?> valueClass) {
        final StringBuilder builder = new StringBuilder("Unable to convert \"")
                                                .append(valueClass == null ? null : valueClass.getName())
                                                .append("\" to");
        String start = " \"";
        for (IntrospectorWriter writer: this.writers) {
            builder.append(start).append(writer.getType().getName()).append('"');
            start = ", \"";
        }
        return new IntrospectionException(builder.append(" writing %s").toString(), this);
    }

    /* ====================================================================== */

    private Class<?> getPrimitiveType(Class<?> type) {
        if (type.equals(Boolean.class)) return Boolean.TYPE;
        return null;
    }

    /* ====================================================================== */

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (object == this) return true;
        try {
            final IntrospectedProperty<?> property = (IntrospectedProperty<?>) object;
            return this.descriptor.getType().equals(property.descriptor.getType()) ?
                           key.equals(property.key) : false;
        } catch (NullPointerException exception) {
            return false;
        } catch (ClassCastException exception) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return key.hashCode() ^ getClass().hashCode() ^ descriptor.getType().hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(this.getClass().getSimpleName());

        char prefix = '[';

        final String name = getName();
        if (name != null) {
            builder.append(prefix).append("name=").append(name);
            prefix = ',';
        }

        final Annotation annotation = getAnnotation();
        if (annotation != null) {
            builder.append(prefix).append("annotation=").append(annotation);
            prefix = ',';
        }

        return builder.append(prefix)
                      .append( "canRead=" ).append(canRead())
                      .append(",canWrite=").append(canWrite())
                      .append("]@")
                      .append(Integer.toHexString(hashCode()))
                      .toString();
    }

    /* ====================================================================== */
    /* THE "RESULT" CLASS, USED WHEN READING TO DISTINGUISH NULLs             */
    /* ====================================================================== */

    private static final Result NULL_RESULT = new Result(null);
    private static final Result TRUE_RESULT = new Result(Boolean.TRUE);
    private static final Result FALSE_RESULT = new Result(Boolean.FALSE);

    private static final class Result {

        private final Object value;

        private Result(Object value) {
            this.value = value;
        }
    }
}
