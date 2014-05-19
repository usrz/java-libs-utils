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

import static org.usrz.libs.utils.Check.check;

import java.security.SecureRandom;

/**
 * Various utilities dealing with {@link String}s.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public final class Strings {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int BOUND = ALPHABET.length;

    private Strings() {
        throw new IllegalStateException("Do not construct");
    }

    /**
     * Create random strings using a 62-character alphabet {@code [A-Za-z0-9]}.
     */
    public static String random(int length) {
        final char[] data = new char[check(length, length > 0, "Invalid length " + length)];
        for (int x = 0; x < length; x ++) data[x] = ALPHABET[RANDOM.nextInt(BOUND)];
        return new String(data);
    }

}
