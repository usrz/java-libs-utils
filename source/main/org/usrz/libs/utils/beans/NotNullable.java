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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The {@link NotNullable} annotation will ensure that generated setters will
 * never accept <em>null</em> values.
 *
 * <p>An example of usage can be outlined as follows:</p>
 *
 * <pre>
 * public interface MyBean {
 *   @NotNullable public void setValue(String value);
 *   public String getValue();
 * }
 * </pre>
 *
 * <p>If the <code>setValue(String value)</code> method is invoked with a
 * <b>null</b> value it will throw an {@link IllegalArgumentException}.</p>
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface NotNullable {

    /* Nothing */

}
