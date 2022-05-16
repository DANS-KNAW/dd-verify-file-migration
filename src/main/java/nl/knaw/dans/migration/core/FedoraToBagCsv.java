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
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FedoraToBagCsv {

  private final String datasetId;
  private final String doi;
  private final String comment;
  private final String transformation;
  private final String uuid1;
  private final String uuid2;
  private final CSVRecord r;

  private static final String DATASET_ID_COLUMN = "easyDatasetId";
  private static final String DOI_COLUMN = "doi";
  private static final String COMMENT_COLUMN = "comment";
  private static final String TRANSFORMATION_TYPE_COLUMN = "transformationType";
  private static final String UUID_1 = "uuid1";
  private static final String UUID_2 = "uuid2";

  public FedoraToBagCsv(CSVRecord r) {
    datasetId = r.get(DATASET_ID_COLUMN);
    doi = r.get(DOI_COLUMN);
    comment = r.get(COMMENT_COLUMN);
    transformation = r.get(TRANSFORMATION_TYPE_COLUMN);
    uuid1 = r.get(UUID_1);
    uuid2 = r.get(UUID_2);
    this.r = r;
  }

  @Override
  public String toString() {
    return r.toString();
  }

  // see https://github.com/DANS-KNAW/easy-fedora-to-bag/blob/8ef3a0bad/src/main/scala/nl/knaw/dans/easy/fedoratobag/CsvRecord.scala#L42-L46
  public static final CSVFormat csvFormat = CSVFormat
      .RFC4180
      .withHeader(DATASET_ID_COLUMN, UUID_1, UUID_2, DOI_COLUMN, "depositor", TRANSFORMATION_TYPE_COLUMN, COMMENT_COLUMN)
      .withDelimiter(',')
      .withFirstRecordAsHeader()
      .withRecordSeparator(System.lineSeparator())
      .withAutoFlush(true);

  static public CSVParser parse(File file) throws IOException {
    return CSVParser.parse(file, StandardCharsets.UTF_8, csvFormat);
  }

  /**
   * @return possible values:
   * simple,
   * thematische-collectie,
   * original-versioned,
   * original-versioned without second bag,
   * fedora-versioned
   */
  public String getTransformation() {
    return transformation;
  }

  /**
   * @return possible values: "OK", "not strict OK", "OK; no payload...", "FAILED..."
   */
  public String getComment() {
    return comment;
  }

  public String getDoi() {
    return doi;
  }

  public String getDatasetId() {
    return datasetId;
  }

  public String getUuid1() {
    return uuid1;
  }

  public String getUuid2() {
    return uuid2;
  }
}
