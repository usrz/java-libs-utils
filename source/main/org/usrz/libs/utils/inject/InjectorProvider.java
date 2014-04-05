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

import java.util.Objects;

import javax.inject.Inject;

import com.google.inject.Injector;
import com.google.inject.Provider;

public abstract class InjectorProvider<T> implements Provider<T> {

    private Injector injector;

    protected InjectorProvider() {
        /* Nothing to do */
    }

    @Inject
    private final void setInjector(Injector injector) {
        this.injector = injector;
    }

    @Override
    public T get() {
        return this.get(Objects.requireNonNull(injector, "Injector unavailable"));
    }

    protected abstract T get(Injector injector);

}
