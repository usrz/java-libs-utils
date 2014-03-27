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

/**
 * An exception indicating that something went wrong parsing or creating a
 * a {@link Configurations} instance.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class ConfigurationsException extends RuntimeException {

    private String location = null;

    /**
     * Create a new {@link ConfigurationsException} for the specified key.
     */
    protected ConfigurationsException(String message) {
        super(message);
    }

    /**
     * Create a new {@link ConfigurationsException} for the specified key.
     */
    protected ConfigurationsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Initialize the location of this {@link ConfigurationsException}.
     */
    protected ConfigurationsException initLocation(String location) {
        if (location != null) this.location = location;
        return this;
    }

    /**
     * Initialize the cause {@link Throwable} of this exception.
     */
    @Override
    public ConfigurationsException initCause(Throwable cause) {
        super.initCause(cause);
        return this;
    }

    /**
     * Return the location (a <em>file name</em>, <em>URL</em>, ... in
     * {@link String} format) associated with this
     * {@link ConfigurationsException} or <b>null</b>.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Return the message associated with this {@link ConfigurationsException}
     * possibly including location information.
     */
    @Override
    public String getMessage() {
        /* Original message */
        final StringBuilder builder = new StringBuilder(super.getMessage());

        /* Append location if needed */
        if (location != null)  {
            builder.append(" (").append(location).append(")");
        }

        /* Return message */
        return builder.toString();
    }

}
