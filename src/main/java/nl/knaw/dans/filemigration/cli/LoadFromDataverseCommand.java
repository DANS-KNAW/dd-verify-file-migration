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
import net.sourceforge.argparse4j.inf.MutuallyExclusiveGroup;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.dans.filemigration.DdVerifyFileMigrationConfiguration;
import nl.knaw.dans.filemigration.core.DataverseLoader;
import nl.knaw.dans.filemigration.core.DataverseLoaderImpl;
import nl.knaw.dans.filemigration.core.FedoraToBagCsv;
import nl.knaw.dans.filemigration.db.ActualFileDAO;
import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.SearchOptions;
import nl.knaw.dans.lib.dataverse.model.search.DatasetResultItem;
import nl.knaw.dans.lib.dataverse.model.search.ResultItem;
import nl.knaw.dans.lib.dataverse.model.search.SearchItemType;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static java.util.Collections.singletonList;

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

        MutuallyExclusiveGroup g = subparser.addMutuallyExclusiveGroup();
        g.addArgument("-d", "--doi")
            .dest("doi")
            .help("The DOI for which to load the files, for example: 'doi:10.17026/dans-xtz-qa6j'");

        g.addArgument("--csv")
            .dest("csv")
            .type(File.class)
            .help("CSV file produced by easy-fedora-to-bag");
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
        String file = namespace.getString("csv");
        if (doi != null)
            proxy.loadFromDataset(doi);
        else if (file == null) {
            log.info("No DOI(s) provided, loading all datasets");
            Iterator<ResultItem> resultItems = client.search().iterator("*", datasetOption());
            toDoiSet(resultItems).forEach(proxy::loadFromDataset);
        }
        else {
            log.info("Loading DOIs found in {}", file);
            for(CSVRecord r: FedoraToBagCsv.parse(new File(file))) {
                proxy.loadFromDataset("doi:"+new FedoraToBagCsv(r).getDoi());
            }
        }
    }

    private SearchOptions datasetOption() {
        SearchOptions searchOptions = new SearchOptions();
        searchOptions.setTypes(singletonList(SearchItemType.dataset));
        return searchOptions;
    }

    private Set<String> toDoiSet(Iterator<ResultItem> itemIterator) {
        Set<String> set = new HashSet<>();
        while (itemIterator.hasNext()) {
            DatasetResultItem ri = (DatasetResultItem) itemIterator.next();
            String doi = ri.getGlobalId();
            log.debug("id={} duplicate={} majorVersion={} minorVersion={} fileCount={} versionId={} ", doi, set.contains(doi), ri.getMajorVersion(), ri.getMinorVersion(), ri.getFileCount(), ri.getVersionId());
            set.add(doi);
        }
        return set;
    }
}
