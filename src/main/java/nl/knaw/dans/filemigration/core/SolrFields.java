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
package nl.knaw.dans.filemigration.core;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SolrFields {
    static String requestedFields = "emd_date_available_formatted,dc_rights,dc_creator";
    private static final CSVFormat solrFormat = CSVFormat.RFC4180.withDelimiter(',');
    final String available;
    final String rights;
    final String creator;
    SolrFields (String line) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8));
        CSVRecord record = CSVParser.parse(inputStream, StandardCharsets.UTF_8, solrFormat).getRecords().get(0);
        available = record.get(0).trim();
        rights = record.get(1)
                .replaceAll("^\"(.*)\"$","$1") // strip quotes
                .replaceAll(",.*",""); // strip licence URL and rights holder;
        creator = record.get(2).trim();
    }
}
