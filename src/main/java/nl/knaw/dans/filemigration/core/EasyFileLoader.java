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
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.util.Arrays;
import java.util.List;

public class EasyFileLoader {
  private static final Logger log = LoggerFactory.getLogger(EasyFileLoader.class);

  private final EasyFileDAO easyFileDAO;
  private final ExpectedFileDAO expectedDAO;

  public EasyFileLoader(EasyFileDAO easyFileDAO, ExpectedFileDAO expectedDAO) {
    this.expectedDAO = expectedDAO;
    this.easyFileDAO = easyFileDAO;
  }

  public void loadFromCsv(FedoraToBagCsv csv) {
    if (!csv.getComment().contains("OK"))
      log.warn("skipped {}", csv);
    else {
      if (!csv.getComment().contains("no payload"))
        fedoraFiles(csv);
      Arrays.stream(migrationFiles).iterator()
          .forEachRemaining(f -> saveExpected(new ExpectedFile(csv.getDoi(), "easy_migration/"+f)));
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
      ExpectedFile expected = new ExpectedFile(csv.getDoi(), f.getSha1checksum(), f.getPath(), f.getPid(),removeOriginal);
      try {
        saveExpected(expected);
      } catch(PersistenceException e){
        // logged as error by org.hibernate.engine.jdbc.spi.SqlExceptionHelper
        if (!(e.getCause() instanceof ConstraintViolationException))
          throw e;
        else {
          if (expected.getRemoved_duplicate_file_count() > 10) {
            // TODO temporary safe guard?
            log.error("too many retries on duplicate file, skipping: {}", expected);
          }
          else {
            expected.incRemoved_duplicate_file_count();
            saveExpected(expected);
          }
        }
      }
    }
  }

  public List<EasyFile> getByDatasetId(FedoraToBagCsv csv) {
    return easyFileDAO.findByDatasetId(csv.getDatasetId());
  }

  public void saveExpected(ExpectedFile expected) {
      expectedDAO.create(expected);
  }

  private static final String forbidden = ":*?\"<>|;#";
  private static final char[] forbiddenInFileName = ":*?\"<>|;#".toCharArray();
  private static final char[] forbiddenInFolders = (forbidden + "'(),[]&+'").toCharArray();
  private static String replaceForbidden (String s, char[] forbidden) {
    for (char c: forbidden)
      s = s.replace(c,'_');
    return s;
  }
}
