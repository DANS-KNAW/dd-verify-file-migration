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

import io.dropwizard.hibernate.UnitOfWork;
import nl.knaw.dans.filemigration.api.EasyFile;
import nl.knaw.dans.filemigration.api.ExpectedFile;
import nl.knaw.dans.filemigration.db.EasyFileDAO;
import nl.knaw.dans.filemigration.db.ExpectedFileDAO;

import java.io.File;
import java.net.URI;
import java.util.List;

public class EasyFileLoaderImpl extends EasyFileLoader {

    public EasyFileLoaderImpl(EasyFileDAO easyFileDAO, ExpectedFileDAO expectedDAO, URI solrBaseUri, File configDir) {
        super(easyFileDAO, expectedDAO, solrBaseUri, configDir);
    }

  @UnitOfWork("easyBundle")
  public List<EasyFile> getByDatasetId(FedoraToBagCsv csv) {
    return super.getByDatasetId(csv);
  }

  @UnitOfWork("hibernate")
  public void saveExpected(ExpectedFile expected) {
    super.saveExpected(expected);
  }

}
