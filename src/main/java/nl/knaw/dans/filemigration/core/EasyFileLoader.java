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
import java.nio.CharBuffer;
import java.util.ArrayList;
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
    if (csv.getComment().contains("OK") && !csv.getComment().contains("no payload"))
      fedoraFiles(csv);
    else log.warn("skipped {}", csv);
  }

  /** note: bag-to-deposit also adds emd.xml to bags from the vault, that does not apply to migration */
  private static final String[] migrationFiles = { "provenance.xml", "dataset.xml", "files.xml" };

  void fedoraFiles(FedoraToBagCsv csv) {
    log.trace(csv.toString());
    // read fedora files before adding expected migration files
    // thus we don't write anything when reading fails
    List<EasyFile> easyFiles = getByDatasetId(csv);
    for (EasyFile f: easyFiles) {
      // note: biggest pdf/image option for europeana in easy-fedora-to-bag does not apply to migration
      ExpectedFile expected = transformedFedoraFile(csv, f);
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
    Arrays.stream(migrationFiles).iterator()
        .forEachRemaining(f -> saveExpected(addedMigrationFile(csv, f)));
  }

  public List<EasyFile> getByDatasetId(FedoraToBagCsv csv) {
    List<EasyFile> easyFiles = easyFileDAO.findByDatasetId(csv.getDatasetId());
    if (easyFiles == null) // TODO how caused by mocking? Manual test with easy-dataset:3 did not cause null
      return new ArrayList<EasyFile>();
    else return easyFiles;
  }

  public void saveExpected(ExpectedFile expected) {
      expectedDAO.create(expected);
  }

  private static ExpectedFile addedMigrationFile(FedoraToBagCsv csv, String migrationFile) {
    ExpectedFile expected = new ExpectedFile();
    expected.setDoi(csv.getDoi());
    expected.setSha1_checksum("");
    expected.setEasy_file_id("");
    expected.setFs_rdb_path("");
    expected.setExpected_path("migration/" + migrationFile);
    expected.setAdded_during_migration(true);
    expected.setRemoved_thumbnail(false);
    expected.setRemoved_original_directory(false);
    expected.setRemoved_duplicate_file_count(0);
    expected.setTransformed_name(false);
    return expected;
  }

  private static ExpectedFile transformedFedoraFile(FedoraToBagCsv csv, EasyFile ef) {
    log.trace("EasyFile = {}" , ef);
    final boolean removeOriginal = csv.getType().startsWith("original") && ef.getPath().startsWith("original/");
    final String path = removeOriginal
        ? ef.getPath().replace("original/","")
        : ef.getPath();
    final String file = replaceForbidden(path.replaceAll(".*/",""), forbiddenInFileName);
    final String folder = replaceForbidden(path.replaceAll("[^/]*",""), forbiddenInFolders);
    final String dvPath = folder + "/" + file;

    ExpectedFile expected = new ExpectedFile();
    expected.setDoi(csv.getDoi());
    expected.setSha1_checksum(ef.getSha1checksum());
    expected.setEasy_file_id(ef.getPid());
    expected.setFs_rdb_path(path);
    expected.setExpected_path(dvPath);
    expected.setAdded_during_migration(false);
    expected.setRemoved_thumbnail(path.matches(".*thumbnails/.*_small.(png|jpg|tiff)"));
    expected.setRemoved_original_directory(removeOriginal);
    expected.setRemoved_duplicate_file_count(0);
    expected.setTransformed_name(!path.equals(dvPath));
    return expected;
  }

  private static final String forbiddenInFileName = ":*?\"<>|;#";
  private static final String forbiddenInFolders = forbiddenInFileName + "'(),[]&+'";
  private static String replaceForbidden (String s, String forbidden) {
    final CharBuffer b = CharBuffer.allocate(s.length());
    for (char c : s.toCharArray()){
      b.append(forbidden.indexOf(c) >= 0 ? '_' : c);
    }
    return b.toString();
  }
}
