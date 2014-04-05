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

import java.lang.annotation.Annotation;

import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;

public abstract class AnnotatedBindingBuilderWrapper<T,
                                                     L extends LinkedBindingBuilder<T>,
                                                     S extends ScopedBindingBuilder>
extends LinkedBindingBuilderWrapper<T, S>
implements AnnotatedBindingBuilderWithTypes<T, L, S> {

    protected final AnnotatedBindingBuilder<T> builder;

    protected AnnotatedBindingBuilderWrapper(AnnotatedBindingBuilder<T> builder) {
        super(builder);
        this.builder = builder;
    }

    /* ====================================================================== */

    protected abstract L newLinkedBindingBuilder(LinkedBindingBuilder<T> builder);

    /* ====================================================================== */

    @Override
    public L annotatedWith(Class<? extends Annotation> annotationType) {
        return newLinkedBindingBuilder(builder.annotatedWith(annotationType));
    }

    @Override
    public L annotatedWith(Annotation annotation) {
        return newLinkedBindingBuilder(builder.annotatedWith(annotation));
    }

}