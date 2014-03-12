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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Qualifier;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.google.inject.BindingAnnotation;
import com.google.inject.Key;

public class GuiceAnnotationIntrospector extends NopAnnotationIntrospector {

    public GuiceAnnotationIntrospector() {
        // TODO Auto-generated constructor stub
    }

    /* ====================================================================== */

    private boolean isInjectable(Annotated annotated) {
        return annotated.hasAnnotation(javax.inject.Inject.class) ||
               annotated.hasAnnotation(com.google.inject.Inject.class) ||
               annotated.hasAnnotation(JacksonInject.class);
    }

    private Key<?> getInjectionKey(AnnotatedMember member) {

        /* Check Guice's @BindingAnnotation and Java's @Qualifier annotations */
        for (Annotation annotation : member.annotations()) {
            if (annotation.annotationType().isAnnotationPresent(BindingAnnotation.class) ||
                annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                    return Key.get(member.getGenericType(), annotation);
            }
        }

        /* No BindingAnnotation or Qualifier annotation, just key */
        return Key.get(member.getGenericType());

    }

    /* ====================================================================== */

    @Override
    public boolean hasCreatorAnnotation(Annotated annotated) {
        return (annotated instanceof AnnotatedConstructor) &&
               (isInjectable(annotated) || annotated.hasAnnotation(JsonCreator.class));
    }

    @Override
    public Object findInjectableValueId(AnnotatedMember member) {

        if (member instanceof AnnotatedMethod) {

            /* Only @Inject methods */
            if (!isInjectable(member)) return null;

            /* Check parameter counts for the method */
            final AnnotatedMethod method = ((AnnotatedMethod) member);
            if (method.getParameterCount() != 1) {
                throw new IllegalArgumentException("Only one parameter allowed " + method);
            }

            /*
             * Jackson doesn't seem to parse annotations for method parameters for some
             * reason (only constructors), therefore we need to re-construct the actual
             * parameter, splat the annotations in there, rinse and repeat.
             */
            final Annotation[] annotations = method.getMember().getParameterAnnotations()[0];
            final AnnotationMap map = new AnnotationMap();
            for (Annotation annotation: annotations) map.add(annotation);
            final Type type = method.getParameter(0).getGenericType();

            /* Call ourselves with the parameter (as if we were a constructor) */
            return findInjectableValueId(new AnnotatedParameter(method, type, map, 0));

        }

        if (member instanceof AnnotatedParameter) {

            /* Check the parameter's method, not @Inject? forget about it */
            if (!isInjectable(((AnnotatedParameter) member).getOwner())) return null;

            /* Find the injection key in Guice and return it */
            return getInjectionKey(member);

        }

        if (member instanceof AnnotatedField) {

            /* Only @Inject fields */
            if (! isInjectable(member)) return null;

            /* Find the injection key in Guice and return it */
            return getInjectionKey(member);
        }

        /* */
        return null;
    }

}
