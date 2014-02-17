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

import java.util.Map;

import org.testng.annotations.Test;
import org.usrz.libs.logging.Logging;
import org.usrz.libs.testing.AbstractTest;

public class MapperBuilderTest extends AbstractTest {

    static { Logging.init(); }

    private final MapperBuilder builder = new MapperBuilder();

    /* ====================================================================== */

    private static void assertNullField(Object mapperObject, String key) {
        final Mapper mapper = (Mapper) mapperObject;
        final Map<String, ?> map = mapper.mappedProperties();
        assertNotNull(map, "Null map instance from mapper");

        assertFalse(map.containsKey(key), "Key \"" + key + "\" is present in mapped properties");

        final Object value = map.get(key);
        assertNull(value, "Key \"" + key + "\" returned non-null value " + value);
    }

    private static Object assertEqualsField(Object mapperObject, String key, Object value) {
        final Mapper mapper = (Mapper) mapperObject;
        final Map<String, ?> map = mapper.mappedProperties();
        assertNotNull(map, "Null map instance from mapper");

        assertTrue(map.containsKey(key), "Key \"" + key + "\" is present in mapped properties");
        assertEquals(map.get(key), value);
        return map.get(key);
    }

    /* ====================================================================== */

    @Test
    public void testMapperSimpleBean() {
        final SimpleBean bean = InstanceBuilder.newInstance(builder.<SimpleBean>newClass(SimpleBean.class));

        assertNullField(bean, "key");
        assertNullField(bean, "value");

        assertNull(bean.getKey());
        try { bean.getValue(); fail("No NPE"); } catch (NullPointerException e) { /* ok */ }

        bean.setKey("my wonderful key");
        bean.setValue(123);

        assertEqualsField(bean, "key", "my wonderful key");
        assertEqualsField(bean, "value", 123);

        assertEquals(bean.getKey(), "my wonderful key");
        assertEquals(bean.getValue(), 123);
    }

    @Test
    public void testSimpleBeanForceNull() {
        final SimpleBean bean = InstanceBuilder.newInstance(builder.<SimpleBean>newClass(SimpleBean.class));
        assertNullField(bean, "key");
        assertNull(bean.getKey());
        bean.setKey(null); /* FORCE A "NULL" VALUE FOR KEY */
        assertEqualsField(bean, "key", null);
        assertEquals(bean.getKey(), null);
    }

    @Test
    public void testPrimitivesBean() {
        final PrimitivesBean bean = InstanceBuilder.newInstance(builder.<PrimitivesBean>newClass(PrimitivesBean.class));

        try { bean.getBooleanValue(); fail("No NPE"); } catch (NullPointerException e) { /* ok */ }
        try { bean.getByteValue();    fail("No NPE"); } catch (NullPointerException e) { /* ok */ }
        try { bean.getCharValue();    fail("No NPE"); } catch (NullPointerException e) { /* ok */ }
        try { bean.getShortValue();   fail("No NPE"); } catch (NullPointerException e) { /* ok */ }
        try { bean.getIntValue();     fail("No NPE"); } catch (NullPointerException e) { /* ok */ }
        try { bean.getLongValue();    fail("No NPE"); } catch (NullPointerException e) { /* ok */ }
        try { bean.getFloatValue();   fail("No NPE"); } catch (NullPointerException e) { /* ok */ }
        try { bean.getDoubleValue();  fail("No NPE"); } catch (NullPointerException e) { /* ok */ }

        assertNullField(bean, "booleanValue");
        assertNullField(bean, "byteValue");
        assertNullField(bean, "charValue");
        assertNullField(bean, "shortValue");
        assertNullField(bean, "intValue");
        assertNullField(bean, "longValue");
        assertNullField(bean, "floatValue");
        assertNullField(bean, "doubleValue");

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

        assertEqualsField(bean, "booleanValue", true);
        assertEqualsField(bean, "byteValue", (byte) -10);
        assertEqualsField(bean, "charValue", '\u1234');
        assertEqualsField(bean, "shortValue", (short) 23456);
        assertEqualsField(bean, "intValue", 12345678);
        assertEqualsField(bean, "longValue", 1234567890123456789L);
        assertEqualsField(bean, "floatValue", 12.34F);
        assertEqualsField(bean, "doubleValue", 1234.567890123456789D);
    }

    @Test
    public void testSettableBean()
    throws Exception {
        final SettableBean bean = InstanceBuilder.newInstance(builder.<SettableBean>newClass(SettableBean.class));

        assertNullField(bean, "myValue");

        final String string = "hello world";
        bean.setMyValue(string);

        assertSame(assertEqualsField(bean, "myValue", string), string);
    }

    @Test
    public void testGettableBean()
    throws Exception {
        final GettableBean bean = InstanceBuilder.newInstance(builder.<GettableBean>newClass(GettableBean.class));

        assertNullField(bean, "myValue");
        assertNull(bean.getMyValue());

        final String string = "hello world";
        ((Mapper) bean).mappedProperties().put("myValue", string);

        assertSame(assertEqualsField(bean, "myValue", string), string);
        assertSame(bean.getMyValue(), string);
    }

    @Test
    public void testGettableSettableCombined()
    throws Exception {
        final GettableBean gettable = InstanceBuilder.newInstance(builder.<GettableBean>newClass(GettableBean.class, SettableBean.class));
        final SettableBean settable = (SettableBean) gettable;

        assertEquals(gettable.getMyValue(), null);
        final String string = "my wonderful value";
        settable.setMyValue(string);
        assertSame(gettable.getMyValue(), string);
    }

    @Test
    public void testBuilderBean()
    throws Exception {
        final Builder b = InstanceBuilder.newInstance(builder.<Builder>newClass(Builder.class));

        try { b.getSomething(); fail("No NPE"); } catch (NullPointerException e) { /* ok */ }
        assertNullField(b, "something");

        final Builder b2 = b.setSomething(-1);
        assertSame(b2, b);

        assertEquals(b.getSomething(), -1);
        assertEqualsField(b, "something", -1);

        b.build(); // should not throw

    }

}
