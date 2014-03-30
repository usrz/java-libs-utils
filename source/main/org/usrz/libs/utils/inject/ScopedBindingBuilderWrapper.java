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
import java.util.Objects;

import com.google.inject.Scope;
import com.google.inject.binder.ScopedBindingBuilder;

public class ScopedBindingBuilderWrapper
implements ScopedBindingBuilder {

    protected final ScopedBindingBuilder builder;

    protected ScopedBindingBuilderWrapper(ScopedBindingBuilder builder) {
        this.builder = Objects.requireNonNull(builder, "Null builder");
    }

    @Override
    public final void in(Class<? extends Annotation> scopeAnnotation) {
        builder.in(scopeAnnotation);
    }

    @Override
    public final void in(Scope scope) {
        builder.in(scope);
    }

    @Override
    public final void asEagerSingleton() {
        builder.asEagerSingleton();
    }

}
