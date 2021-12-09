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
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.dans.filemigration.DdVerifyFileMigrationConfiguration;
import nl.knaw.dans.filemigration.core.DataverseLoader;
import nl.knaw.dans.filemigration.core.DataverseLoaderImpl;
import nl.knaw.dans.filemigration.db.ActualFileDAO;
import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.SearchOptions;
import nl.knaw.dans.lib.dataverse.model.search.DatasetResultItem;
import nl.knaw.dans.lib.dataverse.model.search.ResultItem;
import nl.knaw.dans.lib.dataverse.model.search.SearchItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

public class LoadFromDataverseCommand extends EnvironmentCommand<DdVerifyFileMigrationConfiguration> {

    private static final Logger log = LoggerFactory.getLogger(LoadFromDataverseCommand.class);
    private final HibernateBundle<DdVerifyFileMigrationConfiguration> verificationBundle;

    /**
     * Creates a new environment command.
     *
     * @param application the application providing this command
     */
    public LoadFromDataverseCommand(
        Application<DdVerifyFileMigrationConfiguration> application,
        HibernateBundle<DdVerifyFileMigrationConfiguration> verificationBundle
    ) {
        super(application, "load-from-dataverse", "Load actual table with file info from dataverse");
        this.verificationBundle = verificationBundle;
    }

    @Override
    public void configure(Subparser subparser) {
        // mandatory variant of: super.configure(subparser);
        subparser.addArgument("file")
            .type(File.class)
            .required(true)
            .help("application configuration file");

        subparser.addArgument("-d", "--doi")
            .dest("doi")
            .nargs("?")
            .help("The DOI for which to load the files");
    }

    @Override
    protected void run(Environment environment, Namespace namespace, DdVerifyFileMigrationConfiguration configuration) throws Exception {
        // https://stackoverflow.com/questions/42384671/dropwizard-hibernate-no-session-currently-bound-to-execution-context
        DataverseClient client = configuration.getDataverse().build();
        DataverseLoader proxy = new UnitOfWorkAwareProxyFactory(verificationBundle)
            .create(
                DataverseLoaderImpl.class,
                new Class[] { DataverseClient.class, ActualFileDAO.class },
                new Object[] { client, new ActualFileDAO(verificationBundle.getSessionFactory()) }
            );
        String doi = namespace.getString("doi");
        if (doi != null) proxy.loadFromDataset(doi);
        else {
            log.info("No DOI provided, loading all datasets");
            Iterator<ResultItem> resultItems = client.search().iterator("*", datasetOption());
            toDoiStream(resultItems).forEach(proxy::loadFromDataset);
        }
    }

    private SearchOptions datasetOption() {
        SearchOptions searchOptions = new SearchOptions();
        searchOptions.setTypes(singletonList(SearchItemType.dataset));
        return searchOptions;
    }

    private Stream<String> toDoiStream(Iterator<ResultItem> itemIterator) {
        Spliterator<ResultItem> itemSpliterator = spliteratorUnknownSize(itemIterator, Spliterator.ORDERED);
        Stream<ResultItem> itemStream = stream(itemSpliterator, false);
        return itemStream.map(ri -> ((DatasetResultItem) ri).getGlobalId());
    }
}
