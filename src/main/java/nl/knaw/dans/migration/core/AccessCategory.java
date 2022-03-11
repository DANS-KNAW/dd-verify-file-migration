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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum AccessCategory {

    OPEN_ACCESS("ANONYMOUS"),
    OPEN_ACCESS_FOR_REGISTERED_USERS("KNOWN"),
    REQUEST_PERMISSION("RESTRICTED_REQUEST"),
    NO_ACCESS("NONE"),
    GROUP_ACCESS("RESTRICTED_REQUEST");
    private static final Logger log = LoggerFactory.getLogger(AccessCategory.class);

    private final String fileRights;

    AccessCategory(String fileRights) {
        this.fileRights = fileRights;
    }

    public String getFileRights() {
        return fileRights;
    }
}
