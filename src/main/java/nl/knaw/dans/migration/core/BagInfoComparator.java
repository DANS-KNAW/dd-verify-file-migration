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

import org.joda.time.DateTime;

import java.util.Comparator;

public class BagInfoComparator implements Comparator<BagInfo> {

  @Override
  public int compare(BagInfo v1, BagInfo v2) {
    // with no more precision than seconds, the dates might be identical
    if (v1.getBagId().equals(v1.getBaseId())) {
      // lets at least determine the very first bag in the sequence
      if (v1.getBagId().equals(v1.getBaseId()))
        return -1;
      if (v2.getBagId().equals(v2.getBaseId()))
        return 1;
    }
    // TODO easy-sword2-examples creates bags at a fixed date
    DateTime dtV1 = new DateTime(v1.getCreated());
    DateTime dtV2 = new DateTime(v2.getCreated());
    return dtV1.compareTo(dtV2);
  }
}
