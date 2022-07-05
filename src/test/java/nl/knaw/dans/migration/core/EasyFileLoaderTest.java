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

import nl.knaw.dans.migration.core.tables.EasyFile;
import nl.knaw.dans.migration.core.tables.ExpectedDataset;
import nl.knaw.dans.migration.core.tables.ExpectedFile;
import nl.knaw.dans.migration.core.tables.InputDataset;
import nl.knaw.dans.migration.db.EasyFileDAO;
import nl.knaw.dans.migration.db.ExpectedDatasetDAO;
import nl.knaw.dans.migration.db.ExpectedFileDAO;
import nl.knaw.dans.migration.db.InputDatasetDAO;
import org.apache.commons.csv.CSVParser;
import org.easymock.EasyMock;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import javax.persistence.PersistenceException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class EasyFileLoaderTest {
  private static final String datasetId = "easy-dataset:123";
  private static final String doi = "10.80270/test-nySe-x6f-kf66";
  private static final File csvFile = new File("fedora.csv");

  private static class Loader extends EasyFileLoader {
    private final String expectedSolr;

    final EasyFileDAO easyFileDAO;
    final ExpectedFileDAO expectedFileDAO;
    final ExpectedDatasetDAO expectedDatasetDAO;
    final InputDatasetDAO inputDatasetDAO;

    static Loader create() {
      return create("2022-03-08," + AccessCategory.NO_ACCESS + ",somebody,PUBLISHED,2022-03-25");
    }
    static Loader create(String expectedSolrResponse) {
      final EasyFileDAO easyFileDAO = createMock(EasyFileDAO.class);
      final ExpectedFileDAO expectedFileDAO = createMock(ExpectedFileDAO.class);
      final ExpectedDatasetDAO expectedDatasetDAO = createMock(ExpectedDatasetDAO.class);
      final InputDatasetDAO inputDatasetDAO = createMock(InputDatasetDAO.class);
      return new Loader(expectedSolrResponse, easyFileDAO, expectedFileDAO, expectedDatasetDAO, inputDatasetDAO);
    }

    public Loader(String expectedSolrResponse, EasyFileDAO easyFileDAO, ExpectedFileDAO expectedFileDAO, ExpectedDatasetDAO expectedDatasetDAO, InputDatasetDAO inputDatasetDAO) {
      super(easyFileDAO, expectedFileDAO, expectedDatasetDAO, inputDatasetDAO, dummyBaseUri(), dummyBaseUri(), new File("src/test/resources/debug-etc"));
      this.easyFileDAO = easyFileDAO;
      this.expectedFileDAO = expectedFileDAO;
      this.expectedDatasetDAO = expectedDatasetDAO;
      this.inputDatasetDAO = inputDatasetDAO;
      this.expectedSolr = expectedSolrResponse;
    }

    @Override
    protected String solrInfo(String datasetId) {
      return expectedSolr;
    }

    @Override
    protected String readEmd(String datasetId) {
      return "<ddm><license>http://creativecommons.org/publicdomain/zero/1.0</license></ddm>";
    }

    static URI dummyBaseUri(){
      try {
        return new URI("http://does.not.exist.dans.knaw.nl:8080/solr/");
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
      return null;
    }
  }

  @Test
  public void migrationFilesForSkippedPayload() throws IOException {

    FedoraToBagCsv csv = parseFedoraCsv("easy-dataset:123,uuid1,," + doi + ",user001,simple,OK no payload");

    Loader loader = Loader.create();
    for (ExpectedFile ef: expectedMigrationFiles())
      expectSuccess(loader.expectedFileDAO, ef);

    ExpectedDataset expectedDataset = new ExpectedDataset();
    expectedDataset.setDepositor("somebody");
    expectedDataset.setDoi(doi);
    expectedDataset.setAccessCategory(AccessCategory.NO_ACCESS);
    expectedDataset.setCitationYear("2022");
    expectedDataset.setExpectedVersions(1);
    expectSuccess(loader.expectedDatasetDAO,expectedDataset);

    replayLoadVerify(csv, Mode.BOTH, loader);
  }

  private void replayLoadVerify(FedoraToBagCsv csv, Mode mode, Loader loader) {
    replay(loader.easyFileDAO, loader.expectedFileDAO, loader.expectedDatasetDAO, loader.inputDatasetDAO);
    loader.loadFromCsv(csv, mode, csvFile);
    verify(loader.easyFileDAO, loader.expectedFileDAO, loader.expectedDatasetDAO, loader.inputDatasetDAO);
  }

  @Test
  public void skipFailed() throws IOException {

    FedoraToBagCsv csv = parseFedoraCsv("easy-dataset:123,uuid1,," + doi + ",user001,simple,Failed for some reason");
    Loader loader = Loader.create();

    replayLoadVerify(csv, Mode.BOTH, loader);
  }

  @Test
  public void skipFailedInput() throws IOException {

    FedoraToBagCsv csv = parseFedoraCsv("easy-dataset:123,uuid1,," + doi + ",user001,simple,Failed for some reason");
    Loader loader = Loader.create();
    expectSuccess(loader.inputDatasetDAO,new InputDataset(csv,csvFile));

    replayLoadVerify(csv, Mode.INPUT, loader);
  }

  @Test
  public void migrationFilesForEmptyDataset() throws IOException {

    FedoraToBagCsv csv = parseFedoraCsv("easy-dataset:123,uuid1,,"+doi+",user001,simple,OK");
    Loader loader = Loader.create();
    expect(loader.easyFileDAO.findByDatasetId("easy-dataset:123")).andReturn(Collections.emptyList()).once();
    for (ExpectedFile ef: expectedMigrationFiles())
      expectSuccess(loader.expectedFileDAO, ef);

    replayLoadVerify(csv, Mode.FILES, loader);
  }

  @Test
  public void dd874() throws IOException {
    ExpectedDataset expectedDataset = new ExpectedDataset();
    expectedDataset.setDepositor("does.not.exist@dans.knaw.nl");
    expectedDataset.setDoi(doi);
    expectedDataset.setAccessCategory(AccessCategory.OPEN_ACCESS);
    expectedDataset.setCitationYear("2022");
    expectedDataset.setLicenseUrl("http://creativecommons.org/publicdomain/zero/1.0");
    expectedDataset.setLicenseName("CC0-1.0");
    expectedDataset.setExpectedVersions(1);

    Loader loader = Loader.create("\"\",\"OPEN_ACCESS,accept,http://creativecommons.org/licenses/by/4.0,Econsultancy\",USER001,PUBLISHED,2022-03-25");
    expect(loader.easyFileDAO.findByDatasetId("easy-dataset:123")).andReturn(Collections.emptyList()).once();
    expectSuccess(loader.expectedDatasetDAO, expectedDataset);

    FedoraToBagCsv csv = parseFedoraCsv("easy-dataset:123,uuid1,,"+doi+",user001,simple,OK");
    for (ExpectedFile ef: expectedMigrationFiles())
      expectSuccess(loader.expectedFileDAO, ef);

    replayLoadVerify(csv, Mode.BOTH, loader);
  }

  @Test
  public void dd875() throws IOException {
    ExpectedDataset ed = new ExpectedDataset();
    ed.setDoi(doi);
    ed.setAccessCategory(AccessCategory.GROUP_ACCESS);
    ed.setDeleted(false);
    ed.setDepositor("somebody");
    ed.setCitationYear("2022");
    ed.setLicenseName("CC0-1.0");
    ed.setLicenseUrl("http://creativecommons.org/publicdomain/zero/1.0");
    ed.setExpectedVersions(1);

    FedoraToBagCsv csv = parseFedoraCsv("easy-dataset:123,uuid1,,"+doi+",user001,simple,OK");
    Loader loader = Loader.create("2009-06-04,\"RAAP Archeologisch Adviesbureau,GROUP_ACCESS\",somebody,PUBLISHED,2022-03-25");
    expect(loader.easyFileDAO.findByDatasetId("easy-dataset:123")).andReturn(Collections.emptyList()).once();
    for (ExpectedFile ef: expectedMigrationFiles())
      expectSuccess(loader.expectedFileDAO, ef);
    expectSuccess(loader.expectedDatasetDAO, ed);

    replayLoadVerify(csv, Mode.BOTH, loader);
  }

  @Test
  public void dd875Input() throws IOException {
    ExpectedDataset ed = new ExpectedDataset();
    ed.setDoi(doi);
    ed.setAccessCategory(AccessCategory.GROUP_ACCESS);
    ed.setDeleted(false);
    ed.setDepositor("somebody");
    ed.setCitationYear("2022");
    ed.setLicenseName("CC0-1.0");
    ed.setLicenseUrl("http://creativecommons.org/publicdomain/zero/1.0");
    ed.setExpectedVersions(1);

    FedoraToBagCsv csv = parseFedoraCsv("easy-dataset:123,uuid1,,"+doi+",user001,simple,OK");
    Loader loader = Loader.create("2009-06-04,\"RAAP Archeologisch Adviesbureau,GROUP_ACCESS\",somebody,PUBLISHED,2022-03-25");
    expectSuccess(loader.inputDatasetDAO,new InputDataset(csv,csvFile));

    replayLoadVerify(csv, Mode.INPUT, loader);
  }

  @Test
  public void withoutFiles() throws IOException {
    HashMap<String, Integer> uuidToVersions = new HashMap<>();
    uuidToVersions.put("",1);
    uuidToVersions.put(" ",1);
    uuidToVersions.put("00fab9df-0417-460b-bbb0-312aba55ed27",2);
    uuidToVersions.put("  00fab9df-0417-460b-bbb0-312aba55ed27  ",2);
    for (Map.Entry<String, Integer> entry : uuidToVersions.entrySet()) {
      String uuid = entry.getKey();
      Integer count = entry.getValue();
      FedoraToBagCsv csv = parseFedoraCsv("easy-dataset:123,blabla," + uuid + "," + doi + ",user001,simple,OK");

      ExpectedDataset ed = new ExpectedDataset();
      ed.setDoi(doi);
      ed.setAccessCategory(AccessCategory.GROUP_ACCESS);
      ed.setDeleted(false);
      ed.setDepositor("somebody");
      ed.setCitationYear("2022");
      ed.setLicenseName("CC0-1.0");
      ed.setLicenseUrl("http://creativecommons.org/publicdomain/zero/1.0");
      ed.setExpectedVersions(count);

      Loader loader = Loader.create("2009-06-04,GROUP_ACCESS,somebody,PUBLISHED,2022-03-25");
      expectSuccess(loader.expectedDatasetDAO, ed);
      expect(loader.easyFileDAO.findByDatasetId(datasetId)).andReturn(Collections.emptyList());
      for (ExpectedFile ef : expectedMigrationFiles())
        expectSuccess(loader.expectedFileDAO, ef);

      replayLoadVerify(csv, Mode.BOTH, loader);
    }
  }

  @Test
  public void withoutFilesInput() throws IOException {
    HashMap<String, Integer> uuidToVersions = new HashMap<>();
    uuidToVersions.put("",1);
    uuidToVersions.put(" ",1);
    uuidToVersions.put("00fab9df-0417-460b-bbb0-312aba55ed27",2);
    uuidToVersions.put("  00fab9df-0417-460b-bbb0-312aba55ed27  ",2);
    for (Map.Entry<String, Integer> entry : uuidToVersions.entrySet()) {
      String uuid = entry.getKey();
      FedoraToBagCsv csv = parseFedoraCsv("easy-dataset:123,blabla," + uuid + "," + doi + ",user001,simple,OK");

      Loader loader = Loader.create("2009-06-04,GROUP_ACCESS,somebody,PUBLISHED,2022-03-25");
      expectSuccess(loader.inputDatasetDAO,new InputDataset(csv,csvFile));

      replayLoadVerify(csv, Mode.INPUT, loader);
    }
  }

  //TODO ignore for now @Test
  public void duplicateFiles() throws IOException {

    FedoraToBagCsv csv = parseFedoraCsv("easy-dataset:123,uuid1,,"+doi+",user001,simple,OK");
    Loader loader = Loader.create();
    expect(loader.easyFileDAO.findByDatasetId(datasetId)).andReturn(Arrays.asList(
        mockEasyFile("easy-file:2", "some_/file.txt","file.txt", "text"),
        mockEasyFile("easy-file:1", "some?/file.txt","file.txt", "text"),
        mockEasyFile("easy-file:3", "some?/file.txt","file.txt", "text")
    )).once();
    expectSuccess(loader.expectedFileDAO, new ExpectedFile(doi,"some_/file.txt", false,"123","easy-file:2","some_/file.txt",false,false,false, "ANONYMOUS", "ANONYMOUS"));
    expectThrows(loader.expectedFileDAO, new ExpectedFile(doi,"some_/file.txt", false,"123","easy-file:1","some?/file.txt",false,false,true, "ANONYMOUS", "ANONYMOUS"));
    expectSuccess(loader.expectedFileDAO, new ExpectedFile(doi,"some_/file.txt", false,"123","easy-file:3","some?/file.txt",false,false,true, "ANONYMOUS", "ANONYMOUS"));
    for (ExpectedFile ef: expectedMigrationFiles())
      expectSuccess(loader.expectedFileDAO, ef);

    replayLoadVerify(csv, Mode.BOTH, loader);
  }

  //TODO ignore for now @Test
  public void duplicateCausedByOriginalVersioned() throws IOException {

    FedoraToBagCsv csv = parseFedoraCsv("easy-dataset:123,uuid1,,"+doi+",user001,simple,OK");
    Loader loader = Loader.create();
    expect(loader.easyFileDAO.findByDatasetId(datasetId)).andReturn(Arrays.asList(
        mockEasyFile("easy-file:2", "some_/file.txt","file.txt", "text"),
        mockEasyFile("easy-file:1", "original/some?/file.txt","file.txt", "text")
    )).once();
    expectSuccess(loader.expectedFileDAO, new ExpectedFile(doi,"some_/file.txt", false,"123","easy-file:2","some_/file.txt",false,false,false, "ANONYMOUS", "ANONYMOUS"));
    expectSuccess(loader.expectedFileDAO, new ExpectedFile(doi,"some_/file.txt", true,"123","easy-file:1","original/some?/file.txt",false,false,true, "ANONYMOUS", "ANONYMOUS"));
    expectSuccess(loader.expectedFileDAO, new ExpectedFile(doi,"some_/file.txt", true,"123","easy-file:1","original/some?/file.txt",false,false,true, "ANONYMOUS", "ANONYMOUS"));
    for (ExpectedFile ef: expectedMigrationFiles())
      expectSuccess(loader.expectedFileDAO, ef);

    replayLoadVerify(csv, Mode.BOTH, loader);
  }

  @Test
  public void dropThumbnail() throws IOException {

    FedoraToBagCsv csv = parseFedoraCsv("easy-dataset:123,uuid1,,"+doi+",user001,simple,OK");
    Loader loader = Loader.create();
    expect(loader.easyFileDAO.findByDatasetId(datasetId)).andReturn(Collections.singletonList(
        mockEasyFile("easy-file:1", "some_thumbnails/image_small.png", "image_small.png", "png")
    )).once();
    expectSuccess(loader.expectedFileDAO, new ExpectedFile(doi,"some_thumbnails/image_small.png", false,"123","easy-file:1","some_thumbnails/image_small.png",false,true,false, "ANONYMOUS", "ANONYMOUS"));
    for (ExpectedFile ef: expectedMigrationFiles())
      expectSuccess(loader.expectedFileDAO, ef);

    replayLoadVerify(csv, Mode.FILES, loader);
  }

  @Test
  public void dropThumbnailInput() throws IOException {

    FedoraToBagCsv csv = parseFedoraCsv("easy-dataset:123,uuid1,,"+doi+",user001,simple,OK");
    Loader loader = Loader.create();
    expectSuccess(loader.inputDatasetDAO,new InputDataset(csv,csvFile));

    replayLoadVerify(csv, Mode.INPUT, loader);
  }

  private FedoraToBagCsv parseFedoraCsv(String s) throws IOException {
    String csvHeader = "easyDatasetId,uuid1,uuid2,doi,depositor,transformationType,comment\n";
    return new FedoraToBagCsv(CSVParser.parse(csvHeader + s, FedoraToBagCsv.csvFormat).getRecords().get(0));
  }

  private void expectSuccess(InputDatasetDAO expectedDao, InputDataset expected) {
    expectedDao.create(expected);
    EasyMock.expectLastCall().once();
  }

  private void expectSuccess(ExpectedDatasetDAO expectedDao, ExpectedDataset expected) {
    expectedDao.create(expected);
    EasyMock.expectLastCall().once();
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
      expectedFiles.add(expectedFile);
      expectedFile.setDoi(doi);
      expectedFile.setExpectedPath("easy-migration/" + f);
      expectedFile.setAddedDuringMigration(true);
      expectedFile.setAccessibleTo("ANONYMOUS");
      expectedFile.setVisibleTo("ANONYMOUS");
    }
    return expectedFiles;
  }

  private EasyFile mockEasyFile(String pid, String path, String filename, String mimetype) {
    EasyFile easyFile = new EasyFile();
    easyFile.setPid(pid);
    easyFile.setParentSid("easy-folder:1");
    easyFile.setDatasetSid(EasyFileLoaderTest.datasetId);
    easyFile.setPath(path);
    easyFile.setFilename(filename);
    easyFile.setSize(10);
    easyFile.setMimetype(mimetype);
    easyFile.setCreatorRole("DEPOSITOR");
    easyFile.setVisibleTo("ANONYMOUS");
    easyFile.setAccessibleTo("ANONYMOUS");
    easyFile.setSha1Checksum("123");
    return easyFile;
  }
}
