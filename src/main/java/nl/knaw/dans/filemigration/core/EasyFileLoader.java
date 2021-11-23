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
import nl.knaw.dans.filemigration.db.EasyFileDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class EasyFileLoader {
  private static final Logger log = LoggerFactory.getLogger(EasyFileLoader.class);

  private final EasyFileDAO dao;

  public EasyFileLoader(EasyFileDAO dao) {
    this.dao = dao;
  }

  public void loadFromDatasetIds(Iterator<String> ids) {
    Spliterator<String> spliterator = Spliterators.spliteratorUnknownSize(ids, Spliterator.ORDERED);
    StreamSupport.stream(spliterator, false).forEach(this::loadFromDatasetId);
  }

  @UnitOfWork
  public void loadFromDatasetId(String id) {
    for (Object ef : dao.findByDatasetId("easy-dataset:9")) {
      log.trace("{}" , ef);
      // TODO apply transformation rules and add to Expected table
    }
  }
}
