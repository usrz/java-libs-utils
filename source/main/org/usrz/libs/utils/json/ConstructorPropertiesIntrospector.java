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
import java.lang.reflect.Field;

import javax.inject.Qualifier;

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
    public String findImplicitPropertyName(AnnotatedMember annotated) {

        /*
         * If this is a parameter of a @ConstructorProperties annotated constructor,
         * return the property name (wrapped in Jackson's own type).
         */
        final String property = findConstructorPropertyName(annotated);
        if (property == null) return null;

        /* Only return a property name if this is not injectable */
        return findInjectableValueId(annotated) == null ? property : null;
    }

    @Override
    @SuppressWarnings("restriction")
    public Object findInjectableValueId(AnnotatedMember member) {

        /* Check if this is a parameter of a @ConstructorProperties annotated constructor */
        final String property = findConstructorPropertyName(member);
        if (property == null) return null;

        /* Analyse if the field exist, and is annotated with @Inject */
        try {
            final Field field = member.getDeclaringClass().getDeclaredField(property);
            if (field.isAnnotationPresent(javax.inject.Inject.class) ||
                field.isAnnotationPresent(com.google.inject.Inject.class)) {

                /* If we have an @Inject, check for binding annotations */
                for (Annotation annotation : member.annotations()) {

                    /* Check on Guice (BindingAnnotation) & javax (Qualifier) based injections */
                    if (annotation.annotationType().isAnnotationPresent(BindingAnnotation.class) ||
                        annotation.annotationType().isAnnotationPresent(Qualifier.class)) {

                        /* If we found a binding annotation, return the key */
                        return Key.get(member.getGenericType(), annotation);
                    }
                }

                /* No binding annotation, just the generic type */
                return Key.get(member.getGenericType());
            }

        } catch (NoSuchFieldException exception) {
            /* Ignore this, really */
        }

        /* Field not existing, or not annotated with @Inject */
        return null;
    }

    @Override
    public boolean hasCreatorAnnotation(Annotated annotated) {

        /* Just check this is a @ConstructorPrioerties annotated constructor */
        return findConstructorProperties(annotated) != null;

    }
}
