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

package nl.knaw.dans.filemigration;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.dans.filemigration.api.ActualFile;
import nl.knaw.dans.filemigration.api.EasyFile;
import nl.knaw.dans.filemigration.api.ExpectedFile;
import nl.knaw.dans.filemigration.cli.LoadFromFedoraCommand;
import nl.knaw.dans.filemigration.cli.LoadFromDataverseCommand;

public class DdVerifyFileMigrationApplication extends Application<DdVerifyFileMigrationConfiguration> {

    private final HibernateBundle<DdVerifyFileMigrationConfiguration> easyBundle = new HibernateBundle<DdVerifyFileMigrationConfiguration>(EasyFile.class) {

      @Override
      public DataSourceFactory getDataSourceFactory(DdVerifyFileMigrationConfiguration configuration) {
        return configuration.getEasyDb();
      }

      @Override
      public String name() {
        // the default "hibernate" is apparently required for at least one bundle:
        // the verificationBundle as that one is required by all subcommands
        return "easyBundle";
      }
    };

    private final HibernateBundle<DdVerifyFileMigrationConfiguration> verificationBundle = new HibernateBundle<DdVerifyFileMigrationConfiguration>(ExpectedFile.class, ActualFile.class) {

      @Override
      public DataSourceFactory getDataSourceFactory(DdVerifyFileMigrationConfiguration configuration) {
        return configuration.getVerificationDatabase();
      }
    };

    public static void main(final String[] args) throws Exception {
        new DdVerifyFileMigrationApplication().run(args);
    }

    @Override
    public String getName() {
        return "DD Verify File Migration";
    }

    @Override
    public void initialize(final Bootstrap<DdVerifyFileMigrationConfiguration> bootstrap) {
        bootstrap.addBundle(easyBundle);
        bootstrap.addBundle(verificationBundle);
        bootstrap.addCommand(new LoadFromFedoraCommand(this, easyBundle, verificationBundle));
        bootstrap.addCommand(new LoadFromDataverseCommand(this, verificationBundle));
    }

    @Override
    public void run(final DdVerifyFileMigrationConfiguration configuration, final Environment environment) {
      environment.healthChecks().unregister("hibernate");
      environment.healthChecks().unregister("easyBundle");
    }
}
