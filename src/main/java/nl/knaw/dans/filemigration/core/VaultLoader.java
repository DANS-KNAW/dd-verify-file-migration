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

import nl.knaw.dans.filemigration.api.ExpectedFile;
import nl.knaw.dans.filemigration.db.ExpectedFileDAO;
import org.hibernate.cfg.NotYetImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.UUID;

public class VaultLoader {

  private static final Logger log = LoggerFactory.getLogger(VaultLoader.class);

  private final ExpectedFileDAO expectedDAO;
  private URI bagstoreBaseUri;

  public VaultLoader(ExpectedFileDAO expectedDAO, URI bagstoreBaseUri) {
    this.expectedDAO = expectedDAO;
    this.bagstoreBaseUri = bagstoreBaseUri;
  }

  public void saveExpected(ExpectedFile expected) {
    expectedDAO.create(expected);
  }

  public void loadFromVault(UUID uuid) {
    log.info("Loading {}", uuid);
    throw new NotYetImplementedException();
  }
}
