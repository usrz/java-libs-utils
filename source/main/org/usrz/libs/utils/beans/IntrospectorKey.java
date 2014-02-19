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
package org.usrz.libs.utils.beans;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * A {@link Map} <em>key</em> for {@link IntrospectedProperty} instances.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
class IntrospectorKey {

    private final String name;
    private final Annotation annotation;

    /* ====================================================================== */

    IntrospectorKey(String name, Annotation annotation) {

        /* Normalize "empty" property names as NULLs and lower-case the first character */
        if ((name != null) && (name.length() == 0)) name = null;
        if (name != null) name = Character.toLowerCase(name.charAt(0)) + name.substring(1);

        /* Remember what we have */
        this.name = name;
        this.annotation = annotation;
    }

    /* ====================================================================== */

    final String getName() {
        return name;
    }

    final Annotation getAnnotation() {
        return annotation;
    }

    boolean isNull() {
        return (name == null) && (annotation == null);
    }

    String description() {
        final StringBuilder builder = new StringBuilder();
        if (name != null) {
            builder.append("[name=").append(name);
            if (annotation != null) builder.append(",annotation=").append(annotation);
            return builder.append(']').toString();
        } else if (annotation != null) {
            return builder.append("[annotation=").append(annotation).append(']').toString();
        } else {
            return "[name=null,annotation=null]";
        }
    }

    /* ====================================================================== */

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (object == this) return true;
        try {
            final IntrospectorKey key = (IntrospectorKey) object;
            final boolean propertyEquals = name == null ?
                                               key.name == null :
                                               name.equals(key.name);

            return propertyEquals ?
                       annotation == null ?
                           key.annotation == null :
                           annotation.equals(key.annotation) :
                       false;

        } catch (NullPointerException exception) {
            return false;
        } catch (ClassCastException exception) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (name == null   ? 0 : name.hashCode())
             ^ (annotation == null ? 0 : annotation.hashCode())
             ^ this.getClass().hashCode();
    }

    @Override
    public String toString() {
        return new StringBuilder(this.getClass().getSimpleName())
                         .append(description())
                         .append('@')
                         .append(Integer.toHexString(hashCode()))
                         .toString();
    }
}
