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

import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;


public abstract class BindingModuleSupport extends ModuleSupport {

    protected BindingModuleSupport() {
        /* Nothing to do */
    }

    protected <T> LinkedBindingBuilder<T> bind(Key<T> key) {
        return binder().bind(key);
    }

    protected <T> AnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
        return binder().bind(typeLiteral);
    }

    protected <T> AnnotatedBindingBuilder<T> bind(Class<T> type) {
        return binder().bind(type);
    }

    protected AnnotatedConstantBindingBuilder bindConstant() {
        return binder().bindConstant();
    }

    protected void install(Module... modules) {
        for (Module module: modules) binder().install(module);
    }

}
