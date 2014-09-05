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
package org.usrz.libs.utils;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.format.DateTimeParseException;

/**
 * Various utilities dealing with times and dates.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public final class Times {

    private Times() {
        throw new IllegalStateException("Do not construct");
    }

    public static Duration duration(String what) {
        final String converted = what.toUpperCase()
                                     .replaceAll("\\s", "")
                                     .replaceAll("^(P)?", "P")
                                     .replaceAll("MIN(UTE)?(S)?", "M")
                                     .replaceAll("H(OU)?R(S)?", "H")
                                     .replaceAll("SEC(OND)?(S)?", "S")
                                     .replaceAll("D(AY(S)?)?(T)?", "DT")
                                     .replaceAll("^P([^T]+)$", "PT$1")
                                     .replaceAll("^P([^T]+)T$", "P$1T0M");
        try {
            return Duration.parse(converted);
        } catch (DateTimeParseException exception) {
            throw new DateTimeException("Invalid duration \"" + what + "\" (" + converted + ")", exception);
        }
    }

    public static String format(Duration duration) {
        if (duration == null) return null;

        final StringBuilder builder = new StringBuilder();

        boolean content = false;
        final long days = duration.toDays();
        if (days > 0) {
            builder.append(days).append(" day");
            if (days > 1) builder.append('s');
            duration = duration.minusDays(days);
            content = true;
        }

        final long hours = duration.toHours();
        if (hours > 0) {
            if (content) builder.append(' ');
            builder.append(hours).append(" hour");
            if (hours > 1) builder.append('s');
            duration = duration.minusHours(hours);
            content = true;
        }

        final long minutes = duration.toMinutes();
        if (minutes > 0) {
            if (content) builder.append(' ');
            builder.append(minutes).append(" minute");
            if (minutes > 1) builder.append('s');
            duration = duration.minusMinutes(minutes);
            content = true;
        }

        final long seconds = duration.getSeconds();
        final int nanos = duration.getNano();
        if (content) {
            if (seconds != 0) {
                builder.append(' ').append(seconds);
                if (nanos != 0) {
                    builder.append(String.format(".%09d", nanos).replaceAll("0+$", ""));
                }
                builder.append(" second");
                if ((seconds != 1) || (nanos > 0)) builder.append('s');
            }
        } else {
            builder.append(seconds);
            if (nanos != 0) {
                builder.append(String.format(".%09d", nanos).replaceAll("0+$", ""));
            }
            builder.append(" second");
            if ((seconds != 1) || (nanos > 0)) builder.append('s');
        }

        return builder.toString();
    }
}
