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

import org.usrz.libs.utils.codecs.Base64Codec.Alphabet;

/**
 * A simple factory class instantiating {@link Codec}s by name.
 *
 * <p>Known codecs are:</p>
 *
 * <ul>
 *   <li>{@code HEX}:</li>
 *   <li>{@code HEX/UPPER}:</li>
 *   <li>{@code HEX/UPPERCASE}:</li>
 *   <li>{@code HEX/UPPER_CASE}: {@link HexCodec HEX} encoding, upper case alphabet</li>
 * </ul><br><ul>
 *   <li>{@code HEX/LOWER}:</li>
 *   <li>{@code HEX/LOWERCASE}:</li>
 *   <li>{@code HEX/LOWER_CASE}: {@link HexCodec HEX} encoding, lower case alphabet</li>
 * </ul><br><ul>
 *   <li>{@code BASE32}:</li>
 *   <li>{@code BASE32/UPPER}:</li>
 *   <li>{@code BASE32/UPPERCASE}:</li>
 *   <li>{@code BASE32/UPPER_CASE}: {@link Base32Codec BASE32} encoding, upper case alphabet, no padding</li>
 * </ul><br><ul>
 *   <li>{@code BASE32/LOWER}</li>
 *   <li>{@code BASE32/LOWERCASE}:</li>
 *   <li>{@code BASE32/LOWER_CASE}: {@link Base32Codec BASE32} encoding, lower case alphabet, no padding</li>
 * </ul><br><ul>
 *   <li>{@code BASE64}:</li>
 *   <li>{@code BASE64/STANDARD}:</li>
 *   <li>{@code BASE64/STANDARD/PADDED}:</li>
 *   <li>{@code BASE64/STANDARD/PADDING}: {@link Base64Codec BASE64} encoding, {@link Alphabet#STANDARD STANDARD} alphabet, padded</li>
 * </ul><br><ul>
 *   <li>{@code BASE64/STANDARD/UNPADDED}:</li>
 *   <li>{@code BASE64/STANDARD/NO_PADDING}: {@link Base64Codec BASE64} encoding, {@link Alphabet#STANDARD STANDARD} alphabet, unpadded</li>
 * </ul><br><ul>
 *   <li>{@code BASE64/MODULARCRYPT}:</li>
 *   <li>{@code BASE64/MODULAR_CRYPT}:</li>
 *   <li>{@code BASE64/MODULARCRYPT/PADDED}:</li>
 *   <li>{@code BASE64/MODULAR_CRYPT/PADDED}:</li>
 *   <li>{@code BASE64/MODULARCRYPT/PADDING}:</li>
 *   <li>{@code BASE64/MODULAR_CRYPT/PADDING}: {@link Base64Codec BASE64} encoding, {@link Alphabet#MODULAR_CRYPT MODULAR_CRYPT} alphabet, padded</li>
 * </ul><br><ul>
 *   <li>{@code BASE64/MODULARCRYPT/UNPADDED}:</li>
 *   <li>{@code BASE64/MODULAR_CRYPT/UNPADDED}:</li>
 *   <li>{@code BASE64/MODULARCRYPT/NO_PADDING}:</li>
 *   <li>{@code BASE64/MODULAR_CRYPT/NO_PADDING}: {@link Base64Codec BASE64} encoding, {@link Alphabet#MODULAR_CRYPT MODULAR_CRYPT} alphabet, unpadded</li>
 * </ul><br><ul>
 *   <li>{@code BASE64/URLSAFE}:</li>
 *   <li>{@code BASE64/URL_SAFE}:</li>
 *   <li>{@code BASE64/URLSAFE/PADDED}:</li>
 *   <li>{@code BASE64/URL_SAFE/PADDED}:</li>
 *   <li>{@code BASE64/URLSAFE/PADDING}:</li>
 *   <li>{@code BASE64/URL_SAFE/PADDING}: {@link Base64Codec BASE64} encoding, {@link Alphabet#URL_SAFE URL_SAFE} alphabet, padded</li>
 * </ul><br><ul>
 *   <li>{@code BASE64/URLSAFE/UNPADDED}:</li>
 *   <li>{@code BASE64/URL_SAFE/UNPADDED}:</li>
 *   <li>{@code BASE64/URLSAFE/NO_PADDING}:</li>
 *   <li>{@code BASE64/URL_SAFE/NO_PADDING}: {@link Base64Codec BASE64} encoding, {@link Alphabet#URL_SAFE URL_SAFE} alphabet, unpadded</li>
 * </ul>
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
     */
    public static final ManagedCodec getCodec(String codecSpec) {
        final String spec = Objects.requireNonNull(codecSpec, "Null codec").toUpperCase().trim();
        switch (spec) {
            case "HEX"                  :
            case "HEX/UPPER"            :
            case "HEX/UPPERCASE"        :
            case "HEX/UPPER_CASE"       : return new HexCodec(true);

            case "HEX/LOWER"            :
            case "HEX/LOWERCASE"        :
            case "HEX/LOWER_CASE"       : return new HexCodec(false);

            case "BASE32"               :
            case "BASE32/UPPER"         :
            case "BASE32/UPPERCASE"     :
            case "BASE32/UPPER_CASE"    : return new Base32Codec(true);

            case "BASE32/LOWER"         :
            case "BASE32/LOWERCASE"     :
            case "BASE32/LOWER_CASE"    : return new Base32Codec(false);

            case "BASE64"                     :
            case "BASE64/STANDARD"            :
            case "BASE64/STANDARD/PADDED"     :
            case "BASE64/STANDARD/PADDING"    : return new Base64Codec(STANDARD, true);

            case "BASE64/STANDARD/UNPADDED"   :
            case "BASE64/STANDARD/NO_PADDING" : return new Base64Codec(STANDARD, false);

            case "BASE64/MODULARCRYPT"          :
            case "BASE64/MODULAR_CRYPT"         :
            case "BASE64/MODULARCRYPT/PADDED"   :
            case "BASE64/MODULAR_CRYPT/PADDED"  :
            case "BASE64/MODULARCRYPT/PADDING"  :
            case "BASE64/MODULAR_CRYPT/PADDING" : return new Base64Codec(MODULAR_CRYPT, true);

            case "BASE64/MODULARCRYPT/UNPADDED"    :
            case "BASE64/MODULAR_CRYPT/UNPADDED"   :
            case "BASE64/MODULARCRYPT/NO_PADDING"  :
            case "BASE64/MODULAR_CRYPT/NO_PADDING" : return new Base64Codec(MODULAR_CRYPT, false);

            case "BASE64/URLSAFE"          :
            case "BASE64/URL_SAFE"         :
            case "BASE64/URLSAFE/PADDED"   :
            case "BASE64/URL_SAFE/PADDED"  :
            case "BASE64/URLSAFE/PADDING"  :
            case "BASE64/URL_SAFE/PADDING" : return new Base64Codec(URL_SAFE, true);

            case "BASE64/URLSAFE/UNPADDED"    :
            case "BASE64/URL_SAFE/UNPADDED"   :
            case "BASE64/URLSAFE/NO_PADDING"  :
            case "BASE64/URL_SAFE/NO_PADDING" : return new Base64Codec(URL_SAFE, false);

            default: throw new IllegalArgumentException("Unknown codec spec \"" + spec + "\"");
        }
    }
}
