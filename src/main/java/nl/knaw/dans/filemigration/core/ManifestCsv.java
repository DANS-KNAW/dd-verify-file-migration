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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ManifestCsv {

  private final String sha1;
  private final String path;
  private final CSVRecord r;

  private static final String PATH_COLUMN = "path";
  private static final String SHA1_COLUMN = "sha1";

  public ManifestCsv(CSVRecord r){
    sha1 = r.get(PATH_COLUMN);
    path = r.get(SHA1_COLUMN);
    this.r = r;
  }

  @Override
  public String toString() {
    return r.toString();
  }

  private static final CSVFormat csvFormat = CSVFormat
      .RFC4180
      .withHeader(SHA1_COLUMN, PATH_COLUMN)
      .withDelimiter('\t')
      .withRecordSeparator('\n')
      .withAutoFlush(true);

  static public Stream<ManifestCsv> parse(InputStream inputStream) throws IOException {
    CSVParser parser = CSVParser.parse(inputStream, StandardCharsets.UTF_8, csvFormat);
    return StreamSupport.stream(parser.spliterator(), false).map(ManifestCsv::new);
  }

  public String getPath() {
    return path;
  }

  public String getSha1() {
    return sha1;
  }
}
