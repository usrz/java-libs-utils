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
package org.usrz.libs.utils.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

public class GuiceObjectMapperProvider implements Provider<ObjectMapper> {

    private final Injector injector;

    @Inject
    public GuiceObjectMapperProvider(Injector injector) {
        this.injector = injector;
    }

    @Override
    public ObjectMapper get() {
        final ObjectMapper mapper = new ObjectMapper();
        final GuiceAnnotationIntrospector guiceIntrospector = new GuiceAnnotationIntrospector();
        mapper.setInjectableValues(new GuiceInjectableValues(injector));
        mapper.setAnnotationIntrospectors(
            new AnnotationIntrospectorPair(
                guiceIntrospector, mapper.getSerializationConfig().getAnnotationIntrospector()
            ),
            new AnnotationIntrospectorPair(
                guiceIntrospector, mapper.getDeserializationConfig().getAnnotationIntrospector()
            )
        );

        return mapper;
    }

}