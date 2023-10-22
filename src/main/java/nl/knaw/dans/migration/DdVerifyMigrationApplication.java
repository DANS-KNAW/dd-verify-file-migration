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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import nl.knaw.dans.migration.cli.LoadFromDataverseCommand;
import nl.knaw.dans.migration.core.tables.ActualDataset;
import nl.knaw.dans.migration.core.tables.ActualFile;
import nl.knaw.dans.migration.core.tables.ExpectedDataset;
import nl.knaw.dans.migration.core.tables.ExpectedFile;
import nl.knaw.dans.migration.core.tables.InputDataset;

public class DdVerifyMigrationApplication extends Application<DdVerifyMigrationConfiguration> {

    private final HibernateBundle<DdVerifyMigrationConfiguration> verificationBundle =
            new HibernateBundle<DdVerifyMigrationConfiguration>(ExpectedFile.class, ActualFile.class, ExpectedDataset.class, InputDataset.class, ActualDataset.class) {

        @Override
        public DataSourceFactory getDataSourceFactory(DdVerifyMigrationConfiguration configuration) {
            return configuration.getVerificationDatabase();
        }
    };

    public static void main(final String[] args) throws Exception {
        new DdVerifyMigrationApplication().run(args);
    }

    @Override
    public String getName() {
        return "DD Verify Migration";
    }

    @Override
    public void initialize(final Bootstrap<DdVerifyMigrationConfiguration> bootstrap) {
        bootstrap.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        bootstrap.getObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        bootstrap.addBundle(verificationBundle);
        bootstrap.addCommand(new LoadFromDataverseCommand(this, verificationBundle));
    }

    @Override
    public void run(final DdVerifyMigrationConfiguration configuration, final Environment environment) {
        environment.healthChecks().unregister("hibernate");
        environment.healthChecks().unregister("easyBundle");
    }
}
