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
package nl.knaw.dans.migration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ConfigurationTest {
    private final YamlConfigurationFactory<DdVerifyMigrationConfiguration> factory;

    {
        final ObjectMapper mapper = Jackson.newObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        factory = new YamlConfigurationFactory<>(DdVerifyMigrationConfiguration.class, Validators.newValidator(), mapper, "dw");
    }

    @Test
    public void can_read_assembly_dist_config() {
        String cfgFile = "src/main/assembly/dist/cfg/config.yml";
        assertDoesNotThrow(() -> factory.build(FileInputStream::new, cfgFile));
    }

    @Test
    public void can_read_debug_etc_config() {
        String cfgFile = "src/test/resources/debug-etc/config.yml";
        assertDoesNotThrow(() -> factory.build(FileInputStream::new, cfgFile));
    }
}
