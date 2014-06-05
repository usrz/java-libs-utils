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
package org.usrz.libs.utils.inject;

import static org.usrz.libs.utils.Check.notNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public abstract class BindingBuilder {

    private final Binder binder;
    private final Annotation annotation;
    private final Class<? extends Annotation> annotationType;

    /* ====================================================================== */

    private BindingBuilder(Binder binder, Annotation annotation, Class<? extends Annotation> annotationType) {
        this.binder = notNull(binder, "Null binder").skipSources(getClass());
        this.annotation = annotation;
        this.annotationType = annotationType;
    }

    protected BindingBuilder(Binder binder) {
        this(binder, null, null);
    }

    protected BindingBuilder(Binder binder, Annotation annotation) {
        this(binder, annotation, null);
    }

    protected BindingBuilder(Binder binder, Class<? extends Annotation> annotationType) {
        this(binder, null, annotationType);
    }

    /* ====================================================================== */

    protected final Binder binder() {
        return binder;
    }

    protected final <T> Key<T> key(Class<T> type) {
        if (annotation != null) return Key.get(type, annotation);
        if (annotationType != null) return Key.get(type, annotationType);
        return Key.get(type);
    }

    protected final <T> Key<T> key(TypeLiteral<T> type) {
        if (annotation != null) return Key.get(type, annotation);
        if (annotationType != null) return Key.get(type, annotationType);
        return Key.get(type);
    }

    protected final Key<?> key(Type type) {
        if (annotation != null) return Key.get(type, annotation);
        if (annotationType != null) return Key.get(type, annotationType);
        return Key.get(type);
    }

}
