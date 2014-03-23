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

import java.io.Serializable;
import java.lang.annotation.Annotation;

import org.usrz.libs.utils.inject.ModuleSupport;

public abstract class ConfigurationsModule extends ModuleSupport {

    protected ConfigurationsBindingBuilder configure(Class<?> type) {
        if (type == null) throw new NullPointerException("Null type");
        return new ConfigurationsBindingBuilder(new ConfigurationImpl(type));
    }

    public class ConfigurationsBindingBuilder {

        private final Annotation annotation;

        private ConfigurationsBindingBuilder(Annotation annotation) {
            if (annotation == null) throw new NullPointerException("Null annotation");
            this.annotation = annotation;
        }

        public ConfigurationsModule with(Configurations configurations) {
            if (configurations == null) throw new NullPointerException("Null configurations");
            binder().bind(Configurations.class).annotatedWith(annotation).toInstance(configurations);
            return ConfigurationsModule.this;
        }

    }

    @SuppressWarnings("all")
    private static final class ConfigurationImpl implements Configuration, Serializable {

        private static final long serialVersionUID = 0;
        private final Class<?> type;

        private ConfigurationImpl(Class<?> type) {
            this.type = type;
        }

        @Override
        public Class<?> value() {
            return type;
        }

        @Override
        public int hashCode() {
          // This is specified in java.lang.Annotation.
          return (127 * "value".hashCode()) ^ type.hashCode();
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) return true;
            if (object == null) return false;
            try {
                return type.equals(((Configuration) object).value());
            } catch (ClassCastException exception) {
                return false;
            }
        }

        @Override
        public String toString() {
          return "@" + Configuration.class.getName() + "(value=" + type + ")";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
          return Configuration.class;
        }

    }

}
