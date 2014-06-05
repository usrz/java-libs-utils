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

import static com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.PASCAL_CASE_TO_CAMEL_CASE;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import javax.inject.Inject;

import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.logging.Log;
import org.usrz.libs.utils.inject.ConfigurableProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.module.guice.GuiceAnnotationIntrospector;
import com.fasterxml.jackson.module.guice.GuiceInjectableValues;
import com.google.inject.Injector;

public class ObjectMapperProvider
extends ConfigurableProvider<ObjectMapper> {

    private static final Log log = new Log();

    @Inject
    private ObjectMapperProvider() {
        super(ObjectMapperConfigurations.class, false);
    }

    protected ObjectMapperProvider(Configurations configurations) {
        super(configurations, false);
    }

    @Override
    protected ObjectMapper get(Injector injector, Configurations configurations) {
        log.debug("Constructing new ObjectMapper instance");

        final PropertyNamingStrategy strategy;
        final String naming = configurations.get("field_naming", "underscores");
        switch (naming.toLowerCase()) {
            case "underscores":         strategy = BasicPropertyNamingStrategy.INSTANCE; break;
            case "jackson_underscores": strategy = CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES; break;
            case "pascal":              strategy = PASCAL_CASE_TO_CAMEL_CASE; break;
            case "camelcase":           // "default" Java naming?
            case "camel_case":          strategy = null; break;
            default: throw new IllegalArgumentException("Invalid value for \"naming\" property \"" + naming + "\"");
        }

        final AnnotationIntrospectorPair introspector =
                new AnnotationIntrospectorPair(
                        new ConstructorPropertiesIntrospector(),
                        new AnnotationIntrospectorPair(
                                new GuiceAnnotationIntrospector(),
                                new JacksonAnnotationIntrospector()));

        return new ObjectMapper()
                     .configure(INDENT_OUTPUT,                  configurations.get("indent", false))
                     .configure(WRITE_DATES_AS_TIMESTAMPS,      configurations.get("use_timestamps", true))
                     .configure(ORDER_MAP_ENTRIES_BY_KEYS,      configurations.get("order_keys", false))
                     .configure(SORT_PROPERTIES_ALPHABETICALLY, configurations.get("order_keys", false))
                     .setInjectableValues(new GuiceInjectableValues(injector))
                     .setAnnotationIntrospector(introspector)
                     .setPropertyNamingStrategy(strategy);

    }

}
