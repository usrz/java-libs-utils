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

public class BeanBuilderTest extends AbstractTest {

    static { Logging.init(); }

    private final BeanBuilder builder = new BeanBuilder();

    @Test
    public void testSimpleBean() {
        final SimpleBean bean = InstanceBuilder.newInstance(builder.<SimpleBean>newClass(SimpleBean.class));

        assertEquals(bean.getKey(), null);
        assertEquals(bean.getValue(), 0);

        bean.setKey("my wonderful key");
        bean.setValue(123456789);

        assertEquals(bean.getKey(), "my wonderful key");
        assertEquals(bean.getValue(), 123456789);

    }

    @Test
    public void testPrimitivesBean() {
        final PrimitivesBean bean = InstanceBuilder.newInstance(builder.<PrimitivesBean>newClass(PrimitivesBean.class));

        assertEquals(bean.getBooleanValue(), false);
        assertEquals(bean.getByteValue(), (byte) 0);
        assertEquals(bean.getCharValue(), '\u0000');
        assertEquals(bean.getShortValue(), (short) 0);
        assertEquals(bean.getIntValue(), 0);
        assertEquals(bean.getLongValue(), 0L);
        assertEquals(bean.getFloatValue(), 0.0F);
        assertEquals(bean.getDoubleValue(), 0.0D);

        bean.setBooleanValue(true);
        bean.setByteValue((byte) -10);
        bean.setCharValue('\u1234');
        bean.setShortValue((short) 23456);
        bean.setIntValue(12345678);
        bean.setLongValue(1234567890123456789L);
        bean.setFloatValue(12.34F);
        bean.setDoubleValue(1234.567890123456789D);

        assertEquals(bean.getBooleanValue(), true);
        assertEquals(bean.getByteValue(), (byte) -10);
        assertEquals(bean.getCharValue(), '\u1234');
        assertEquals(bean.getShortValue(), (short) 23456);
        assertEquals(bean.getIntValue(), 12345678);
        assertEquals(bean.getLongValue(), 1234567890123456789L);
        assertEquals(bean.getFloatValue(), 12.34F);
        assertEquals(bean.getDoubleValue(), 1234.567890123456789D);

    }

    @Test
    public void testSettableBean()
    throws Exception {
        final SettableBean bean = InstanceBuilder.newInstance(builder.<SettableBean>newClass(SettableBean.class));

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
        final GettableBean bean = InstanceBuilder.newInstance(builder.<GettableBean>newClass(GettableBean.class));

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
        final GettableBean gettable = InstanceBuilder.newInstance(builder.<GettableBean>newClass(GettableBean.class, SettableBean.class));
        final SettableBean settable = (SettableBean) gettable;

        assertEquals(gettable.getMyValue(), null);
        settable.setMyValue("my wonderful value");
        assertEquals(gettable.getMyValue(), "my wonderful value");

    }


    @Test
    public void testBuilderBean()
    throws Exception {
        final Builder b = InstanceBuilder.newInstance(builder.<Builder>newClass(Builder.class));

        assertEquals(b.getSomething(), 0);
        b.setSomething(-1);
        assertEquals(b.getSomething(), -1);
        b.build(); // should not throw

    }

    @Test
    public void testBridgeClass() {
        final BridgeClass bridge = InstanceBuilder.newInstance(builder.<BridgeClass>newClass(BridgeClass.class));

        assertEquals(bridge.getGeneric(), null);
        bridge.setGeneric("my first value");
        assertEquals(bridge.getGeneric(), "my first value");
        bridge.overrideGeneric("my second value");
        assertEquals(bridge.getGeneric(), "my second value");

    }

    @Test
    public void testBonanza() {
        final BridgeClass bridge = InstanceBuilder.newInstance(builder.<BridgeClass>newClass(BridgeClass.class, SimpleBean.class, GettableBean.class, SettableBean.class));

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
        InstanceBuilder.newInstance(builder.newClass(SimpleBean.class, IncompatibleBean.class));
    }

    @Test
    public void testConstructableBean() {
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
