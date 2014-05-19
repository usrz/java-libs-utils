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

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class RandomStringTest extends AbstractTest {

    @Test
    public void testRandomness() {
        final Set<String> strings = new HashSet<>();
        for (int x = 0; x < 1000; x ++) {
            assertTrue(strings.add(Strings.random(32)));
        }
    }

    @Test
    public void testLengths() {
        for (int x = 1; x <= 1000; x ++) {
            assertEquals(Strings.random(x).length(), x);
        }
    }

    @Test(expectedExceptions=IllegalArgumentException.class,
          expectedExceptionsMessageRegExp="Invalid length 0")
    public void testInvalidLength0() {
        Strings.random(0);
    }

    @Test(expectedExceptions=IllegalArgumentException.class,
          expectedExceptionsMessageRegExp="Invalid length -1")
    public void testInvalidLengthNegative() {
        Strings.random(-1);
    }
}
