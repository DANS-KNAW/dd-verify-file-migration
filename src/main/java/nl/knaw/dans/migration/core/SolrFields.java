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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public class SolrFields {

    private static final Logger log = LoggerFactory.getLogger(EasyFileLoader.class);

    public static String requestedFields = "emd_date_available_formatted,dc_rights,amd_depositor_id,ds_state,emd_date_created_formatted,emd_date_created";
    private static final CSVFormat solrFormat = CSVFormat.RFC4180.withDelimiter(',');
    final String available;
    final String creator;
    final AccessCategory accessCategory;
    final String state;
    final String date;

    SolrFields (String line) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8));
        CSVRecord record = CSVParser.parse(inputStream, StandardCharsets.UTF_8, solrFormat).getRecords().get(0);
        available = record.get(0).trim();
        creator = record.get(2).trim();
        state = record.get(3).trim();
        String trimmed = record.get(4).trim();
        String dateFormatted = trimmed.length() >=4 ? trimmed.substring(0,4) : record.get(5).trim() ;
        date = dateFormatted;
        String[] dcRights = record.get(1).trim()
                .replaceAll("^\"", "") // strip leading quote
                .replaceAll("\"$", "") // strip trailing quote
                .split(", *");
        Optional<AccessCategory> maybeRights= Arrays.stream(dcRights)
                .filter(this::isDatasetRights)
                .map(AccessCategory::valueOf)
                .findFirst();
        if (maybeRights.isPresent())
            accessCategory = maybeRights.get();
        else {
            log.warn("no dataset rights found in solr response: {} using NO_ACCESS", line);
            accessCategory = AccessCategory.NO_ACCESS;
        }
    }

    private boolean isDatasetRights(String s) {
        try {
            AccessCategory.valueOf(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public DatasetRights datasetRights() {
        FileRights rights = new FileRights();
        rights.setFileRights(accessCategory);
        rights.setEmbargoDate(available);
        DatasetRights datasetRights = new DatasetRights();
        datasetRights.setAccessCategory(accessCategory);
        datasetRights.setDefaultFileRights(rights);
        return datasetRights;
    }
}
