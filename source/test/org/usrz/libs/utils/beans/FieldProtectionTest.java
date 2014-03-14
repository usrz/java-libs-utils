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

public class FieldProtectionTest extends AbstractTest {

    private void testProtection(ClassBuilder builder) {

        /* Check initial state */
        final Class<ProtectedBean> beanClass = builder.newClass(ProtectedBean.class);
        final ProtectedBean bean = InstanceBuilder.newInstance(beanClass);

        assertNotNull(bean);
        assertNull(bean.getNormalValue());
        assertNull(bean.getProtectedValue());

        /* Initial setting to "null" */
        bean.setNormalValue(null);
        try {
            bean.setProtectedValue(null);
            fail("Exception not thrown");
        } catch (IllegalArgumentException exception) {
            assertEquals(exception.getMessage(), "Invalid null value for setter \"setProtectedValue(String)\"");
        }

        assertNull(bean.getNormalValue());
        assertNull(bean.getProtectedValue());

        /* Proper assignment with variables */
        bean.setNormalValue("foo");
        bean.setProtectedValue("bar");
        assertEquals(bean.getNormalValue(), "foo");
        assertEquals(bean.getProtectedValue(), "bar");

        /* Second assignment (for protection test) */
        bean.setNormalValue("baz");
        try {
            bean.setProtectedValue("baz");
            fail("Exception not thrown");
        } catch (IllegalStateException exception) {
            assertEquals(exception.getMessage(), "Protected setter \"setProtectedValue(String)\" already invoked");
        }

        assertEquals(bean.getNormalValue(), "baz");
        assertEquals(bean.getProtectedValue(), "bar");

        /* Third assignment (for protection test / nullability) */
        bean.setNormalValue(null);
        try {
            bean.setProtectedValue(null);
            fail("Exception not thrown");
        } catch (IllegalArgumentException exception) {
            assertEquals(exception.getMessage(), "Invalid null value for setter \"setProtectedValue(String)\"");
        }

        assertEquals(bean.getNormalValue(), null);
        assertEquals(bean.getProtectedValue(), "bar");

    }

    @Test
    public void testBeanBuilderProtection() {
        testProtection(new BeanBuilder());
    }

    @Test
    public void testMapperBuilderProtection() {
        testProtection(new MapperBuilder());
    }

    public static interface ProtectedBean {

        public String getNormalValue();

        public void setNormalValue(String value);

        public String getProtectedValue();

        @Protected @NotNullable
        public void setProtectedValue(String value);

    }

}
