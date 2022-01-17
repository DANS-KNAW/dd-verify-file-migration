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
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.dans.filemigration.DdVerifyFileMigrationConfiguration;
import nl.knaw.dans.filemigration.core.VaultLoader;
import nl.knaw.dans.filemigration.core.VaultLoaderImpl;
import nl.knaw.dans.filemigration.db.ExpectedFileDAO;
import nl.knaw.dans.lib.util.DefaultConfigEnvironmentCommand;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.UUID;

public class LoadFromVaultCommand extends DefaultConfigEnvironmentCommand<DdVerifyFileMigrationConfiguration> {

    private static final Logger log = LoggerFactory.getLogger(LoadFromVaultCommand.class);
    private final HibernateBundle<DdVerifyFileMigrationConfiguration> expectedBundle;

    /**
     * Creates a new environment command.
     *
     * @param application the application providing this command
     */
    public LoadFromVaultCommand(
        Application<DdVerifyFileMigrationConfiguration> application,
        HibernateBundle<DdVerifyFileMigrationConfiguration> expectedBundle
    ) {
        super(application, "load-from-vault", "Load expected table with info from manifest-sha1.txt of bags in the vault");
        this.expectedBundle = expectedBundle;
    }

    @Override
    public void configure(Subparser subparser) {
        super.addFileArgument(subparser);

        MutuallyExclusiveGroup group = subparser.addMutuallyExclusiveGroup().required(true);
        group.addArgument("-u", "--uuids")
            .dest("uuids")
            .type(File.class)
            .help("file with UUIDs of a bag in the vault");
        group.addArgument("-U", "--UUID")
            .dest("uuid")
            .type(String.class)
            .help("UUID of a bag in the vault");
        group.addArgument("-s", "--store")
            .type(String.class)
            .help("name of a bag store in the vault");
    }

    @Override
    protected void run(Environment environment, Namespace namespace, DdVerifyFileMigrationConfiguration configuration) throws Exception {
        log.info(namespace.getAttrs().toString());
        // https://stackoverflow.com/questions/42384671/dropwizard-hibernate-no-session-currently-bound-to-execution-context
        ExpectedFileDAO expectedDAO = new ExpectedFileDAO(expectedBundle.getSessionFactory());
        VaultLoader proxy = new UnitOfWorkAwareProxyFactory(expectedBundle)
            .create(
                VaultLoaderImpl.class,
                new Class[] { ExpectedFileDAO.class, URI.class, URI.class },
                new Object[] { expectedDAO, configuration.getBagStoreBaseUri(), configuration.getBagIndexBaseUri() }
            );
        String uuid = namespace.getString("uuid");
        String file = namespace.getString("uuids");
        String store = namespace.getString("store");
        if (uuid != null)
            proxy.loadFromVault(UUID.fromString(uuid));
        else if (file != null) {
            String uuids = FileUtils.readFileToString(new File(file), Charset.defaultCharset());
            for (String s : uuids.split(System.lineSeparator())) {
                proxy.loadFromVault(UUID.fromString(s.trim()));
            }
        }
        else {
            log.error("not yet implemented:loading from store {}", store);
        }
    }
}
