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
package nl.knaw.dans.migration.cli;

import io.dropwizard.Application;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.dans.lib.util.DefaultConfigEnvironmentCommand;
import nl.knaw.dans.migration.DdVerifyMigrationConfiguration;
import nl.knaw.dans.migration.core.Mode;
import nl.knaw.dans.migration.core.VaultLoader;
import nl.knaw.dans.migration.db.ExpectedDatasetDAO;
import nl.knaw.dans.migration.db.ExpectedFileDAO;
import nl.knaw.dans.migration.db.InputDatasetDAO;
import org.apache.commons.io.FileUtils;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.UUID;

public class LoadFromVaultCommand extends DefaultConfigEnvironmentCommand<DdVerifyMigrationConfiguration> {

    private static final Logger log = LoggerFactory.getLogger(LoadFromVaultCommand.class);
    private final HibernateBundle<DdVerifyMigrationConfiguration> verificationBundle;

    /**
     * Creates a new environment command.
     *
     * @param application the application providing this command
     */
    public LoadFromVaultCommand(
        Application<DdVerifyMigrationConfiguration> application,
        HibernateBundle<DdVerifyMigrationConfiguration> expectedBundle
    ) {
        super(application, "load-from-vault", "Load expected tables with info from manifest-sha1.txt of bags in the vault");
        this.verificationBundle = expectedBundle;
    }

    @Override
    public void configure(Subparser subparser) {
        super.addFileArgument(subparser);

        Mode.configure(
            subparser.addArgument("--mode")
        );
        subparser.addArgument("-u", "--uuids")
            .dest("uuids")
            .required(true)
            .type(File.class)
            .help("file with UUIDs of a bag in the vault");
        subparser.addArgument("-s", "--store")
            .type(String.class)
            .required(true)
            .help("name of a bag store in the vault, saved in input_datasets table");
    }

    @Override
    protected void run(Environment environment, Namespace namespace, DdVerifyMigrationConfiguration configuration) throws Exception {
        log.info(namespace.getAttrs().toString());
        // https://www.dropwizard.io/en/stable/manual/hibernate.html#transactional-resource-methods-outside-jersey-resources
        SessionFactory verificationBundleSessionFactory = verificationBundle.getSessionFactory();
        VaultLoader proxy = new UnitOfWorkAwareProxyFactory(verificationBundle)
            .create(
                VaultLoader.class,
                new Class[] { ExpectedFileDAO.class, ExpectedDatasetDAO.class, InputDatasetDAO.class, URI.class, URI.class, File.class },
                new Object[] {
                        new ExpectedFileDAO(verificationBundleSessionFactory),
                        new ExpectedDatasetDAO(verificationBundleSessionFactory),
                        new InputDatasetDAO(verificationBundleSessionFactory),
                        configuration.getBagStoreBaseUri(),
                        configuration.getBagIndexBaseUri(),
                        new File(namespace.getString("file")).getParentFile(),
                }
            );
        String file = namespace.getString("uuids");
        String store = namespace.getString("store");
        Mode mode = Mode.from(namespace);
        if (file != null) {
            proxy.deleteBatch(file, store);
            String uuids = FileUtils.readFileToString(new File(file), Charset.defaultCharset());
            for (String s : uuids.split(System.lineSeparator())) {
                proxy.loadFromVault(UUID.fromString(s.trim()), mode, file, store);
            }
        }
    }
}
