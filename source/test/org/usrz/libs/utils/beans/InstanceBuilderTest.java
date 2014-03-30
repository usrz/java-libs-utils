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

public class InstanceBuilderTest extends AbstractTest {

    @Test
    public void testConstructableBean() {
        final BeanBuilder builder = new BeanBuilder();
        final Class<ConstructableBean> clazz = builder.newClass(ConstructableBean.class);

        final ConstructableBean bean1 = InstanceBuilder.newInstance(clazz, "hello world", new Integer(123));
        final Object object1 = new Object();
        assertEquals(bean1.getString(), "hello world");
        assertEquals(bean1.getNumber(), new Integer(123));
        assertEquals(bean1.getObject(), null);
        bean1.setObject(object1);
        assertSame(bean1.getObject(), object1);

        final ConstructableBean bean2 = InstanceBuilder.newInstance(clazz, "hello world", null);
        final Object object2 = new Object();
        assertEquals(bean2.getString(), "hello world");
        assertEquals(bean2.getNumber(), null);
        assertEquals(bean2.getObject(), null);
        bean2.setObject(object2);
        assertSame(bean2.getObject(), object2);

        final ConstructableBean bean3 = InstanceBuilder.newInstance(clazz, null, new Integer(123));
        final Object object3 = new Object();
        assertEquals(bean3.getString(), null);
        assertEquals(bean3.getNumber(), new Integer(123));
        assertEquals(bean3.getObject(), null);
        bean3.setObject(object3);
        assertSame(bean3.getObject(), object3);

        final ConstructableBean bean4 = InstanceBuilder.newInstance(clazz, null, null);
        final Object object4 = new Object();
        assertEquals(bean4.getString(), null);
        assertEquals(bean4.getNumber(), null);
        assertEquals(bean4.getObject(), null);
        bean4.setObject(object4);
        assertSame(bean4.getObject(), object4);

    }
}
