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
package org.usrz.libs.utils.configurations;

import java.util.Set;

import javax.inject.Inject;

import com.google.inject.Provider;

/**
 * A {@link Provider} used in conjunction with the {@link ConfigurationsBinder}.
 * <p>
 * Apparently, when using <em>private modules</em> and <em>private binders</em>
 * Guice does NOT correctly inject <em>optional</em> dependencies (it seems to
 * leave them to <em>null</em>).
 * <p>
 * The trick is to have at least <em>somehting</em> which is not a standard
 * Guice ojbect being injected into the provider. As we are dealing with
 * configurations, here we <em>require</em> the injection of a
 * {@link Configurations} object, which is defaulted to the
 * {@linkplain Configurations#EMPTY_CONFIGURATIONS empty configurations}
 * by the {@link ConfigurationsBinder}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public abstract class ConfiguredProvider<T> implements Provider<T> {

    protected final Configurations configurations = new Configurations() {

        @Override
        public String getString(Object key, String defaultValue) {
            return internal.get(key, defaultValue);
        }

        @Override
        public Set<Entry<String, String>> entrySet() {
            return internal.entrySet();
        }

        @Override
        public int size() {
            return internal.size();
        }

    };

    private Configurations internal = Configurations.EMPTY_CONFIGURATIONS;

    protected ConfiguredProvider() {
        /* Nothing to do */
    }

    @Inject
    private void injectConfigurations(Configurations configurations) {
        if (configurations != null) this.internal = configurations;
    }

}
