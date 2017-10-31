/*
 *  Copyright 2013-present, Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.beans.maven;

import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

/**
 * Test.
 */
@RunWith(DataProviderRunner.class)
public class TestRegexMessageParsing {

    @DataProvider
    public static Object[][] dataMatch() {
        return new Object[][] {
            {"Error in bean: E:\\dev\\test\\Test.java, Line: 123, Message: Bad thing happened",
                "E:\\dev\\test\\Test.java", 123, "Bad thing happened"},
            {"Error in bean: E:\\dev\\test\\T,est.java, Line: 1, Message: Invalid bean builder scope: rubbish",
                    "E:\\dev\\test\\T,est.java", 1, "Invalid bean builder scope: rubbish"},
        };
    }

    @Test
    @UseDataProvider("dataMatch")
    public void testParse(String input, String file, int line, String msg) {
        Matcher matcher = AbstractJodaBeansMojo.MESSAGE_PATTERN.matcher(input);
        assertTrue(matcher.matches());
    }

}
