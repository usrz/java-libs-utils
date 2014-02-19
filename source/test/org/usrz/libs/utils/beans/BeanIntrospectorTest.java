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

import static org.usrz.libs.utils.beans.InstanceBuilder.newInstance;

import java.util.Map;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class BeanIntrospectorTest extends AbstractTest {

    private final Introspector introspector = new Introspector();
    private final BeanBuilder builder = new BeanBuilder();

    @Test
    public void testIntrospection()
    throws Exception {

        final Class<AnnotatedBean> clazz = builder.newClass(AnnotatedBean.class, SimpleBean.class);
        final IntrospectionDescriptor<AnnotatedBean> descriptor = introspector.getDescriptor(clazz);
        final AnnotatedBean annotated = newInstance(clazz);
        final SimpleBean simple = (SimpleBean) annotated;

        /* Show off what we've got */
        descriptor.describe(System.err);

        /* Everyone has this, our class */
        assertSame(descriptor.getProperty("class").read(annotated), clazz);

        /* Start with the "SimpleBean" interface (key/value, both strings) */
        IntrospectedProperty<AnnotatedBean> key = descriptor.getProperty("key");
        IntrospectedProperty<AnnotatedBean> val = descriptor.getProperty("value");

        assertNull(simple.getKey());
        assertEquals(simple.getValue(), 0);
        key.write(annotated, "first key");
        val.write(annotated, 1);
        assertEquals(simple.getKey(), "first key");
        assertEquals(simple.getValue(), 1);
        simple.setKey("second key");
        simple.setValue(2);
        assertEquals(key.read(annotated, String.class), "second key");
        assertEquals(val.read(annotated, int.class), Integer.valueOf(2));

        /* Writing "value" with String or null should fail (conversion) */
        try { val.write(annotated, "hi"); fail("no throws"); } catch (IntrospectionException e) {}
        try { val.write(annotated, null); fail("no throws"); } catch (IntrospectionException e) {}

        /* annotatedField has a "boolean" setter, and a String getter! */
        IntrospectedProperty<AnnotatedBean> annotatedField = descriptor.getProperty("annotatedField");

        assertNull(annotatedField.read(annotated));
        annotatedField.write(annotated, 123.45); // write straight
        assertEquals(annotatedField.read(annotated), "the value is 123.45");
        annotatedField.write(annotated, 543210); // write an int (number conversion)
        assertEquals(annotatedField.read(annotated), "the value is 543210.0");
        annotatedField.write(annotated, "11.22"); // write a string (conversion 1)
        assertEquals(annotatedField.read(annotated), "the value is 11.22");
        annotatedField.write(annotated, "76543"); // write a string (conversion 2)
        assertEquals(annotatedField.read(annotated), "the value is 76543.0");

        /* See what we get when requesting "SimpleAnnotation" annotated fields */
        final Map<SimpleAnnotation, IntrospectedProperty<AnnotatedBean>> simplyAnnotated = descriptor.getProperties(SimpleAnnotation.class);
        assertEquals(simplyAnnotated.size(), 1);
        for (Map.Entry<SimpleAnnotation, IntrospectedProperty<AnnotatedBean>> entry: simplyAnnotated.entrySet()) {
            assertEquals(entry.getKey().annotationType(), SimpleAnnotation.class);
            assertTrue(entry.getValue().canRead());
            assertTrue(entry.getValue().canWrite());
            assertNull(entry.getValue().read(annotated));

            /* Write "object" get "string" */
            final Object object = new Object();
            entry.getValue().write(annotated, object);
            assertEquals(entry.getValue().read(annotated), object.toString());
        }
    }

    @Test
    public void testPrimitives()
    throws Exception {
        /* Do something different, introspection on interface, rather than class */
        final PrimitivesBean instance = newInstance(builder.<PrimitivesBean>newClass(PrimitivesBean.class));
        final IntrospectionDescriptor<PrimitivesBean> descriptor = introspector.getDescriptor(PrimitivesBean.class);

        /* Show off what we've got */
        descriptor.describe(System.err);

        /* Start simple: booleans! */
        final IntrospectedProperty<PrimitivesBean> booleanProperty = descriptor.getProperty("booleanValue");
        assertFalse(booleanProperty.read(instance, Boolean.class)); // initially false
        booleanProperty.write(instance, "   tRUe   "); // some space, mixed case...
        assertTrue(booleanProperty.read(instance, Boolean.class));
        booleanProperty.write(instance, " false " ); // write false, get string
        assertEquals(booleanProperty.read(instance, String.class), "false");
        booleanProperty.write(instance, true); // write false, get string
        assertEquals(booleanProperty.read(instance, String.class), "true");

        /* Array of numbers to iterate on */
        final Number[] values = new Number[] { new Byte((byte)-12),
                                               new Short((short) 789),
                                               new Integer(68438239),
                                               new Long(32905954670929L),
                                               new Float(645986754.123),
                                               new Double(893475348.905678D) };

        /* All the number properties (also have "char" and "boolean" there) */
        final String[] propertyNames = new String[] { "byteValue",
                                                      "shortValue",
                                                      "intValue",
                                                      "longValue",
                                                      "floatValue",
                                                      "doubleValue" };

        /* Loop! */
        for (String propertyName: propertyNames) {
            final IntrospectedProperty<PrimitivesBean> property = descriptor.getProperty(propertyName);
            assertEquals(propertyName, property.getName());

            /* Initial value should *ALWAYS* be zero */
            assertEquals(property.read(instance, Number.class).intValue(), 0);

            /* Go for the numbers */
            for (Number number: values) {

                /* We'll loose precision, need to convert the number */
                final Number converted;
                switch (propertyName) {
                    case "byteValue"  : converted = new Byte   (number.byteValue())   ; break;
                    case "shortValue" : converted = new Short  (number.shortValue())  ; break;
                    case "intValue"   : converted = new Integer(number.intValue())    ; break;
                    case "longValue"  : converted = new Long   (number.longValue())   ; break;
                    case "floatValue" : converted = new Float  (number.floatValue())  ; break;
                    case "doubleValue": converted = new Double (number.doubleValue()) ; break;
                    default: throw new IllegalStateException("Wrong property " + propertyName);
                }

                /* First with numbers! */
                property.write(instance, number);
                try {
                    assertEquals(property.read(instance, Byte.class),    new Byte   (converted.byteValue()),   "to byte");
                    assertEquals(property.read(instance, Short.class),   new Short  (converted.shortValue()),  "to short");
                    assertEquals(property.read(instance, Integer.class), new Integer(converted.intValue()),    "to int");
                    assertEquals(property.read(instance, Long.class),    new Long   (converted.longValue()),   "to long");
                    assertEquals(property.read(instance, Float.class),   new Float  (converted.floatValue()),  "to float");
                    assertEquals(property.read(instance, Double.class),  new Double (converted.doubleValue()), "to double");

                } catch (AssertionError error) {
                    fail("Processing conversion from " + number.getClass().getSimpleName() + ": " + number + " (converted=" + converted + ") " + error.getMessage(), error);
                }

                /* Then with strings, we write the "converted" value as we only parse long or double when writing */
                property.write(instance, converted.toString());
                try {
                    assertEquals(property.read(instance, Byte.class),    new Byte   (converted.byteValue()),   "to byte");
                    assertEquals(property.read(instance, Short.class),   new Short  (converted.shortValue()),  "to short");
                    assertEquals(property.read(instance, Integer.class), new Integer(converted.intValue()),    "to int");
                    assertEquals(property.read(instance, Long.class),    new Long   (converted.longValue()),   "to long");
                    assertEquals(property.read(instance, Float.class),   new Float  (converted.floatValue()),  "to float");
                    assertEquals(property.read(instance, Double.class),  new Double (converted.doubleValue()), "to double");

                } catch (AssertionError error) {
                    fail("Processing conversion from String representation of " + converted.getClass().getSimpleName() + ": " + number + " (converted=" + converted + ") " + error.getMessage(), error);
                }
            }
        }
    }
}
