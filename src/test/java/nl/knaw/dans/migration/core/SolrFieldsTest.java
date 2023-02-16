/*
 * Copyright (C) 2021 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.migration.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SolrFieldsTest {
    @Test
    public void canParseFreeDate() throws IOException {
        String result = new SolrFields("2012-06-29,\"OPEN_ACCESS,accept\",onderzoeksdataNIOD,PUBLISHED,,\"1983-1984\"").date;
        assertEquals("1983-1984", result);
    }

    @Test
    public void canParseFormattedDate() throws IOException {
        String result = new SolrFields("2012-06-29,\"OPEN_ACCESS,accept\",onderzoeksdataNIOD,PUBLISHED,\"1983-01-01T22:00:00Z\",\"1984\"").date;
        assertEquals("1983", result);
    }

    @Test
    public void canParseNoDateAtAll() throws IOException {
        String result = new SolrFields("2012-06-29,\"OPEN_ACCESS,accept\",onderzoeksdataNIOD,PUBLISHED,,").date;
        assertEquals("", result);
    }
}
