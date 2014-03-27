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

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class DefaultMethodsTest extends AbstractTest {

    @Test
    public void testDefaultMethod() {
        final Sample sample1 = InstanceBuilder.newInstance(new BeanBuilder().<Sample>newClass(Sample.class));
        sample1.setPrefix("foo").setSuffix("bar");

        assertEquals(sample1.combine(), "foobar");
        assertEquals(sample1.getReversed(), "barfoo");

        final Sample sample2 = InstanceBuilder.newInstance(new MapperBuilder().<Sample>newClass(Sample.class));
        sample2.setPrefix("foo").setSuffix("bar");

        assertEquals(sample2.combine(), "foobar");
        assertEquals(sample2.getReversed(), "barfoo");

    }

    public static interface Sample {

        // As a standard method
        default String combine() {
            return getPrefix() + getSuffix();
        }

        // As a bean getter
        default String getReversed() {
            return getSuffix() + getPrefix();
        }

        public String getPrefix();

        public String getSuffix();

        public Sample setPrefix(String prefix);

        public Sample setSuffix(String suffix);

    }
}
