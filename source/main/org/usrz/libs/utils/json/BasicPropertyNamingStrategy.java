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
package org.usrz.libs.utils.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategy.PropertyNamingStrategyBase;

public class BasicPropertyNamingStrategy extends PropertyNamingStrategyBase {

    public static final BasicPropertyNamingStrategy INSTANCE = new BasicPropertyNamingStrategy();

    private BasicPropertyNamingStrategy() {
        /* Nothing to do, really */
    }

    @Override
    public String translate(String propertyName) {
        if (propertyName == null) return propertyName;
        final StringBuilder builder = new StringBuilder();

        boolean lastWasUpper = false;
        for (char c: propertyName.toCharArray()) {
            if (Character.isUpperCase(c) || Character.isDigit(c)) {
                if (! lastWasUpper) builder.append('_');
                builder.append(Character.toLowerCase(c));
                lastWasUpper = true;
            } else {
                builder.append(c);
                lastWasUpper = false;
            }
        }
        return builder.toString();
    }

}
