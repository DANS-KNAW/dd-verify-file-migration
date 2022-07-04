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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.csv.CSVFormat.RFC4180;

public class Mapping {
    private static final CSVFormat format = RFC4180
            .withDelimiter(',')
            .withFirstRecordAsHeader()
            .withIgnoreSurroundingSpaces()
            .withRecordSeparator(System.lineSeparator());

    static public Map<String, String> load(File csvFile, boolean toLower, String... header) {
        if (!csvFile.exists() || 0 == csvFile.length()) {
            throw new IllegalStateException("No (content in) " + csvFile);
        }
        List<CSVRecord> records;
        try {
            records = CSVParser.parse(new FileInputStream(csvFile), UTF_8, format.withHeader(header)).getRecords();
        } catch (IOException e) {
            throw new IllegalStateException("Can't read " + csvFile, e);
        }
        HashMap<String, String> mapping = new HashMap<>();
        records.forEach(csvRecord -> {
            if (toLower) Arrays.stream(header).map(String::toLowerCase);
            mapping.put(
                    csvRecord.get(header[0]), csvRecord.get(header[1]));
            }
        );
        return mapping;
    }

}
