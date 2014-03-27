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
package org.usrz.libs.utils.codecs;

import static org.usrz.libs.utils.codecs.Base64Codec.Alphabet.MODULAR_CRYPT;
import static org.usrz.libs.utils.codecs.Base64Codec.Alphabet.STANDARD;
import static org.usrz.libs.utils.codecs.Base64Codec.Alphabet.URL_SAFE;

import java.util.Objects;

/**
 * A simple factory class instantiating {@link Codec}s by name.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public final class CodecManager {

    private CodecManager() {
        throw new IllegalStateException();
    }

    /**
     * Retrieve an instance of the {@link Codec} associated with the given
     * name (case insensitive).
     *
     * <p>Known codecs are:</p>
     *
     * <ul>
     *   <li>{@code HEX}</li>
     *   <li>{@code HEX/UPPER}</li>
     *   <li>{@code HEX/UPPERCASE}</li>
     *   <li>{@code HEX/UPPER_CASE}</li>
     *   <li>{@code HEX/LOWER}</li>
     *   <li>{@code HEX/LOWERCASE}</li>
     *   <li>{@code HEX/LOWER_CASE}</li>
     *   <li>{@code BASE32}</li>
     *   <li>{@code BASE32/UPPER}</li>
     *   <li>{@code BASE32/UPPERCASE}</li>
     *   <li>{@code BASE32/UPPER_CASE}</li>
     *   <li>{@code BASE32/LOWER}</li>
     *   <li>{@code BASE32/LOWERCASE}</li>
     *   <li>{@code BASE32/LOWER_CASE}</li>
     *   <li>{@code BASE64}</li>
     *   <li>{@code BASE64/STANDARD}</li>
     *   <li>{@code BASE64/MODULARCRYPT}</li>
     *   <li>{@code BASE64/MODULAR_CRYPT}</li>
     *   <li>{@code BASE64/URLSAFE}</li>
     *   <li>{@code BASE64/URL_SAFE}</li>
     * </ul>
     */
    public static final Codec getCodec(String codecSpec) {
        final String spec = Objects.requireNonNull(codecSpec, "Null codec").toUpperCase().trim();
        switch (spec) {
            case "HEX"                  : return new HexCodec();

            case "HEX/UPPER"            :
            case "HEX/UPPERCASE"        :
            case "HEX/UPPER_CASE"       : return new HexCodec(true);

            case "HEX/LOWER"            :
            case "HEX/LOWERCASE"        :
            case "HEX/LOWER_CASE"       : return new HexCodec(false);

            case "BASE32"               : return new Base32Codec();

            case "BASE32/UPPER"         :
            case "BASE32/UPPERCASE"     :
            case "BASE32/UPPER_CASE"    : return new Base32Codec(true);

            case "BASE32/LOWER"         :
            case "BASE32/LOWERCASE"     :
            case "BASE32/LOWER_CASE"    : return new Base32Codec(false);

            case "BASE64"               : return new Base64Codec();

            case "BASE64/STANDARD"      : return new Base64Codec(STANDARD);
            case "BASE64/MODULARCRYPT"  :
            case "BASE64/MODULAR_CRYPT" : return new Base64Codec(MODULAR_CRYPT);
            case "BASE64/URLSAFE"       :
            case "BASE64/URL_SAFE"      : return new Base64Codec(URL_SAFE);

            default: throw new IllegalArgumentException("Unknown codec spec \"" + spec + "\"");
        }
    }
}
