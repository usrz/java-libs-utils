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

import static java.lang.Runtime.getRuntime;
import static org.usrz.libs.utils.Check.notNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.usrz.libs.logging.Log;

public class Reloader {

    private final Log log = new Log();

    private final AtomicReference<Thread> thread = new AtomicReference<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final FileWatcher watcher;
    private final ClassLoader parent;
    private final String[] arguments;
    private final Class<?> clazz;
    private final URL[] urls;

    public Reloader(Class<?> clazz, String[] arguments)
    throws IOException {
        this.clazz = notNull(clazz, "Null class");
        this.arguments = notNull(arguments, "Null arguments");

        final ClassLoader classLoader = clazz.getClassLoader();
        if (!(classLoader instanceof URLClassLoader)) {
            throw new IllegalStateException("Class " + clazz.getName() + " not loaded by a URLClassLoader");
        }

        urls = ((URLClassLoader) classLoader).getURLs();
        parent = classLoader.getParent();

        try {
            clazz.getDeclaredMethod("main", new Class<?>[] { arguments.getClass() });
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("Class " + clazz + " does not specify a \"main(String[])\" method", exception);
        }

        watcher = new FileWatcher((event) -> {
            final Thread interruptable = thread.get();
            log.info("Notified of change in \"%s\"", event.getFile());
            if (interruptable != null) interruptable.interrupt();
        });

        for (URL url: urls) {
            if (! "file".equals(url.getProtocol())) continue;
            watcher.add(new File(url.getPath()));
        }

    }

    public void execute() {
        final URLClassLoader reloadingClassLoader = new URLClassLoader(urls, parent);
        try {
            final Class<?> reloadingClass = reloadingClassLoader.loadClass(clazz.getName());
            final Method reloadingMain = reloadingClass.getMethod("main", new Class<?>[] { arguments.getClass() });

            log.debug("Invoking \"main(...)\" method of class " + reloadingClass.getName());

            Thread.currentThread().setContextClassLoader(reloadingClassLoader);
            reloadingMain.invoke(reloadingClass, new Object[] { arguments });
            log.info("Class %s exited normally", reloadingClass.getName());

        } catch (InvocationTargetException exception) {
            final Throwable cause = exception.getCause();
            if (cause instanceof InterruptedException) {
                log.info("InterruptedException detected");
            } else {
                log.warn(cause, "Class %s threw an exception", clazz.getName());
            }
        } catch (Exception exception) {
            log.warn(exception, "Class %s threw an exception", clazz.getName());
        } finally {
            try {
                reloadingClassLoader.close();
            } catch (IOException exception) {
                log.warn(exception, "I/O error closing class loader");
            }
        }
    }

    private void keepAlive() {
        for (;;) {
            if (running.get()) execute();
            if (! running.get()) return;
            Thread.interrupted();
            try {
                log.info("Waiting before reload");
                Thread.sleep(1000);
            } catch (InterruptedException exception) {
                log.warn("Interrupted waiting for reload");
            }
        }
    }

    public void start()
    throws IOException {
        final Thread executor = new Thread(() -> keepAlive());
        if (thread.compareAndSet(null, executor)) {
            running.set(true);
            executor.start();
        } else {
            throw new IllegalStateException("Already started");
        }
    }

    public void stop() {
        final Thread executor = thread.getAndSet(null);
        if (executor == null) throw new IllegalStateException("Never started");
        running.set(false);
        executor.interrupt();
        try {
            executor.join();
        } catch (InterruptedException exception) {
            log.warn("Interrupted awaiting executor termination");
        }
    }

    public static void main(String args[])
    throws Exception {

        if (args.length < 1) throw new IllegalArgumentException("Class to execute not specified");

        final String[] arguments = new String[args.length - 1];
        System.arraycopy(args, 1, arguments, 0, arguments.length);

        final Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(args[0]);

        final Reloader watcher = new Reloader(clazz, arguments);

        final Thread thread = Thread.currentThread();
        getRuntime().addShutdownHook(new Thread(() -> thread.interrupt()));

        watcher.start();
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException exception) {
            watcher.stop();
        }
    }
}
