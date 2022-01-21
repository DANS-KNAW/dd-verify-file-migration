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
package nl.knaw.dans.filemigration.cli;

import io.dropwizard.Application;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.dans.filemigration.DdVerifyFileMigrationConfiguration;
import nl.knaw.dans.filemigration.core.EasyFileLoader;
import nl.knaw.dans.filemigration.core.EasyFileLoaderImpl;
import nl.knaw.dans.filemigration.core.FedoraToBagCsv;
import nl.knaw.dans.filemigration.db.EasyFileDAO;
import nl.knaw.dans.filemigration.db.ExpectedFileDAO;
import nl.knaw.dans.lib.util.DefaultConfigEnvironmentCommand;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.ConfigurationException;
import java.io.File;

public class LoadFromFedoraCommand extends DefaultConfigEnvironmentCommand<DdVerifyFileMigrationConfiguration> {

    private static final Logger log = LoggerFactory.getLogger(LoadFromFedoraCommand.class);
    private final HibernateBundle<DdVerifyFileMigrationConfiguration> easyBundle;
    private final HibernateBundle<DdVerifyFileMigrationConfiguration> expectedBundle;

    /**
     * Creates a new environment command.
     *
     * @param application the application providing this command
     */
    public LoadFromFedoraCommand(
        Application<DdVerifyFileMigrationConfiguration> application,
        HibernateBundle<DdVerifyFileMigrationConfiguration> easyBundle,
        HibernateBundle<DdVerifyFileMigrationConfiguration> expectedBundle
    ) {
        super(application, "load-from-fedora", "Load expected table with info from easy_files in fs-rdb and transformation rules", true);
        this.easyBundle = easyBundle;
        this.expectedBundle = expectedBundle;
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);
        subparser.addArgument("csv")
            .type(File.class)
            .nargs("+")
            .help("CSV file produced by easy-fedora-to-bag");
    }

    @Override
    protected void run(Bootstrap<DdVerifyFileMigrationConfiguration> bootstrap, Namespace namespace, DdVerifyFileMigrationConfiguration configuration) throws Exception {
        // TODO move up to DefaultConfigEnvironmentCommand in dans-java-utils with generic <T>
        initialize(bootstrap, namespace, configuration);
        super.run(bootstrap, namespace, configuration); // calls the abstract run(environment, namespace, configuration)
    }

    public void initialize(Bootstrap<DdVerifyFileMigrationConfiguration> bootstrap, Namespace namespace, DdVerifyFileMigrationConfiguration configuration) throws Exception {
        if (configuration.getEasyDb() == null)
            throw new ConfigurationException(getName() + " requires easyDb parameters in " + namespace.get("file"));
        bootstrap.addBundle(easyBundle);
    }

    @Override
    protected void run(Environment environment, Namespace namespace, DdVerifyFileMigrationConfiguration configuration) throws Exception {
        // https://stackoverflow.com/questions/42384671/dropwizard-hibernate-no-session-currently-bound-to-execution-context
        EasyFileDAO easyFileDAO = new EasyFileDAO(easyBundle.getSessionFactory());
        ExpectedFileDAO expectedDAO = new ExpectedFileDAO(expectedBundle.getSessionFactory());
        EasyFileLoader proxy = new UnitOfWorkAwareProxyFactory(easyBundle, expectedBundle)
            .create(
                EasyFileLoaderImpl.class,
                new Class[] { EasyFileDAO.class, ExpectedFileDAO.class },
                new Object[] { easyFileDAO, expectedDAO }
            );
        for (File file : namespace.<File> getList("csv")) {
            log.info(file.toString());
            for (CSVRecord r : FedoraToBagCsv.parse(file)) {
                proxy.loadFromCsv(new FedoraToBagCsv(r));
            }
        }
    }
}
