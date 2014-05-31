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

import java.io.File;

public class AbstractFileListener implements FileListener {

    protected AbstractFileListener() {
        /* Nothing to do here */
    }

    @Override
    public final void accept(FileEvent event) {
        final File file = event.getFile();
        switch (event.getType()) {
            case CHANGED: changed(file); break;
            case CREATED: created(file);  break;
            case DELETED: deleted(file);  break;
        }
    }

    protected void created(File file) {}

    protected void deleted(File file) {}

    protected void changed(File file) {}

}
