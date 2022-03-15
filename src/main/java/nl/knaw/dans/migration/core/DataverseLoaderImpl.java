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

import io.dropwizard.hibernate.UnitOfWork;
import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.migration.core.tables.ActualDataset;
import nl.knaw.dans.migration.core.tables.ActualFile;
import nl.knaw.dans.migration.db.ActualDatasetDAO;
import nl.knaw.dans.migration.db.ActualFileDAO;

public class DataverseLoaderImpl extends DataverseLoader {

  public DataverseLoaderImpl(DataverseClient client, ActualFileDAO actualFileDAO, ActualDatasetDAO actualDatasetDAO) {
    super(client, actualFileDAO, actualDatasetDAO);
  }

  @UnitOfWork("hibernate")
  public void saveActualFile(ActualFile actualFile) {
    super.saveActualFile(actualFile);
  }

  @UnitOfWork("hibernate")
  public void saveActualDataset(ActualDataset actualDataset) {
    super.saveActualDataset(actualDataset);
  }
}
