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
package org.usrz.libs.utils.files;

import static java.lang.Integer.toHexString;
import static org.usrz.libs.utils.Check.notNull;

import java.io.File;
import java.util.EventObject;

public final class FileEvent extends EventObject {

    public enum Type { CHANGED, CREATED, DELETED };

    private final File file;
    private final Type type;

    FileEvent(FileWatcher source, File file, Type type) {
        super(source);
        this.file = notNull(file, "Null file");
        this.type = notNull(type, "Null type");
    }

    @Override
    public FileWatcher getSource() {
        return (FileWatcher) source;
    }

    public File getFile() {
        return file;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + '[' + type + ':' + file + ']' + '@' + toHexString(hashCode());
    }
}
