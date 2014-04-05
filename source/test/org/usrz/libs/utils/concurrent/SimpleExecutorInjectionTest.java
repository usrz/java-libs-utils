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
package org.usrz.libs.utils.concurrent;

import java.util.concurrent.ExecutionException;

import org.testng.annotations.Test;
import org.usrz.libs.configurations.ConfigurationsBuilder;
import org.usrz.libs.inject.Injector;
import org.usrz.libs.testing.AbstractTest;

public class SimpleExecutorInjectionTest extends AbstractTest {

    @Test
    public void testSimpleExecutorInjection()
    throws InterruptedException, ExecutionException {
        final SimpleExecutor executor = Injector.create().getInstance(SimpleExecutor.class);
        final String threadName = executor.call(() -> Thread.currentThread().getName()).get();
        assertTrue(threadName.matches("^SimpleExecutor@[\\dabcdef]+-[\\d]+$"), "Wrong thread name \"" + threadName);
    }

    @Test
    public void testSimpleExecutorInjectionWithName()
    throws InterruptedException, ExecutionException {
        final SimpleExecutor executor = Injector.create((binder) -> {
            binder.configure(new ConfigurationsBuilder()
                                 .put(SimpleExecutorProvider.EXECUTOR_NAME, "FooBarBaz")
                                 .build());
            binder.bind(SimpleExecutor.class).toProvider(SimpleExecutorProvider.class);
        }).getInstance(SimpleExecutor.class);

        final String threadName = executor.call(() -> Thread.currentThread().getName()).get();
        assertTrue(threadName.matches("^FooBarBaz-[\\d]+$"), "Wrong thread name \"" + threadName);
    }

}
