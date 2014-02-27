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

public abstract class AnnotatedBean {

    public String annotatedField;
    public Void voidField;

    @NotIntrospected
    private String nonStandardValue;
    @NotIntrospected
    private String standardValue;

    @SimpleAnnotation
    public void nonStandardValue(String something) {
        nonStandardValue = something;
    }

    @SimpleAnnotation
    public String nonStandardValue() {
        return nonStandardValue;
    }

    @SimpleAnnotation
    public void setStandardValue(String something) {
        standardValue = something;
    }

    @SimpleAnnotation
    public String getStandardValue() {
        return standardValue;
    }

    @ComplexAnnotation(number=4321)
    public void setAnnotatedField(float value) {
        annotatedField = "the value is " + value;
    }

    /* Those should simply NOT make the introspector fail */
    public void invalidSetter(String foo) {}
    public String invalidGetter() { return null; }

}
