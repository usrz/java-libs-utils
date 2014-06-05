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

import java.beans.ConstructorProperties;
import java.lang.annotation.Annotation;

import javax.inject.Qualifier;

import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.google.inject.BindingAnnotation;
import com.google.inject.Key;

public class ConstructorPropertiesIntrospector extends NopAnnotationIntrospector {

    public ConstructorPropertiesIntrospector() {
        /* Nothing to do */
    }

    /* ====================================================================== */

    private String findConstructorPropertyName(Annotated annotated) {

        /* We operate only on (constructor) parameters */
        if (annotated instanceof AnnotatedParameter) {
            final AnnotatedParameter parameter = (AnnotatedParameter) annotated;
            final String[] properties = findConstructorProperties(parameter.getOwner());

            /* If we have the properties, return the one at the right index */
            if (properties != null) return properties[parameter.getIndex()];
        }

        /* Not a parameter */
        return null;
    }

    private String[] findConstructorProperties(Annotated annotated) {

        /* We operate only on constructors */
        if (annotated instanceof AnnotatedConstructor) {
            final AnnotatedConstructor constructor = (AnnotatedConstructor) annotated;

            /* Check if we have our @ConstructorProperties annotation */
            final ConstructorProperties properties = constructor.getAnnotation(ConstructorProperties.class);
            if (properties == null) return null;

            /* Triple check the parameter counts against the annotation values */
            final String[] names = properties.value();
            if (constructor.getParameterCount() != names.length) {
                throw new RuntimeJsonMappingException("Constructor " + constructor + " defines " + constructor.getParameterCount() +
                                            " parameters, but @ConstructorProperties annotation only defines " + names.length);
            }

            /* Done */
            return properties.value();
        }

        /* Not a constructor */
        return null;
    }

    /* ====================================================================== */

    @Override
    public PropertyName findNameForDeserialization(Annotated annotated) {

        /*
         * If this is a parameter of a @ConstructorProperties annotated constructor,
         * return the property name (wrapped in Jackson's own type).
         */
        final String property = findConstructorPropertyName(annotated);
        return property == null ? null : new PropertyName(property);
    }

    @Override
    public Object findInjectableValueId(AnnotatedMember member) {

        /* Check if this is a parameter of a @ConstructorProperties annotated constructor */
        if (findConstructorPropertyName(member) != null) {
            for (Annotation annotation : member.annotations()) {

                /* Check on Guice (BindingAnnotation) & javax (Qualifier) based injections */
                if (annotation.annotationType().isAnnotationPresent(BindingAnnotation.class) ||
                    annotation.annotationType().isAnnotationPresent(Qualifier.class)) {

                    /* If we found a binding annotation, return the key */
                    return Key.get(member.getGenericType(), annotation);
                }
            }

            /* No binding annotation, only generic type */
            return Key.get(member.getGenericType());
        }

        /* Not a parameter, not annotated with @ConstructorProperties, ... */
        return null;
    }

    @Override
    public boolean hasCreatorAnnotation(Annotated annotated) {

        /* Just check this is a @ConstructorPrioerties annotated constructor */
        return findConstructorProperties(annotated) != null;

    }
}
