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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import nl.knaw.dans.lib.dataverse.DataverseItemDeserializer;
import nl.knaw.dans.lib.dataverse.MetadataFieldDeserializer;
import nl.knaw.dans.lib.dataverse.ResultItemDeserializer;
import nl.knaw.dans.lib.dataverse.model.dataset.MetadataField;
import nl.knaw.dans.lib.dataverse.model.dataverse.DataverseItem;
import nl.knaw.dans.lib.dataverse.model.search.ResultItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class BagInfoResultTest {

  protected ObjectMapper mapper;
  private static final Class<?> classUnderTest = BagInfoResult.class;

  @BeforeEach
  public void beforeEach() {
    mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addDeserializer(MetadataField.class, new MetadataFieldDeserializer());
    module.addDeserializer(DataverseItem.class, new DataverseItemDeserializer());
    module.addDeserializer(ResultItem.class, new ResultItemDeserializer(mapper));
    mapper.registerModule(module);
  }

  @Test
  public void canParse() throws IOException {
    mapper.readValue(new File("src/test/resources/bag-info.json"), BagInfoEnvelope.class).getResult().getBagInfo();
  }

  @Test
  public void canNotParseEmpty() throws IOException {
    try {
      mapper.readValue("", BagInfoEnvelope.class);
    } catch (MismatchedInputException ignored) {}
  }
}
