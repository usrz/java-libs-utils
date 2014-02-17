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

import java.lang.reflect.Field;

import org.testng.annotations.Test;
import org.usrz.libs.logging.Logging;
import org.usrz.libs.testing.AbstractTest;
import org.usrz.libs.utils.beans.BeanBuilder;

public class BeanBuilderTest extends AbstractTest {

    static { Logging.init(); }

    private final BeanBuilder builder = new BeanBuilder();

    @Test
    public void testSimpleBean() {
        final SimpleBean bean = BeanBuilder.newInstance(builder.newClass(SimpleBean.class));

        assertEquals(bean.getKey(), null);
        assertEquals(bean.getValue(), 0);

        bean.setKey("my wonderful key");
        bean.setValue(123456789);

        assertEquals(bean.getKey(), "my wonderful key");
        assertEquals(bean.getValue(), 123456789);

    }

    @Test
    public void testSettableBean()
    throws Exception {
        final SettableBean bean = BeanBuilder.newInstance(builder.newClass(SettableBean.class));

        final Field field = bean.getClass().getDeclaredField("myValue");

        assertEquals(String.class, field.getType());

        field.setAccessible(true);

        assertNull(field.get(bean));

        final String string = new String("hello, world!");
        bean.setMyValue(string);
        assertSame(field.get(bean), string);

    }

    @Test
    public void testGettableBean()
    throws Exception {
        final GettableBean bean = BeanBuilder.newInstance(builder.newClass(GettableBean.class));

        final Field field = bean.getClass().getDeclaredField("myValue");

        assertEquals(String.class, field.getType());

        field.setAccessible(true);

        assertNull(field.get(bean));

        final String string = new String("hello, world");
        field.set(bean, string);
        assertSame(bean.getMyValue(), string);

    }

    @Test
    public void testGettableSettableCombined()
    throws Exception {
        final GettableBean gettable = BeanBuilder.newInstance(builder.newClass(GettableBean.class, SettableBean.class));
        final SettableBean settable = (SettableBean) gettable;

        assertEquals(gettable.getMyValue(), null);
        settable.setMyValue("my wonderful value");
        assertEquals(gettable.getMyValue(), "my wonderful value");

    }


    @Test
    public void testBuilderBean()
    throws Exception {
        final Builder b = BeanBuilder.newInstance(builder.newClass(Builder.class));

        assertEquals(b.getSomething(), 0);
        b.setSomething(-1);
        assertEquals(b.getSomething(), -1);
        b.build(); // should not throw

    }

    @Test
    public void testBridgeClass() {
        final BridgeClass bridge = BeanBuilder.newInstance(builder.newClass(BridgeClass.class));

        assertEquals(bridge.getGeneric(), null);
        bridge.setGeneric("my first value");
        assertEquals(bridge.getGeneric(), "my first value");
        bridge.overrideGeneric("my second value");
        assertEquals(bridge.getGeneric(), "my second value");

    }

    @Test
    public void testBonanza() {
        final BridgeClass bridge = BeanBuilder.newInstance(builder.newClass(BridgeClass.class, SimpleBean.class, GettableBean.class, SettableBean.class));

        assertEquals(bridge.getGeneric(), null);
        bridge.setGeneric("my first value");
        assertEquals(bridge.getGeneric(), "my first value");
        bridge.overrideGeneric("my second value");
        assertEquals(bridge.getGeneric(), "my second value");

        final GettableBean gettable = (GettableBean) bridge;
        final SettableBean settable = (SettableBean) bridge;

        assertEquals(gettable.getMyValue(), null);
        settable.setMyValue("my wonderful value");
        assertEquals(gettable.getMyValue(), "my wonderful value");

        final SimpleBean bean = (SimpleBean) bridge;

        assertEquals(bean.getKey(), null);
        assertEquals(bean.getValue(), 0);

        bean.setKey("my wonderful key");
        bean.setValue(123456789);

        assertEquals(bean.getKey(), "my wonderful key");
        assertEquals(bean.getValue(), 123456789);
    }

    @Test(expectedExceptions=IllegalStateException.class,
          expectedExceptionsMessageRegExp="^Field \"value\" types mismatch.*")
    public void testIncompatibleInterfaces() {
        BeanBuilder.newInstance(builder.newClass(SimpleBean.class, IncompatibleBean.class));
    }

    @Test
    public void testConstructableBean() {
        final Class<ConstructableBean> clazz = builder.newClass(ConstructableBean.class);

        final ConstructableBean bean1 = BeanBuilder.newInstance(clazz, "hello world", new Integer(123));
        final Object object1 = new Object();
        assertEquals(bean1.getString(), "hello world");
        assertEquals(bean1.getNumber(), new Integer(123));
        assertEquals(bean1.getObject(), null);
        bean1.setObject(object1);
        assertSame(bean1.getObject(), object1);

        final ConstructableBean bean2 = BeanBuilder.newInstance(clazz, "hello world", null);
        final Object object2 = new Object();
        assertEquals(bean2.getString(), "hello world");
        assertEquals(bean2.getNumber(), null);
        assertEquals(bean2.getObject(), null);
        bean2.setObject(object2);
        assertSame(bean2.getObject(), object2);

        final ConstructableBean bean3 = BeanBuilder.newInstance(clazz, null, new Integer(123));
        final Object object3 = new Object();
        assertEquals(bean3.getString(), null);
        assertEquals(bean3.getNumber(), new Integer(123));
        assertEquals(bean3.getObject(), null);
        bean3.setObject(object3);
        assertSame(bean3.getObject(), object3);

        final ConstructableBean bean4 = BeanBuilder.newInstance(clazz, null, null);
        final Object object4 = new Object();
        assertEquals(bean4.getString(), null);
        assertEquals(bean4.getNumber(), null);
        assertEquals(bean4.getObject(), null);
        bean4.setObject(object4);
        assertSame(bean4.getObject(), object4);

    }
}
