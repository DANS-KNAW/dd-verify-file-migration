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

import nl.knaw.dans.lib.dataverse.model.dataset.DatasetVersion;

import java.util.Comparator;

public class DatasetVersionComparator implements Comparator<DatasetVersion> {
    @Override
    public int compare(DatasetVersion v1, DatasetVersion v2) {
        int major = Integer.compare(v1.getVersionNumber(), v2.getVersionNumber());
        if (major != 0)
            return major;
        else
            return Integer.compare(v1.getVersionMinorNumber(), v2.getVersionMinorNumber());
    }
}
