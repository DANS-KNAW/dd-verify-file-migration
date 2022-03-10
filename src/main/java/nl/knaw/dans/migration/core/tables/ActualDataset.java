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
package nl.knaw.dans.migration.core.tables;

import org.hsqldb.lib.StringUtil;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Objects;

@Entity
@IdClass(ActualDatasetKey.class)
@Table(name = "actual_datasets")
public class ActualDataset {
  // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#schema-generation

  // most lengths from easy-dtap/provisioning/roles/easy-fs-rdb/templates/create-easy-db-tables.sql
  // doi length as in dd-dtap/shared-code/dataverse/scripts/database/create/create_v*.sql

  @Id
  @Column(length = 255)
  private String doi;

  @Id
  @Column(name="major_version_nr")
  private int majorVersionNr;

  @Id
  @Column(name="minor_version_nr")
  private int minorVersionNr;

  @Column(name="accessible_to")
  private String accessibleTo;

  @Nullable
  @Column(name="embargo_date")
  private String embargoDate;

  @Nullable
  @Column(name="depositor")
  private String depositor;
}
