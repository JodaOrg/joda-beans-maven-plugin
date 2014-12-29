/*
 *  Copyright 2013 Stephen Colebourne
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

import static org.testng.Assert.assertEquals;

import java.util.regex.Matcher;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test.
 */
@Test
public class TestRegexMessageParsing {

    @DataProvider(name = "match")
    Object[][] data_match() {
        return new Object[][] {
            {"Error in bean: E:\\dev\\test\\Test.java, Line: 123, Message: Bad thing happened",
                "E:\\dev\\test\\Test.java", 123, "Bad thing happened"},
            {"Error in bean: E:\\dev\\test\\T,est.java, Line: 1, Message: Invalid bean builder scope: rubbish",
                    "E:\\dev\\test\\T,est.java", 1, "Invalid bean builder scope: rubbish"},
        };
    }

    @Test(dataProvider = "match")
    public void test_parse(String input, String file, int line, String msg) {
        Matcher matcher = AbstractJodaBeansMojo.MESSAGE_PATTERN.matcher(input);
        assertEquals(matcher.matches(), true);
    }

}
