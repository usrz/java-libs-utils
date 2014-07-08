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

import static org.usrz.libs.utils.Check.notNull;

/**
 * Simplify parsing and representation of ordering in sorting functions.
 * <p>
 * This class will parse strings formatted as {@code +field}, or {@code -field},
 * returning the corresponding specified <em>field</em> and <em>ascending</em>
 * (or <em>descending</em>) flag value.
 * <p>
 * If neither the {@code +} <em>(plus)</em> nor {@code -} <em>(minus)</em>
 * characters were specified, this implementation assumes <em>ascending</em>
 * ordering by default.
 * <p>
 * When validating <em>enums</em> field names are matched case sensitive
 * <b>or</em> in their all-uppercase variant.
 * <p>
 * Parsing a {@code null} value or the <em>empty string</em> will return a
 * {@code null} {@link Sort} instance.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 * @param <F>
 */
public final class Sort<F> {

    private final F field;
    private final boolean ascending;

    private Sort(F field, boolean ascending) {
        this.field = notNull(field, "Invalid field");
        this.ascending = ascending;
    }

    /**
     * Return the <em>field</em> on which to sort on.
     */
    public F field() {
        return field;
    }

    /**
     * Whether <em>ascending</em> or <em>descending</em> order is assumed.
     */
    public boolean ascending() {
        return ascending;
    }

    /* ====================================================================== */

    @Override
    public int hashCode() {
        final int hash = 127 * field.hashCode();
        return ascending ? hash : -hash;
    }

    @Override
    public String toString() {
        return Sort.class.getName() + '[' + field.toString() + ',' + (ascending ? "ascending" : "descending") + ']';
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (object == this) return true;
        try {
            final Sort<?> sort = (Sort<?>) object;
            return sort.field.equals(field) && (sort.ascending == ascending);
        } catch (ClassCastException exception) {
            return false;
        }
    }

    /* ====================================================================== */

    /**
     * Create a {@link Sort} parsing the specified {@link String}.
     */
    public static Sort<String> by(String sortSpec) {
        if (sortSpec == null) return null;
        if ((sortSpec = sortSpec.trim()).isEmpty()) return null;

        final boolean ascending;
        final char firstChar = sortSpec.charAt(0);
        if (firstChar == '+') {
            sortSpec = sortSpec.substring(1).trim();
            ascending = true;
        } else if (firstChar == '-' ) {
            sortSpec = sortSpec.substring(1).trim();
            ascending = false;
        } else {
            ascending = true;
        }

        if (sortSpec.isEmpty()) throw new IllegalArgumentException("No field after ascending/descending marker");
        return new Sort<String>(sortSpec, ascending);
    }

    /**
     * Create a {@link Sort} parsing the specified {@link String}, and
     * validating the field name against the specified {@link Enum} class.
     *
     * @throws IllegalArgumentException If the field name was not found.
     */
    public static <F extends Enum<F>> Sort<F> by(String sortSpec, Class<F> values) {
        final Sort<String> sort = by(sortSpec);
        if (sort == null) return null;
        try {
            return new Sort<F>(Enum.valueOf(values, sort.field()), sort.ascending());
        } catch (IllegalArgumentException exception) {
            return new Sort<F>(Enum.valueOf(values, sort.field().toUpperCase()), sort.ascending());
        }
    }

}
