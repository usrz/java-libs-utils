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

import static org.usrz.libs.utils.Check.notNull;

import java.io.File;
import java.util.EventListener;
import java.util.function.Consumer;

import org.usrz.libs.utils.Check;
import org.usrz.libs.utils.files.FileEvent.Type;

@FunctionalInterface
public interface FileListener extends EventListener, Consumer<FileEvent> {

    public static FileListener create(Type type, Consumer<File> consumer) {
        Check.notNull(consumer, "Null consumer");
        switch (notNull(type, "Null type")) {
            case CHANGED: return new AbstractFileListener() { @Override protected void changed(File file) { consumer.accept(file); } };
            case CREATED: return new AbstractFileListener() { @Override protected void created(File file) { consumer.accept(file); } };
            case DELETED: return new AbstractFileListener() { @Override protected void deleted(File file) { consumer.accept(file); } };
        }
        throw new IllegalArgumentException("Unkown type " + type);
    }

}
