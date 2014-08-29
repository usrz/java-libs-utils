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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class AnnotationsTest extends AbstractTest {

    private final Pattern annotationString = Pattern.compile("^\\@([^\\(]+)\\([^\\)]*\\)$");
    private final Pattern annotationDetails = Pattern.compile("^\\@[^\\(]+\\(([^\\)]*)\\)$");
    private final Pattern annotationComponent = Pattern.compile("(\\w+)=(([^\\[][^,]+)|(\\[[^\\]]*\\]))(, |$)");

    private Map<String, String> parseComponents(String string) {
        final Map<String, String> components = new HashMap<>();
        final Matcher matcher = annotationComponent.matcher(string);
        while(matcher.find()) {
            components.put(matcher.group(1), matcher.group(2));
        }
        return components;
    }

    private <A extends Annotation> void assertAnnotation(A loaded, A constructed) {
        assertNotNull(loaded,      "Null loaded annotation");
        assertNotNull(constructed, "Null constructed annotation");

        /* Basic checks */
        assertEquals(constructed.annotationType(),    loaded.annotationType(),    "annotationType() mismatch");
        assertEquals(constructed.hashCode(),          loaded.hashCode(),          "hashCode() mismatch");
        //assertEquals(constructed.toString(),          loaded.toString(),          "toString() mismatch");

        /* String check */
        final String cString = constructed.toString();
        final String lString = loaded.toString();

        /* Must have same length */
        assertEquals(cString.length(), lString.length(), "toString().length() mismatch");

        /* The part @my.nice.MyAnnotation(...) must match */
        final Matcher cAnnotationStringMatcher = annotationString.matcher(cString);
        final Matcher lAnnotationStringMatcher = annotationString.matcher(lString);
        assertTrue(cAnnotationStringMatcher.matches(), "Constructed annotation toString() does not match outside pattern: " + cString);
        assertTrue(lAnnotationStringMatcher.matches(),      "Loaded annotation toString() does not match outside pattern: " + lString);
        assertEquals(cAnnotationStringMatcher.group(1), lAnnotationStringMatcher.group(1), "Wrong toString result (outside)");

        /* Parse out details @...(foo=bar, baz=123): DO NOT use commas or square brackets in test annotation attributes!!! */
        final Matcher cAnnotationDetailsMatcher = annotationDetails.matcher(cString);
        final Matcher lAnnotationDetailsMatcher = annotationDetails.matcher(lString);
        assertTrue(cAnnotationDetailsMatcher.matches(), "Constructed annotation toString() does not match inside pattern: " + cString);
        assertTrue(lAnnotationDetailsMatcher.matches(),      "Loaded annotation toString() does not match inside pattern: " + lString);

        /* One by one, get the various string components */
        final String cDetails = cAnnotationDetailsMatcher.group(1);
        final String lDetails = lAnnotationDetailsMatcher.group(1);

        final Map<String, String> cComponents = parseComponents(cDetails);
        final Map<String, String> lComponents = parseComponents(lDetails);

        assertEquals(cComponents, lComponents, "Invalid components:\n  constructed => \"" + cDetails + "\"\n       loaded => \"" + lDetails + "\"\n");

        /* Equality check */
        assertTrue(constructed.equals(loaded), "Constructed annotation does not equal loaded");
        assertTrue(loaded.equals(constructed), "Loaded annotation does not equal constructed");
    }

    /* ====================================================================== */
    /* RETENTION TESTS                                                        */
    /* ====================================================================== */

    @Test(expectedExceptions=IllegalArgumentException.class,
          expectedExceptionsMessageRegExp="^Annotation org.usrz.libs.utils.AnnotationsTest\\$AnnotationWithoutRetention is not annotated with @Retention$")
    public void testAnnotationWithoutRetention() {
        Annotations.newInstance(AnnotationWithoutRetention.class);
    }

    @Test(expectedExceptions=IllegalArgumentException.class,
          expectedExceptionsMessageRegExp="^Annotation org.usrz.libs.utils.AnnotationsTest\\$AnnotationWithClassRetention specifies the wrong \"CLASS\" @Retention policy$")
    public void testAnnotationWithClassRetention() {
        Annotations.newInstance(AnnotationWithClassRetention.class);
    }

    @Test(expectedExceptions=IllegalArgumentException.class,
          expectedExceptionsMessageRegExp="^Annotation org.usrz.libs.utils.AnnotationsTest\\$AnnotationWithSourceRetention specifies the wrong \"SOURCE\" @Retention policy$")
    public void testAnnotationWithSourceRetention() {
        Annotations.newInstance(AnnotationWithSourceRetention.class);
    }

    @Test
    public void testAnnotationWithRuntimeRetention() {
        assertNotNull(Annotations.newInstance(AnnotationWithRuntimeRetention.class));
    }

    public static @interface AnnotationWithoutRetention {}
    @Retention(RetentionPolicy.CLASS)
    public static @interface AnnotationWithClassRetention {}
    @Retention(RetentionPolicy.SOURCE)
    public static @interface AnnotationWithSourceRetention {}
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface AnnotationWithRuntimeRetention {}

    /* ====================================================================== */
    /* ANNOTATION TESTS                                                       */
    /* ====================================================================== */

    @Test
    public void testEmpty() {
        final Empty a1 = Annotated.class.getAnnotation(Empty.class);
        final Empty a2 = Annotations.newInstance(Empty.class);
        assertAnnotation(a1, a2);
    }

    @Retention(RUNTIME)
    public static @interface Empty { }

    /* ====================================================================== */

    @Test
    public void testWithDefaults() {
        final WithDefaults a1 = Annotated.class.getAnnotation(WithDefaults.class);
        final WithDefaults a2 = Annotations.newInstance(WithDefaults.class);

        /* Check our components before checking the rest of the annotation */
        assertEquals(a2.booleanProperty(),     a1.booleanProperty(),         "booleanProperty mismatch");
        assertEquals(a2.stringProperty(),      a1.stringProperty(),           "stringProperty mismatch");
        assertEquals(a2.classProperty(),       a1.classProperty(),             "classProperty mismatch");
        assertEquals(a2.intArrayProperty(),    a1.intArrayProperty(),       "intArrayProperty mismatch");
        assertEquals(a2.stringArrayProperty(), a1.stringArrayProperty(), "stringArrayProperty mismatch");

        /* Check the rest of the annotation */
        assertAnnotation(a1, a2);
    }

    @Test
    public void testWithDefaultsOverridden() {
        final WithDefaults a1 = AnnotatedOverrides.class.getAnnotation(WithDefaults.class);
        final WithDefaults a2 = Annotations.newInstance(WithDefaults.class,
                new MapBuilder<String, Object>()
                    .put(    "booleanProperty", false)
                    .put(     "stringProperty", "houdini the wizard")
                    .put(      "classProperty", String.class)
                    .put(   "intArrayProperty", new int[] { 789, 987 })
                    .put("stringArrayProperty", new String[] { "houdini", "the", "wizard"})
                    .map());

        /* Check our components before checking the rest of the annotation */
        assertEquals(a2.booleanProperty(),     a1.booleanProperty(),         "booleanProperty mismatch");
        assertEquals(a2.stringProperty(),      a1.stringProperty(),           "stringProperty mismatch");
        assertEquals(a2.classProperty(),       a1.classProperty(),             "classProperty mismatch");
        assertEquals(a2.intArrayProperty(),    a1.intArrayProperty(),       "intArrayProperty mismatch");
        assertEquals(a2.stringArrayProperty(), a1.stringArrayProperty(), "stringArrayProperty mismatch");

        /* Check the rest of the annotation */
        assertAnnotation(a1, a2);
    }

    @Retention(RUNTIME)
    public static @interface WithDefaults {
        boolean  booleanProperty()     default true;
        String   stringProperty()      default "bozo the clown";
        Class<?> classProperty()       default Array.class;
        int[]    intArrayProperty()    default { 123, 321 };
        String[] stringArrayProperty() default { "bozo", "the", "clown" };
    }

    /* ====================================================================== */

    @Empty
    @WithDefaults
    public static class Annotated { }

    @WithDefaults(booleanProperty     = false,
                  stringProperty      = "houdini the wizard",
                  classProperty       = String.class,
                  intArrayProperty    = { 789, 987 },
                  stringArrayProperty = { "houdini", "the", "wizard"})
    public static class AnnotatedOverrides { }

}
