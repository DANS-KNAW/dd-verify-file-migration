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
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Arrays;
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
    ExpectedFileDAO expectedFileDAO = createMock(ExpectedFileDAO.class);
    EasyFileDAO easyFileDAO = createMock(EasyFileDAO.class);
    for (ExpectedFile ef: expectedMigrationFiles())
      expectSuccess(expectedFileDAO, ef);

    replay(csv, expectedFileDAO, easyFileDAO);
    new EasyFileLoader(easyFileDAO, expectedFileDAO).loadFromCsv(csv);
    verify(csv, expectedFileDAO, easyFileDAO);
  }

  @Test
  public void skipFailed() {

    FedoraToBagCsv csv = mockCSV("Failed for some reason", "blabla");

    ExpectedFileDAO expectedFileDAO = createMock(ExpectedFileDAO.class);
    EasyFileDAO easyFileDAO = createMock(EasyFileDAO.class);
    replay(csv, expectedFileDAO, easyFileDAO);
    new EasyFileLoader(null, null).loadFromCsv(csv);
    verify(csv, expectedFileDAO, easyFileDAO);
  }

  @Test
  public void migrationFilesForEmptyDataset() {

    FedoraToBagCsv csv = mockCSV("OK", "blabla");
    EasyFileDAO easyFileDAO = mockEasyFileDAO();
    ExpectedFileDAO expectedFileDAO = createMock(ExpectedFileDAO.class);
    for (ExpectedFile ef: expectedMigrationFiles())
      expectSuccess(expectedFileDAO, ef);

    replay(csv, easyFileDAO, expectedFileDAO);
    new EasyFileLoader(easyFileDAO, expectedFileDAO).loadFromCsv(csv);
    verify(csv, easyFileDAO, expectedFileDAO);
  }

  @Test
  public void duplicateFiles() {

    FedoraToBagCsv csv = mockCSV("OK", "blabla");
    EasyFileDAO easyFileDAO = mockEasyFileDAO(
        new EasyFile("easy-file:2","easy-folder:1",datasetId,"some_/file.txt","file.txt",10,"text","DEPOSITOR","ANONYMOUS","ANONYMOUS","123"),
        new EasyFile("easy-file:1","easy-folder:1",datasetId,"some?/file.txt","file.txt",10,"text","DEPOSITOR","ANONYMOUS","ANONYMOUS","123")
    );
    ExpectedFileDAO expectedFileDAO = createMock(ExpectedFileDAO.class);
    expectSuccess(expectedFileDAO, new ExpectedFile(doi,"some_/file.txt",0,false,"123","easy-file:2","some_/file.txt",false,false,false));
    expectThrows(expectedFileDAO, new ExpectedFile(doi,"some_/file.txt",0,false,"123","easy-file:1","some?/file.txt",false,false,true));
    expectSuccess(expectedFileDAO, new ExpectedFile(doi,"some_/file.txt",1,false,"123","easy-file:1","some?/file.txt",false,false,true));
    for (ExpectedFile ef: expectedMigrationFiles())
      expectSuccess(expectedFileDAO, ef);

    replay(csv, easyFileDAO, expectedFileDAO);
    new EasyFileLoader(easyFileDAO, expectedFileDAO).loadFromCsv(csv);
    verify(csv, easyFileDAO, expectedFileDAO);
  }

  @Test
  public void duplicateCausedByOriginalVersioned() {

    FedoraToBagCsv csv = mockCSV("OK", "original_versioned");
    EasyFileDAO easyFileDAO = mockEasyFileDAO(
        new EasyFile("easy-file:2","easy-folder:1",datasetId,"some_/file.txt","file.txt",10,"text","DEPOSITOR","ANONYMOUS","ANONYMOUS","123"),
        new EasyFile("easy-file:1","easy-folder:1",datasetId,"original/some?/file.txt","file.txt",10,"text","DEPOSITOR","ANONYMOUS","ANONYMOUS","123")
    );
    ExpectedFileDAO expectedFileDAO = createMock(ExpectedFileDAO.class);
    expectSuccess(expectedFileDAO, new ExpectedFile(doi,"some_/file.txt",0,false,"123","easy-file:2","some_/file.txt",false,false,false));
    expectThrows(expectedFileDAO, new ExpectedFile(doi,"some_/file.txt",0,true,"123","easy-file:1","original/some?/file.txt",false,false,true));
    expectSuccess(expectedFileDAO, new ExpectedFile(doi,"some_/file.txt",1,true,"123","easy-file:1","original/some?/file.txt",false,false,true));
    for (ExpectedFile ef: expectedMigrationFiles())
      expectSuccess(expectedFileDAO, ef);

    replay(csv, easyFileDAO, expectedFileDAO);
    new EasyFileLoader(easyFileDAO, expectedFileDAO).loadFromCsv(csv);
    verify(csv, easyFileDAO, expectedFileDAO);
  }


  @Test
  public void dropThumbnail() {

    FedoraToBagCsv csv = mockCSV("OK", "blabla");
    EasyFileDAO easyFileDAO = mockEasyFileDAO(
        new EasyFile("easy-file:1","easy-folder:1",datasetId,"some_thumbnails/image_small.png","image_small.png",10,"png","DEPOSITOR","ANONYMOUS","ANONYMOUS","123")
    );
    ExpectedFileDAO expectedFileDAO = createMock(ExpectedFileDAO.class);
    expectSuccess(expectedFileDAO, new ExpectedFile(doi,"some_thumbnails/image_small.png",0,false,"123","easy-file:1","some_thumbnails/image_small.png",false,true,false));
    for (ExpectedFile ef: expectedMigrationFiles())
      expectSuccess(expectedFileDAO, ef);

    replay(csv, easyFileDAO, expectedFileDAO);
    new EasyFileLoader(easyFileDAO, expectedFileDAO).loadFromCsv(csv);
    verify(csv, easyFileDAO, expectedFileDAO);
  }

  private void expectSuccess(ExpectedFileDAO expectedFileDAO, ExpectedFile ef) {
    expectedFileDAO.create(ef);
    EasyMock.expectLastCall().once();
  }

  private void expectThrows(ExpectedFileDAO expectedFileDAO, ExpectedFile ef) {
    expectedFileDAO.create(ef);
    EasyMock.expectLastCall().andThrow(new PersistenceException(new ConstraintViolationException("",null,"blabla"))).once();
  }

  private List<ExpectedFile> expectedMigrationFiles() {

    ArrayList<ExpectedFile> expectedFiles = new ArrayList<>();
    for (String f : new String[] { "provenance.xml", "dataset.xml", "files.xml", "emd.xml" }) {
      ExpectedFile expectedFile = new ExpectedFile();
      expectedFile.setDoi(doi);
      expectedFile.setExpected_path("easy-migration/" + f);
      expectedFile.setAdded_during_migration(true);
      expectedFiles.add(expectedFile);
    }
    return expectedFiles;
  }

  private FedoraToBagCsv mockCSV(String comment, String type) {

    FedoraToBagCsv mockedCSV = createMock(FedoraToBagCsv.class);
    expect(mockedCSV.getComment()).andReturn(comment).anyTimes();
    expect(mockedCSV.getTransformation()).andReturn(type).anyTimes();
    expect(mockedCSV.getDoi()).andReturn(doi).anyTimes();
    expect(mockedCSV.getDatasetId()).andReturn(datasetId).anyTimes();
    return mockedCSV;
  }

  private EasyFileDAO mockEasyFileDAO(EasyFile... easyFiles) {

    EasyFileDAO mock = createMock(EasyFileDAO.class);
    expect(mock.findByDatasetId(datasetId)).andReturn(Arrays.asList(easyFiles)).once();
    return mock;
  }
}
