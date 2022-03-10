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
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.dans.migration.cli.LoadFromDataverseCommand;
import nl.knaw.dans.migration.cli.LoadFromFedoraCommand;
import nl.knaw.dans.migration.cli.LoadFromVaultCommand;
import nl.knaw.dans.migration.core.tables.ActualFile;
import nl.knaw.dans.migration.core.EasyFile;
import nl.knaw.dans.migration.core.tables.ExpectedFile;

public class DdVerifyMigrationApplication extends Application<DdVerifyMigrationConfiguration> {

    private final HibernateBundle<DdVerifyMigrationConfiguration> easyBundle = new HibernateBundle<DdVerifyMigrationConfiguration>(EasyFile.class) {

        @Override
        public DataSourceFactory getDataSourceFactory(DdVerifyMigrationConfiguration configuration) {
            return configuration.getEasyDb();
        }

        @Override
        public String name() {
            // the default "hibernate" is apparently required for at least one bundle:
            // the verificationBundle as that one is required by all subcommands
            return "easyBundle";
        }
    };

    private final HibernateBundle<DdVerifyMigrationConfiguration> verificationBundle = new HibernateBundle<DdVerifyMigrationConfiguration>(ExpectedFile.class, ActualFile.class) {

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
        bootstrap.addBundle(verificationBundle);// easyBundle is added by LoadFromFedoraCommand
        bootstrap.addCommand(new LoadFromFedoraCommand(this, easyBundle, verificationBundle));
        bootstrap.addCommand(new LoadFromDataverseCommand(this, verificationBundle));
        bootstrap.addCommand(new LoadFromVaultCommand(this, verificationBundle));
    }

    @Override
    public void run(final DdVerifyMigrationConfiguration configuration, final Environment environment) {
        environment.healthChecks().unregister("hibernate");
        environment.healthChecks().unregister("easyBundle");
    }
}
