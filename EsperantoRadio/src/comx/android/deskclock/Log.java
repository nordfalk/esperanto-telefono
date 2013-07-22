/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * package-level logging flag
 */

package comx.android.deskclock;

import java.text.SimpleDateFormat;
import java.util.Date;


final class Log {
    public final static String LOGTAG = dk.dr.radio.util.Log.TAG;

    static final boolean LOGV = false;

    static void v(String logMe) {
        dk.dr.radio.util.Log.d(logMe);
    }

    static void i(String logMe) {
        dk.dr.radio.util.Log.d(logMe);
    }

    static void e(String logMe) {
        dk.dr.radio.util.Log.d(logMe);
    }

    static void e(String logMe, Exception ex) {
        dk.dr.radio.util.Log.e(logMe, ex);
        dk.dr.radio.util.Log.kritiskFejlStille(ex);
    }

    static void wtf(String logMe) {
        e(logMe, new IllegalStateException(logMe));
    }


    static String formatTime(long millis) {
        return new SimpleDateFormat("HH:mm:ss.SSS aaa").format(new Date(millis));
    }
}
