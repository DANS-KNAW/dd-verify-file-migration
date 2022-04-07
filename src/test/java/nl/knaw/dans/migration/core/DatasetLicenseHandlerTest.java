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

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DatasetLicenseHandlerTest {

    @Test
    public void parseDD_874() throws IOException {
        String license = DatasetLicenseHandler
            .parseLicense(new FileInputStream("src/test/resources/ddm/DD-874.xml"), AccessCategory.OPEN_ACCESS_FOR_REGISTERED_USERS);
        assertEquals("http://creativecommons.org/licenses/by/4.0", license);
    }

    @Test
    public void defaultCC0() {
        String license = DatasetLicenseHandler
            .parseLicense(new ByteArrayInputStream("<emd></emd>".getBytes(StandardCharsets.UTF_8)), AccessCategory.OPEN_ACCESS);
        assertEquals(DatasetLicenseHandler.cc0, license);
    }

    @Test
    public void defaultDans() {
        ByteArrayInputStream ddmIS = new ByteArrayInputStream("<emd></emd>".getBytes(StandardCharsets.UTF_8));
        String license = DatasetLicenseHandler.parseLicense(ddmIS, AccessCategory.OPEN_ACCESS_FOR_REGISTERED_USERS);
        assertEquals(DatasetLicenseHandler.dansLicense, license);
    }

    @Test
    public void defaultNone() {
        String ddm = "<emd xmlns:dct=\"http://purl.org/dc/terms/\"><dct:license>accept</dct:license></emd>";
        String license = DatasetLicenseHandler
            .parseLicense(new ByteArrayInputStream(ddm.getBytes(StandardCharsets.UTF_8)), AccessCategory.NO_ACCESS);
        assertNull(license);
    }

    @Test
    public void alsoForDdm() {
        String start = "<ddm:DDM xmlns:ddm=\"http://easy.dans.knaw.nl/schemas/md/ddm/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
        String ddm = start + "<ddm:dcmiMetadata><dcterms:license xsi:type=\"dcterms:URI\">http://opensource.org/licenses/MIT</dcterms:license></ddm:dcmiMetadata></ddm:DDM>";
        String license = DatasetLicenseHandler
            .parseLicense(new ByteArrayInputStream(ddm.getBytes(StandardCharsets.UTF_8)), AccessCategory.NO_ACCESS);
        assertEquals("http://opensource.org/licenses/MIT", license);
    }
}
