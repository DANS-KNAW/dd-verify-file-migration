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

import nl.knaw.dans.migration.core.tables.ExpectedDataset;

public class DatasetRights {
    AccessCategory accessCategory;
    FileRights defaultFileRights;

    public AccessCategory getAccessCategory() {
        return accessCategory;
    }

    public void setAccessCategory(AccessCategory accessCategory) {
        this.accessCategory = accessCategory;
    }

    public FileRights getDefaultFileRights() {
        return defaultFileRights;
    }

    public void setDefaultFileRights(FileRights defaultFileRights) {
        this.defaultFileRights = defaultFileRights;
    }

    public ExpectedDataset expectedDataset() {
        // ExpectedLoader.saveExpectedDataset applies account-substitutes.csv to depositor
        ExpectedDataset expectedDataset = new ExpectedDataset();
        expectedDataset.setAccessCategory(accessCategory);
        expectedDataset.setEmbargoDate(defaultFileRights);
        return expectedDataset;
    }
}
