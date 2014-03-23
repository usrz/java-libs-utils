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

import com.google.inject.Binder;
import com.google.inject.Module;

public abstract class ModuleSupport implements Module {

    private final ThreadLocal<Binder> binder = new ThreadLocal<Binder>();

    @Override
    public void configure(Binder binder) {
        if (binder == null) throw new NullPointerException("Null binder");
        if (this.binder.get() != null) {
            throw new IllegalStateException("Binder already specified in current thread");
        } else try {
            this.binder.set(binder);
            this.configure();
        } finally {
            this.binder.remove();
        }
    }

    public abstract void configure();

    protected final Binder binder() {
        final Binder binder = this.binder.get();
        if (binder == null) throw new IllegalStateException("No binder available");
        return binder;
    }

}
