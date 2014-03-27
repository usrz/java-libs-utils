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

import static org.usrz.libs.utils.Charsets.UTF8;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * An <i>idiotic</i> {@link Codec} simply using a {@link Charset} to encode and
 * decode byte arrays into and from {@link String}s.
 *
 * @author <a href="mailto:pier@usrz.com">Pier Fumagalli</a>
 */
public class CharsetCodec extends AbstractCodec {

    private final Charset charset;

    /**
     * Create a new {@link CharsetCodec} instance using the default
     * <i>UTF-8</i> character set.
     */
    public CharsetCodec() {
        this(UTF8);
    }

    /**
     * Create a new {@link CharsetCodec} instance using the specified character
     * set, or the default <i>UTF-8</i> if <b>null</b>.
     *
     * @throws UnsupportedCharsetException If the character set was not supported.
     */
    public CharsetCodec(String charset)
    throws UnsupportedCharsetException {
        this(charset == null ? UTF8 : Charset.forName(charset));
    }

    /**
     * Create a new {@link CharsetCodec} instance using the specified character
     * set, or the default <i>UTF-8</i> if <b>null</b>.
     */
    public CharsetCodec(Charset charset) {
        this.charset = charset == null ? UTF8 : charset;
    }

    /* ====================================================================== */

    @Override
    public String encode(byte[] data, int offset, int length) {
        return new String(data, offset, length, charset);
    }

    @Override
    public byte[] decode(String data)
    throws IllegalArgumentException {
        return data.getBytes(charset);
    }

}
