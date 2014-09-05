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
package org.usrz.libs.utils;

import java.time.Duration;

import org.testng.annotations.Test;
import org.usrz.libs.testing.AbstractTest;

public class TimesTest extends AbstractTest {

    @Test
    public void testDuration() {
        assertEquals(Times.duration("2 days 3 hours 3 minutes 5.22 seconds"), Duration.parse("PT51H3M5.22S"));
        assertEquals(Times.duration("2 day  3 hour  3 minute  5.22 second "), Duration.parse("PT51H3M5.22S"));
        assertEquals(Times.duration("2 d    3 hrs   3 mins    5.22 secs   "), Duration.parse("PT51H3M5.22S"));
        assertEquals(Times.duration("2 d    3 hr    3 min     5.22 sec    "), Duration.parse("PT51H3M5.22S"));
        assertEquals(Times.duration("2 d    3 h     3 m       5.22 s      "), Duration.parse("PT51H3M5.22S"));
        assertEquals(Times.duration("2d3h3m5.22s"),                           Duration.parse("PT51H3M5.22S"));
        assertEquals(Times.duration("P2DT3H3M5.22S"),                         Duration.parse("PT51H3M5.22S"));
        assertEquals(Times.duration("PT51H3M5.22S"),                          Duration.parse("PT51H3M5.22S"));

        assertEquals(Times.duration("1 day 1 hour 1 minute 1 second"),        Duration.parse("PT25H1M1S"));
        assertEquals(Times.duration("1 d   1 hr   1 min    1 sec   "),        Duration.parse("PT25H1M1S"));
        assertEquals(Times.duration("1 d   1 h    1 m      1 s     "),        Duration.parse("PT25H1M1S"));
        assertEquals(Times.duration("1d1h1m1s"),                              Duration.parse("PT25H1M1S"));
        assertEquals(Times.duration("P1DT1H1M1S"),                            Duration.parse("PT25H1M1S"));
        assertEquals(Times.duration("PT25H1M1S"),                             Duration.parse("PT25H1M1S"));

        assertEquals(Times.duration("1 hour 5 minutes 3.1 seconds"),          Duration.parse("PT1H5M3.1S"));
        assertEquals(Times.duration("2 hrs 35 secs"),                         Duration.parse("PT2H35S"));
        assertEquals(Times.duration("7 min 12s"),                             Duration.parse("PT7M12S"));
        assertEquals(Times.duration("3 hours"),                               Duration.parse("PT3H"));
        assertEquals(Times.duration("4m"),                                    Duration.parse("PT4M"));
        assertEquals(Times.duration("59s"),                                   Duration.parse("PT59S"));
        assertEquals(Times.duration("1d"),                                    Duration.parse("PT24H"));
    }

    @Test
    public void testFormat() {
        assertEquals(Times.format(Duration.parse("PT51H3M5.22S")), "2 days 3 hours 3 minutes 5.22 seconds");
        assertEquals(Times.format(Duration.parse("PT25H1M1S")), "1 day 1 hour 1 minute 1 second");
        assertEquals(Times.format(Duration.parse("PT1H5M3.1S")), "1 hour 5 minutes 3.1 seconds");
        assertEquals(Times.format(Duration.parse("PT2H35S")), "2 hours 35 seconds");
        assertEquals(Times.format(Duration.parse("PT7M12S")), "7 minutes 12 seconds");
        assertEquals(Times.format(Duration.parse("PT3H")), "3 hours");
        assertEquals(Times.format(Duration.parse("PT4M")), "4 minutes");
        assertEquals(Times.format(Duration.parse("PT59S")), "59 seconds");
        assertEquals(Times.format(Duration.parse("PT24H")), "1 day");
        assertEquals(Times.format(Duration.parse("PT0.1S")), "0.1 seconds");
        assertEquals(Times.format(Duration.parse("PT1S")), "1 second");
        assertEquals(Times.format(Duration.parse("PT0S")), "0 seconds");
    }
}
