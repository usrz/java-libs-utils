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
        final AnnotatedBean annotated = InstanceBuilder.newInstance(clazz);
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

    }

}
