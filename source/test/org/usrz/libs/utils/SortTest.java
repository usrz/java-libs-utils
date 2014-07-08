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

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class SortTest extends AbstractTest {

    private static enum Foo { FOO };

    private <T> void test(Sort<T> sort, T field, boolean ascending) {
        assertNotNull(sort);
        assertNotNull(sort.field());
        assertEquals(sort.field(), field, "Field mismatch");
        assertEquals(sort.ascending(), ascending, "Ascending/descending mismatch");
    }

    @Test
    public void testSortString() {
        test(Sort.by("foo"), "foo", true);
        test(Sort.by("foo "), "foo", true);
        test(Sort.by(" foo"), "foo", true);
        test(Sort.by(" foo "), "foo", true);
        test(Sort.by("+foo"), "foo", true);
        test(Sort.by("+foo "), "foo", true);
        test(Sort.by(" +foo"), "foo", true);
        test(Sort.by(" +foo "), "foo", true);
        test(Sort.by("+ foo"), "foo", true);
        test(Sort.by("+ foo "), "foo", true);
        test(Sort.by(" + foo"), "foo", true);
        test(Sort.by(" + foo "), "foo", true);
        test(Sort.by("-foo"), "foo", false);
        test(Sort.by("-foo "), "foo", false);
        test(Sort.by(" -foo"), "foo", false);
        test(Sort.by(" -foo "), "foo", false);
        test(Sort.by("- foo"), "foo", false);
        test(Sort.by("- foo "), "foo", false);
        test(Sort.by(" - foo"), "foo", false);
        test(Sort.by(" - foo "), "foo", false);
    }

    @Test
    public void testSortEnumGoodCase() {
        test(Sort.by("FOO", Foo.class), Foo.FOO, true);
        test(Sort.by("FOO ", Foo.class), Foo.FOO, true);
        test(Sort.by(" FOO", Foo.class), Foo.FOO, true);
        test(Sort.by(" FOO ", Foo.class), Foo.FOO, true);
        test(Sort.by("+FOO", Foo.class), Foo.FOO, true);
        test(Sort.by("+FOO ", Foo.class), Foo.FOO, true);
        test(Sort.by(" +FOO", Foo.class), Foo.FOO, true);
        test(Sort.by(" +FOO ", Foo.class), Foo.FOO, true);
        test(Sort.by("+ FOO", Foo.class), Foo.FOO, true);
        test(Sort.by("+ FOO ", Foo.class), Foo.FOO, true);
        test(Sort.by(" + FOO", Foo.class), Foo.FOO, true);
        test(Sort.by(" + FOO ", Foo.class), Foo.FOO, true);
        test(Sort.by("-FOO", Foo.class), Foo.FOO, false);
        test(Sort.by("-FOO ", Foo.class), Foo.FOO, false);
        test(Sort.by(" -FOO", Foo.class), Foo.FOO, false);
        test(Sort.by(" -FOO ", Foo.class), Foo.FOO, false);
        test(Sort.by("- FOO", Foo.class), Foo.FOO, false);
        test(Sort.by("- FOO ", Foo.class), Foo.FOO, false);
        test(Sort.by(" - FOO", Foo.class), Foo.FOO, false);
        test(Sort.by(" - FOO ", Foo.class), Foo.FOO, false);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testStringMissingAscending() {
        Sort.by("+");
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testStringMissingDescending() {
        Sort.by("-");
    }

    @Test
    public void testSortEnumBadCase() {
        test(Sort.by("fOo", Foo.class), Foo.FOO, true);
        test(Sort.by("fOo ", Foo.class), Foo.FOO, true);
        test(Sort.by(" fOo", Foo.class), Foo.FOO, true);
        test(Sort.by(" fOo ", Foo.class), Foo.FOO, true);
        test(Sort.by("+fOo", Foo.class), Foo.FOO, true);
        test(Sort.by("+fOo ", Foo.class), Foo.FOO, true);
        test(Sort.by(" +fOo", Foo.class), Foo.FOO, true);
        test(Sort.by(" +fOo ", Foo.class), Foo.FOO, true);
        test(Sort.by("+ fOo", Foo.class), Foo.FOO, true);
        test(Sort.by("+ fOo ", Foo.class), Foo.FOO, true);
        test(Sort.by(" + fOo", Foo.class), Foo.FOO, true);
        test(Sort.by(" + fOo ", Foo.class), Foo.FOO, true);
        test(Sort.by("-fOo", Foo.class), Foo.FOO, false);
        test(Sort.by("-fOo ", Foo.class), Foo.FOO, false);
        test(Sort.by(" -fOo", Foo.class), Foo.FOO, false);
        test(Sort.by(" -fOo ", Foo.class), Foo.FOO, false);
        test(Sort.by("- fOo", Foo.class), Foo.FOO, false);
        test(Sort.by("- fOo ", Foo.class), Foo.FOO, false);
        test(Sort.by(" - fOo", Foo.class), Foo.FOO, false);
        test(Sort.by(" - fOo ", Foo.class), Foo.FOO, false);
    }

    @Test
    public void testSortNull() {
        assertNull(Sort.by(""));
        assertNull(Sort.by(" "));
        assertNull(Sort.by(null));

        assertNull(Sort.by("", Foo.class));
        assertNull(Sort.by(" ", Foo.class));
        assertNull(Sort.by(null, Foo.class));
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testEnumWrongValue() {
        Sort.by("bar", Foo.class);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testEnumWrongValueAscending() {
        Sort.by("+bar", Foo.class);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testEnumWrongValueDescending() {
        Sort.by("-bar", Foo.class);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testEnumMissingAscending() {
        Sort.by("+", Foo.class);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void testEnumMissingDescending() {
        Sort.by("-", Foo.class);
    }

}
