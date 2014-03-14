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

import static com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperTest extends AbstractTest {

    private void testObjectMapper(ClassBuilder builder)
    throws Exception {
        final Class<SimpleBean> beanClass = builder.newClass(SimpleBean.class);

        final ObjectMapper mapper = new ObjectMapper()
                                          .configure(INDENT_OUTPUT,                  false)
                                          .configure(WRITE_DATES_AS_TIMESTAMPS,      true)
                                          .configure(ORDER_MAP_ENTRIES_BY_KEYS,      true)
                                          .configure(SORT_PROPERTIES_ALPHABETICALLY, true)
                                          .setPropertyNamingStrategy(CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        SimpleBean bean = InstanceBuilder.newInstance(beanClass);
        bean.setName("fooName");
        bean.setDescription("fooDescription");
        assertEquals(mapper.writeValueAsString(bean), "{\"description\":\"fooDescription\",\"name\":\"fooName\"}");

        bean = mapper.readValue("{\"name\":\"barName\",\"description\":\"barDescription\"}", beanClass);
        assertEquals(bean.getName(), "barName");
        assertEquals(bean.getDescription(), "barDescription");

        mapper.readerForUpdating(bean).readValue("{\"name\":\"bazName\",\"description\":\"bazDescription\"}");
        assertEquals(bean.getName(), "bazName");
        assertEquals(bean.getDescription(), "bazDescription");

    }

    @Test
    public void testObjectMapperWithBeanBuilder()
    throws Exception {
        testObjectMapper(new BeanBuilder());
    }

    @Test
    public void testObjectMapperWithMapperBuilder()
    throws Exception {
        testObjectMapper(new MapperBuilder());
    }

    public static interface SimpleBean {

        public String getName();

        public void setName(String name);

        public String getDescription();

        public void setDescription(String description);

    }

}
