/*
 * Copyright (C) 2019 ByteDance Inc
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
package com.bytedance.scenerouter.apt;

import java.util.regex.Pattern;

public class Utility {
    /**
     * 要求
     * 1，不能带host
     * 2，不能带端口号
     * 3，不能带除了/ : 英文和数字之外的其他字符
     * app:///path/to/target
     * \w+://(\/\w{1,})*\/?
     * <p>
     * /path/to/target
     * (\/\w{1,})*\/?
     */
    private static final String URL_SCHEME_PATTERN = "\\w+://(\\/\\w{1,})*\\/?";
    private static final String URL_WITHOUT_SCHEME_PATTERN = "(\\/\\w{1,})*\\/?";

    private Utility() {

    }

    public static boolean isEmpty(String text) {
        if (text == null || text.trim().length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static void throwExceptionIfUrlIncorrect(String url) {
        url = url.toLowerCase().trim();
        if (!Pattern.matches(URL_SCHEME_PATTERN, url) && !Pattern.matches(URL_WITHOUT_SCHEME_PATTERN, url)) {
            throw new IllegalArgumentException("Url format invalidate, format should be schema:///path/to/target or /path/to/target, not support =? argument now, wrong url " + url);
        }
    }
}
