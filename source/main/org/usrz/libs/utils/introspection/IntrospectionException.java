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
package org.usrz.libs.utils.introspection;

/**
 * An <em>unchecked</em> exception thrown when dealing with bean introspection.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class IntrospectionException extends RuntimeException {

    private final IntrospectedProperty<?> property;

    IntrospectionException(String message) {
        super(message);
        property = null;
    }

    IntrospectionException(String message, Throwable cause) {
        super(message, cause);
        property = null;
    }

    IntrospectionException(String message, IntrospectedProperty<?> property) {
        this(message, property, null);
    }

    IntrospectionException(String message, IntrospectedProperty<?> property, Throwable cause) {
        super(String.format(message, property), cause);
        this.property = property;
    }

    /**
     * Return the {@link IntrospectedProperty} (if any) associated with this.
     */
    public IntrospectedProperty<?> getProperty() {
        return property;
    }

}
