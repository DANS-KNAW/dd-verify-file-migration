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
import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class EaseyFileLoaderTest {
  private static final String datasetId = "easy-dataset:123";
  private static final String doi = "10.80270/test-nySe-x6f-kf66";

  @Test
  public void skipNoPayload() {

    FedoraToBagCsv csv = mockCSV("OK no payload", "blabla");
    replay(csv);
    new EasyFileLoader(null, null)
        .loadFromCsv(csv);
    verify(csv);
  }

  @Test
  public void skipFailed() {

    FedoraToBagCsv csv = mockCSV("Failed for some reason", "blabla");
    replay(csv);
    new EasyFileLoader(null, null)
        .loadFromCsv(csv);
    verify(csv);
  }

  @Test
  public void migrationFilesForEmptyDataset() {

    FedoraToBagCsv csv = mockCSV("OK", "blabla");
    EasyFileDAO easyFileDAO = mockEasyFileDAO(new ArrayList<EasyFile>());
    ExpectedFileDAO expectedFileDAO = mockExpectedFileDAO(expectedMigrationFiles());

    replay(csv, easyFileDAO, expectedFileDAO);
    new EasyFileLoader(easyFileDAO, expectedFileDAO).loadFromCsv(csv);
    verify(csv, easyFileDAO, expectedFileDAO);
  }

  private List<ExpectedFile> expectedMigrationFiles() {

    ArrayList<ExpectedFile> expectedFiles = new ArrayList<>();
    for (String f : new String[] { "provenance.xml", "dataset.xml", "files.xml" }) {
      ExpectedFile expectedFile = new ExpectedFile();
      expectedFile.setDoi(doi);
      expectedFile.setExpected_path("migration/" + f);
      expectedFile.setAdded_during_migration(true);
      expectedFiles.add(expectedFile);
    }
    return expectedFiles;
  }

  private FedoraToBagCsv mockCSV(String comment, String type) {

    FedoraToBagCsv mockedCSV = createMock(FedoraToBagCsv.class);
    expect(mockedCSV.getComment()).andReturn(comment).anyTimes();
    expect(mockedCSV.getType()).andReturn(type).anyTimes();
    expect(mockedCSV.getDoi()).andReturn(doi).anyTimes();
    expect(mockedCSV.getDatasetId()).andReturn(datasetId).anyTimes();
    return mockedCSV;
  }

  private ExpectedFileDAO mockExpectedFileDAO(List<ExpectedFile> expectedFiles) {

    // TODO a variant that can throw a duplicate key exception
    ExpectedFileDAO mock = createMock(ExpectedFileDAO.class);
    for (ExpectedFile f : expectedFiles) {
      mock.create(f);
      EasyMock.expectLastCall().once();
    }
    return mock;
  }

  private EasyFileDAO mockEasyFileDAO(List<EasyFile> easyFiles) {

    EasyFileDAO mock = createMock(EasyFileDAO.class);
    expect(mock.findByDatasetId(datasetId)).andReturn(easyFiles).once();
    return mock;
  }
}
