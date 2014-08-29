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

import static org.usrz.libs.configurations.Configurations.EMPTY_CONFIGURATIONS;

import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Test;
import org.usrz.libs.configurations.Configurations;
import org.usrz.libs.configurations.ConfigurationsBuilder;
import org.usrz.libs.testing.AbstractTest;

public class KeyedExecutorTest extends AbstractTest {

    @Test
    public void testKeyedExecutor() throws Exception {
        final Configurations configurations = new ConfigurationsBuilder()
                        .put("core_pool_size", 5)
                        .put("maximum_pool_size", 10)
                        .put("notifier_threads", 2)
                        .put("keep_alive_time", "10 sec")
                        .put("queue_size", 1000)
                        .build();

        final KeyedExecutor<String> executor = new KeyedExecutor<>(SimpleExecutorProvider.create(EMPTY_CONFIGURATIONS));
        final AtomicInteger performed = new AtomicInteger(0);
        final AtomicInteger notified = new AtomicInteger(0);

        for (int x = 0; x < 10000; x ++) {
            executor.call("FOO", () -> {
                Thread.sleep(1000);
                return performed.incrementAndGet();
            }).withConsumer((f) -> notified.incrementAndGet());
        }

        for (int x = 0; x < 50; x ++) {
            Thread.sleep(100);
            System.err.printf("%02d/50: performed %d, notified %d\n", x, performed.get(), notified.get());
            if (performed.get() < 0) continue;
            if (notified.get() < 10000) continue;
            break;
        }

        assertEquals(performed.get(), 1, "Wrong number of performed iterations");
        assertEquals(notified.get(), 10000, "Wrong number of notifications");
    }

}
