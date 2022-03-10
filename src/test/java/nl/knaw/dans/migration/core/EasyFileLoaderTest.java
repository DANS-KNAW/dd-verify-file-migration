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

import nl.knaw.dans.migration.core.tables.ExpectedFile;
import nl.knaw.dans.migration.db.EasyFileDAO;
import nl.knaw.dans.migration.db.ExpectedFileDAO;
import org.easymock.EasyMock;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import javax.persistence.PersistenceException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.*;

public class EasyFileLoaderTest {
  private static final String datasetId = "easy-dataset:123";
  private static final String doi = "10.80270/test-nySe-x6f-kf66";
  private static final String expectedSolr = "2022-03-08," + DatasetRights.NO_ACCESS;

  private static class Loader extends EasyFileLoader {

    private final String expectedSolr;

    public Loader(String expectedSolr, EasyFileDAO easyFileDAO, ExpectedFileDAO expectedDAO) {
      super(easyFileDAO, expectedDAO, solrBaseUri());
      this.expectedSolr = expectedSolr;
    }

    @Override
    protected String rightsFromSolr(String datasetId) {
      return expectedSolr;
    }
  }

  static URI solrBaseUri(){
    try {
      return new URI("http://does.not.exist.dans.knaw.nl:8080/solr/");
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Test
  public void skipNoPayload() {

    FedoraToBagCsv csv = mockCSV("OK no payload", "blabla");
    ExpectedFileDAO expectedFileDAO = createMock(ExpectedFileDAO.class);
    EasyFileDAO easyFileDAO = createMock(EasyFileDAO.class);
    for (ExpectedFile ef: expectedMigrationFiles("NONE"))
      expectSuccess(expectedFileDAO, ef);

    replay(csv, expectedFileDAO, easyFileDAO);
    new Loader(expectedSolr, easyFileDAO, expectedFileDAO).loadFromCsv(csv);
    verify(csv, expectedFileDAO, easyFileDAO);
  }

  @Test
  public void skipFailed() {

    FedoraToBagCsv csv = mockCSV("Failed for some reason", "blabla");

    ExpectedFileDAO expectedFileDAO = createMock(ExpectedFileDAO.class);
    EasyFileDAO easyFileDAO = createMock(EasyFileDAO.class);
    replay(csv, expectedFileDAO, easyFileDAO);
    new Loader("", null, null).loadFromCsv(csv);
    verify(csv, expectedFileDAO, easyFileDAO);
  }

  @Test
  public void migrationFilesForEmptyDataset() {

    FedoraToBagCsv csv = mockCSV("OK", "blabla");
    EasyFileDAO easyFileDAO = mockEasyFileDAO();
    ExpectedFileDAO expectedFileDAO = createMock(ExpectedFileDAO.class);
    for (ExpectedFile ef: expectedMigrationFiles("NONE"))
      expectSuccess(expectedFileDAO, ef);

    replay(csv, easyFileDAO, expectedFileDAO);
    new Loader(expectedSolr, easyFileDAO, expectedFileDAO).loadFromCsv(csv);
    verify(csv, easyFileDAO, expectedFileDAO);
  }

  @Test
  public void dd874() {

    FedoraToBagCsv csv = mockCSV("OK", "blabla");
    EasyFileDAO easyFileDAO = mockEasyFileDAO();
    ExpectedFileDAO expectedFileDAO = createMock(ExpectedFileDAO.class);
    for (ExpectedFile ef: expectedMigrationFiles("ANONYMOUS"))
      expectSuccess(expectedFileDAO, ef);

    replay(csv, easyFileDAO, expectedFileDAO);
    new Loader("\"\",\"OPEN_ACCESS,accept,http://creativecommons.org/licenses/by/4.0,Econsultancy\"", easyFileDAO, expectedFileDAO).loadFromCsv(csv);
    verify(csv, easyFileDAO, expectedFileDAO);
  }

  @Test
  public void dd875() {

    FedoraToBagCsv csv = mockCSV("OK", "blabla");
    EasyFileDAO easyFileDAO = mockEasyFileDAO();
    ExpectedFileDAO expectedFileDAO = createMock(ExpectedFileDAO.class);
    for (ExpectedFile ef: expectedMigrationFiles("RESTRICTED_REQUEST"))
      expectSuccess(expectedFileDAO, ef);

    replay(csv, easyFileDAO, expectedFileDAO);
    new Loader("2009-06-04,\"RAAP Archeologisch Adviesbureau,GROUP_ACCESS\"", easyFileDAO, expectedFileDAO).loadFromCsv(csv);
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
    expectSuccess(expectedFileDAO, new ExpectedFile(doi,"some_/file.txt",0,false,"123","easy-file:2","some_/file.txt",false,false,false, "ANONYMOUS", "ANONYMOUS"));
    expectThrows(expectedFileDAO, new ExpectedFile(doi,"some_/file.txt",0,false,"123","easy-file:1","some?/file.txt",false,false,true, "ANONYMOUS", "ANONYMOUS"));
    expectSuccess(expectedFileDAO, new ExpectedFile(doi,"some_/file.txt",1,false,"123","easy-file:1","some?/file.txt",false,false,true, "ANONYMOUS", "ANONYMOUS"));
    for (ExpectedFile ef: expectedMigrationFiles("NONE"))
      expectSuccess(expectedFileDAO, ef);

    replay(csv, easyFileDAO, expectedFileDAO);
    new Loader(expectedSolr, easyFileDAO, expectedFileDAO).loadFromCsv(csv);
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
    expectSuccess(expectedFileDAO, new ExpectedFile(doi,"some_/file.txt",0,false,"123","easy-file:2","some_/file.txt",false,false,false, "ANONYMOUS", "ANONYMOUS"));
    expectThrows(expectedFileDAO, new ExpectedFile(doi,"some_/file.txt",0,true,"123","easy-file:1","original/some?/file.txt",false,false,true, "ANONYMOUS", "ANONYMOUS"));
    expectSuccess(expectedFileDAO, new ExpectedFile(doi,"some_/file.txt",1,true,"123","easy-file:1","original/some?/file.txt",false,false,true, "ANONYMOUS", "ANONYMOUS"));
    for (ExpectedFile ef: expectedMigrationFiles("NONE"))
      expectSuccess(expectedFileDAO, ef);

    replay(csv, easyFileDAO, expectedFileDAO);
    new Loader(expectedSolr, easyFileDAO, expectedFileDAO).loadFromCsv(csv);
    verify(csv, easyFileDAO, expectedFileDAO);
  }


  @Test
  public void dropThumbnail() {

    FedoraToBagCsv csv = mockCSV("OK", "blabla");
    EasyFileDAO easyFileDAO = mockEasyFileDAO(
        new EasyFile("easy-file:1","easy-folder:1",datasetId,"some_thumbnails/image_small.png","image_small.png",10,"png","DEPOSITOR","ANONYMOUS","ANONYMOUS","123")
    );
    ExpectedFileDAO expectedFileDAO = createMock(ExpectedFileDAO.class);
    expectSuccess(expectedFileDAO, new ExpectedFile(doi,"some_thumbnails/image_small.png",0,false,"123","easy-file:1","some_thumbnails/image_small.png",false,true,false, "ANONYMOUS", "ANONYMOUS"));
    for (ExpectedFile ef: expectedMigrationFiles("NONE"))
      expectSuccess(expectedFileDAO, ef);

    replay(csv, easyFileDAO, expectedFileDAO);
    new Loader(expectedSolr, easyFileDAO, expectedFileDAO).loadFromCsv(csv);
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

  private List<ExpectedFile> expectedMigrationFiles(String rights) {

    ArrayList<ExpectedFile> expectedFiles = new ArrayList<>();
    for (String f : new String[] { "provenance.xml", "dataset.xml", "files.xml", "emd.xml" }) {
      ExpectedFile expectedFile = new ExpectedFile();
      expectedFile.setDoi(doi);
      expectedFile.setExpectedPath("easy-migration/" + f);
      expectedFile.setAddedDuringMigration(true);
      expectedFiles.add(expectedFile);
      expectedFile.setAccessibleTo(rights);
      expectedFile.setVisibleTo("ANONYMOUS");
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
