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

import nl.knaw.dans.filemigration.api.EasyFile;
import nl.knaw.dans.filemigration.api.ExpectedFile;
import nl.knaw.dans.filemigration.db.EasyFileDAO;
import nl.knaw.dans.filemigration.db.ExpectedFileDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EasyFileLoader extends ExpectedLoader {
  private static final Logger log = LoggerFactory.getLogger(EasyFileLoader.class);

  private final EasyFileDAO easyFileDAO;

  public EasyFileLoader(EasyFileDAO easyFileDAO, ExpectedFileDAO expectedDAO) {
    super(expectedDAO);
    this.easyFileDAO = easyFileDAO;
  }

  public void loadFromCsv(FedoraToBagCsv csv) {
    if (!csv.getComment().contains("OK"))
      log.warn("skipped {}", csv);
    else {
      if (!csv.getComment().contains("no payload"))
        fedoraFiles(csv);
      expectedMigrationFiles(csv.getDoi(),migrationFiles);
    }
  }

  /** note: easy-convert-bag-to-deposit does not add emd.xml to bags from the vault */
  private static final String[] migrationFiles = { "provenance.xml", "dataset.xml", "files.xml", "emd.xml" };

  private void fedoraFiles(FedoraToBagCsv csv) {
    log.trace(csv.toString());
    // read fedora files before adding expected migration files
    // thus we don't write anything when reading fails
    List<EasyFile> easyFiles = getByDatasetId(csv);
    for (EasyFile f: easyFiles) {
      // note: biggest pdf/image option for europeana in easy-fedora-to-bag does not apply to migration
      log.trace("EasyFile = {}" , f);
      final boolean removeOriginal = csv.getTransformation().startsWith("original") && f.getPath().startsWith("original/");
      ExpectedFile expected = new ExpectedFile(csv.getDoi(), f.getSha1checksum(), f.getPath(), f.getPid(), removeOriginal);
      retriedSave(expected);
    }
  }

  public List<EasyFile> getByDatasetId(FedoraToBagCsv csv) {
    return easyFileDAO.findByDatasetId(csv.getDatasetId());
  }
}
