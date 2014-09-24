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
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static org.usrz.libs.utils.Check.notNull;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.usrz.libs.logging.Log;
import org.usrz.libs.utils.files.FileEvent.Type;

public class FileWatcher implements Closeable {

    private final Log log = new Log();

    private final Consumer<FileEvent> listener;

    private final Map<FileSystem, WatchService> services = new HashMap<>();
    private final List<Poller> pollers = new ArrayList<>();
    private final Set<File> files = new HashSet<>();

    public FileWatcher(Consumer<FileEvent> listener) {
        this.listener = notNull(listener, "Null listener");
    }

    @Override
    public void close() {

        /* Close all our watcher services */
        services.values().forEach((service) -> { try {
                service.close();
            } catch (IOException exception) {
                log.warn(exception, "I/O error closing watch service");
            }});

        /* Interrupt all our pollers and wait for them to terminate */
        for (Poller poller: pollers) poller.interrupt();
        for (Poller poller: pollers) try {
            poller.join();
        } catch (InterruptedException exception) {
            log.warn("Interrupted waiting for pollers to exit");
            return;
        }
    }

    @SuppressWarnings("resource")
    public FileWatcher add(File file)
    throws IOException {
        final File canonicalFile = file.getCanonicalFile();
        final FileSystem fileSystem = canonicalFile.toPath().getFileSystem();

        WatchService service = services.get(fileSystem);
        if (service == null) {
            service = fileSystem.newWatchService();
            final Poller poller = new Poller(service);
            poller.setDaemon(true);
            poller.start();
            services.put(fileSystem, service);
            pollers.add(poller);
        }

        this.add(service, canonicalFile);
        files.add(canonicalFile);
        log.info("Watching changes for %s %s", (canonicalFile.isDirectory() ? "directory" : "file"), canonicalFile);
        return this;
    }

    private void add(WatchService service, File file)
    throws IOException {
        final Path path;

        if (file.isFile()) {
            path = file.getParentFile().toPath();

        } else if (file.isDirectory()) {
            path = file.toPath();
            for (File child: file.listFiles()) {
                if (child.isDirectory()) add(service, child);
            }

        } else {
            throw new FileNotFoundException("File " + file + " is not a file or directory");
        }

        path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW);
        log.debug("Listening to notifications from directory %s", path);

    }

    /* ====================================================================== */

    private class Poller extends Thread {

        private final WatchService service;

        private Poller(WatchService service) {
            this.service = notNull(service, "Null watch service");
            setName(FileWatcher.this.getClass().getSimpleName()
                        + '$' + this.getClass().getSimpleName()
                        + '@' + toHexString(hashCode()));
        }

        @Override
        public void run() {
            log.debug("Starting notification listener %s", this);
            for(;;) try {
                final WatchKey key = service.take();
                for (WatchEvent<?> event: key.pollEvents()) {
                    final Kind<?> kind = event.kind();
                    if (OVERFLOW.equals(kind)) {
                        log.warn("Overflow event received polling");
                        continue;
                    }

                    final File file = ((Path) key.watchable()).resolve((Path) event.context()).toFile();
                    log.debug("Received notification for %s", file);

                    File current = file;
                    while (current != null) {
                        if (files.contains(current)) {
                            final Type type = ENTRY_MODIFY.equals(kind) ? Type.CHANGED :
                                              ENTRY_CREATE.equals(kind) ? Type.CREATED :
                                              ENTRY_DELETE.equals(kind) ? Type.DELETED :
                                              null;
                            if (type == null) {
                                log.warn("Unknown event kind %s for file %s", kind, file);
                                break;
                            }

                            try {
                                listener.accept(new FileEvent(FileWatcher.this, file, type));
                            } catch (Exception exception) {
                                log.warn(exception, "Exception notifying listener");
                            }

                            if (ENTRY_CREATE.equals(kind) && file.isDirectory()) try {
                                FileWatcher.this.add(service, file);
                            } catch (Exception exception) {
                                log.warn(exception, "Exception adding extra notification path %s", file);
                            }

                            break;
                        } else {
                            current = current.getParentFile();
                        }
                    }
                }

                key.reset();

            } catch (InterruptedException exception) {
                log.debug("Notification listener %s interrupted, exiting...", this);
                try {
                    service.close();
                } catch (IOException ioException) {
                    log.warn("I/O error closing watch service", exception);
                }
                return;
            }
        }

    }

}
