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

package nl.knaw.dans.filemigration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.knaw.dans.lib.util.DataverseClientFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import static org.apache.commons.csv.CSVFormat.RFC4180;

public class DdVerifyFileMigrationConfiguration extends Configuration {
  @Valid
  @NotNull
  private DataverseClientFactory dataverse;

  @Valid
  @NotNull
  private URI bagStoreBaseUri;

  @Valid
  @NotNull
  private URI bagIndexBaseUri;

  @Valid
  @NotNull
  private URI solrBaseUri;

  @Valid
  @Nullable
  private DataSourceFactory easyDb = new DataSourceFactory();

  @Valid
  @NotNull
  private DataSourceFactory verificationDatabase = new DataSourceFactory();

  HashMap<String, String> accountSubstitutes = new HashMap<>();

  @JsonProperty("easyDb")
  public DataSourceFactory getEasyDb() {
    return easyDb;
  }

  public void setEasyDb(DataSourceFactory easyDb) {
    this.easyDb = easyDb;
  }

  @JsonProperty("verificationDatabase")
  public DataSourceFactory getVerificationDatabase() {
    return verificationDatabase;
  }

  public void loadAccountSubstitutes(Namespace namespace) throws IOException {
    CSVFormat format = RFC4180.withHeader("old", "new")
            .withDelimiter(',')
            .withFirstRecordAsHeader()
            .withRecordSeparator(System.lineSeparator());
    String configDir = new File(namespace.getString("file")).getParent();
    File csvFile = new File(configDir + "/account-substitutes.csv");
    if (csvFile.exists() && 0 != csvFile.length()) {
      List<CSVRecord> records = CSVParser.parse(new FileInputStream(csvFile), StandardCharsets.UTF_8, format).getRecords();
      records.forEach(csvRecord -> accountSubstitutes.put(csvRecord.get("old"), csvRecord.get("new")));
    }
  }

  public HashMap<String, String> getAccountSubstitutes() {
    return accountSubstitutes;
  }

  public void setAccountSubstitutes(HashMap<String, String> accountSubstitutes) {
    this.accountSubstitutes = accountSubstitutes;
  }

  public void setVerificationDatabase(DataSourceFactory dataSourceFactory) {
    this.verificationDatabase = dataSourceFactory;
  }

  public DataverseClientFactory getDataverse() {
    return dataverse;
  }

  public void setDataverse(DataverseClientFactory dataverse) {
    this.dataverse = dataverse;
  }

  public URI getBagStoreBaseUri() {
    return bagStoreBaseUri;
  }

  public void setBagStoreBaseUri(URI bagStoreUri) {
    this.bagStoreBaseUri = bagStoreUri;
  }

  public URI getBagIndexBaseUri() {
    return bagIndexBaseUri;
  }

  public void setBagIndexBaseUri(URI bagIndexBaseUri) {
    this.bagIndexBaseUri = bagIndexBaseUri;
  }

  public URI getSolrBaseUri() {
    return solrBaseUri;
  }

  public void setSolrBaseUri(URI solrBaseUri) {
    this.solrBaseUri = solrBaseUri;
  }
}
