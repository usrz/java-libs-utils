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
package org.usrz.libs.utils.json;

import javax.inject.Inject;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Module;

public class ObjectMapperTest extends AbstractTest {

    private static final Object injectableObject = new Object();
    private static final String injectableString = new String("IWasInjected!");
    private static final Number injectableNumber = new Integer(12345);
    private static final Module module = (binder) -> {
        binder.requireExplicitBindings();
        binder.bind(Object.class).toInstance(injectableObject);
        binder.bind(String.class).toInstance(injectableString);
        binder.bind(Number.class).toInstance(injectableNumber);
    };

    private ObjectMapper getMapper(Module module) {
        return Guice.createInjector((binder) -> new ObjectMapperBuilder(binder), module)
                    .getInstance(ObjectMapper.class);
    }

    /* ====================================================================== */

    @Test
    public void testLombok()
    throws Exception {
        val mapper = getMapper(module);
        LombokBean bean;

        bean = mapper.readValue("{}", LombokBean.class);
        log.debug("Empty Bean:  %s", bean);
        assertNotNull(bean);
        assertSame   (bean.constructorInjected, injectableObject);
        assertSame   (bean.methodInjected,      injectableNumber);
        assertSame   (bean.fieldInjected,       injectableString);
        assertNull   (bean.constructorValue);
        assertNull   (bean.methodValue);
        assertNull   (bean.fieldValue);

        bean = mapper.readValue("{\"constructor_value\":\"myConstructorValue\",\"method_value\":\"myMethodValue\",\"field_value\":\"myFieldValue\"}", LombokBean.class);
        log.debug("Values Bean: %s", bean);
        assertNotNull(bean);
        assertSame   (bean.constructorInjected, injectableObject);
        assertSame   (bean.methodInjected,      injectableNumber);
        assertSame   (bean.fieldInjected,       injectableString);
        assertEquals (bean.constructorValue,    "myConstructorValue");
        assertEquals (bean.methodValue,         "myMethodValue");
        assertEquals (bean.fieldValue,          "myFieldValue");

    }

    @Test(expectedExceptions=ConfigurationException.class,
          expectedExceptionsMessageRegExp="(?s).*No implementation for java\\.lang\\.Number was bound.*")
    public void testLombokFailImplementationNotBound()
    throws Exception {
        getMapper((c) -> {}).readValue("{}", LombokBean.class);
    }

    @Test(expectedExceptions=UnrecognizedPropertyException.class,
          expectedExceptionsMessageRegExp="(?s)^Unrecognized field \"constructor_injected\".*")
    public void testLombokFailUnrecognizedProperty()
    throws Exception {
        getMapper(module).readValue("{\"constructor_injected\":\"myConstructorInjected\",\"constructor_value\":\"myConstructorValue\",\"method_value\":\"myMethodValue\",\"field_value\":\"myFieldValue\"}", LombokBean.class);
    }


    @Test
    public void testLombokExt()
    throws Exception {
        val mapper = getMapper(module);
        LombokBean bean;

        bean = mapper.readValue("{}", LombokBeanExt.class);
        log.debug("Empty Bean:  %s", bean);
        assertNotNull(bean);
        assertSame   (bean.constructorInjected, injectableObject);
        assertSame   (bean.methodInjected,      injectableNumber);
        assertSame   (bean.fieldInjected,       injectableString);
        assertNull   (bean.constructorValue); // No @ConstructorPropery, only *specific* values
        assertNull   (bean.methodValue);
        assertNull   (bean.fieldValue);

        bean = mapper.readValue("{\"constructor_value\":\"myConstructorValue\",\"method_value\":\"myMethodValue\",\"field_value\":\"myFieldValue\"}", LombokBeanExt.class);
        log.debug("Values Bean: %s", bean);
        assertNotNull(bean);
        assertSame   (bean.constructorInjected, injectableObject);
        assertSame   (bean.methodInjected,      injectableNumber);
        assertSame   (bean.fieldInjected,       injectableString);
        assertEquals (bean.constructorValue,    "myConstructorValue");
        assertEquals (bean.methodValue,         "myMethodValue");
        assertEquals (bean.fieldValue,          "myFieldValue");
    }

    @Test(expectedExceptions=UnrecognizedPropertyException.class,
          expectedExceptionsMessageRegExp="(?s)^Unrecognized field \"constructor_injected\".*")
    public void testLombokExtFailUnrecognizedProperty()
    throws Exception {
        getMapper(module).readValue("{\"constructor_injected\":\"myConstructorInjected\",\"constructor_value\":\"myConstructorValue\",\"method_value\":\"myMethodValue\",\"field_value\":\"myFieldValue\"}", LombokBeanExt.class);
    }

    /* ====================================================================== */

    @ToString
    @EqualsAndHashCode
    @RequiredArgsConstructor
    private static class LombokBean {

        @Inject
        private String fieldInjected;
        // From the constructor, injected
        @Inject
        private final Object constructorInjected;
        // From the setter, injected
        private Number methodInjected;

        // From the constructor, value
        private final String constructorValue;
        // From the auto-generated setter, value
        @Setter private String methodValue;
        // From the field, simply set value
        @JsonProperty("field_value") private String fieldValue;

        @Inject @JsonIgnore
        private void setNumber(Number number) {
            methodInjected = number;
        }
    }

    private static class LombokBeanExt extends LombokBean {

        @JsonCreator
        public LombokBeanExt(@JacksonInject Object injectedObject,
                             @JsonProperty("constructorValue") String constructorValue) {
            super(injectedObject, constructorValue);
        }

    }
}
