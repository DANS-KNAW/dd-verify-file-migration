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
package nl.knaw.dans.migration.db;

import io.dropwizard.hibernate.AbstractDAO;
import nl.knaw.dans.migration.core.tables.EasyFile;
import nl.knaw.dans.migration.core.tables.InputDataset;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputDatasetDAO extends AbstractDAO<EasyFile> {
  private static final Logger log = LoggerFactory.getLogger(InputDatasetDAO.class);

  public InputDatasetDAO(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public void create(InputDataset input) {
    log.trace(input.toString());
    currentSession().save(input);
  }

  public void deleteBatch(String batch, String source) {
    log.info("deleting InputDataset {} {}", batch, source);
    int r = currentSession()
        .createQuery("DELETE FROM InputDataset WHERE batch = :batch AND source = :source")
        .setParameter("batch", batch)
        .setParameter("source", source)
        .executeUpdate();
    log.info("deleted {} {} from InputDataset", r, source);
  }}
