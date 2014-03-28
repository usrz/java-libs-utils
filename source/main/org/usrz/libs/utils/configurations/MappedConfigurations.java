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
package org.usrz.libs.utils.configurations;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.usrz.libs.logging.Log;

/**
 * The {@link MappedConfigurations} class represents a {@link Configurations}
 * instance backed by a {@link HashMap}.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class MappedConfigurations extends Configurations {

    /* Pattern for validating configuration keys */
    private static final Pattern NAME_PATTERN = Pattern.compile("^([\\w-]+(\\.[\\w-]+)*)?$");
    /* Our logger */
    private static final Log log = new Log();

    /* The Map of our configurations (immutable) */
    private final Map<String, String> configurations;

    /* ====================================================================== */
    /* CONSTRUCTION                                                           */
    /* ====================================================================== */

    /**
     * Create a new {@link MappedConfigurations} from a {@link Map}.
     */
    public MappedConfigurations(Map<?, ?> map)
    throws ConfigurationsException {
        if (map == null) throw new NullPointerException("Null map");

        /* Do we *really* have to check names? */
        boolean checkNames = (! (map instanceof Configurations));

        /* Prepare our map where key/values will be copied into */
        final Map<String, String> configurations = new HashMap<>();

        /* Iterate through the given map */
        for (Entry<?, ?> entry: map.entrySet()) {
            /* Validate or normalize the key */
            final String key;
            try {
                final Object object = entry.getKey();
                key = checkNames ? validateKey(object) : object.toString();
            } catch (NullPointerException exception) {
                throw new NullPointerException("Null key");
            }
            final Object object = entry.getValue();

            /* Null or empty values? */
            if (object == null) {
                log.debug("Null value in map for key \"%s\", ignoring...", key);
                continue;
            }
            final String value = object.toString().trim();
            if (value.length() == 0) {
                log.debug("Empty value in map for key \"%s\", ignoring...", key);
                continue;
            }

            /* Remember this mapping */
            configurations.put(key, value);
        }

        /* All done! */
        this.configurations = Collections.unmodifiableMap(configurations);
    }

    /* ====================================================================== */
    /* IMPLEMENTATION                                                         */
    /* ====================================================================== */

    /**
     * Return the value of associated with the given <em>key</em> as a
     * {@link String} or the specified <em>default value</em> if no mapping
     * was found.
     */
    @Override
    public final String getString(Object key, String defaultValue) {
        final String string;
        if (key == null) {
            string = "";
        } else {
            string = ((String) key).trim();
        }

        final String value = configurations.get(string);
        return value == null ? defaultValue : value;
    }

    /**
     * Returns a {@link Set} view of the keys contained in this instance.
     */
    @Override
    public final Set<String> keySet() {
        return Collections.unmodifiableSet(configurations.keySet());
    }

    /**
     * Returns a {@link Collection} view of the values contained in this
     * instance.
     */
    @Override
    public final Collection<String> values() {
        return Collections.unmodifiableCollection(configurations.keySet());
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this instance.
     */
    @Override
    public final Set<Entry<String, String>> entrySet() {
        return Collections.unmodifiableSet(configurations.entrySet());
    }

    /**
     * Returns the number of key-value mappings in this instance.
     */
    @Override
    public final int size() {
        return configurations.size();
    }

    /* ====================================================================== */
    /* VALIDATE KEY NAMES                                                     */
    /* ====================================================================== */

    private static final String validateKey(Object key)
    throws ConfigurationsException {
        if (key == null) return "";
        final String name = key.toString().trim();
        if (NAME_PATTERN.matcher(name).matches()) return name;
        throw new ConfigurationsException("Invalid key name \"" + key + "\"");
    }

}
