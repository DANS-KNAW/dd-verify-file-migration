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
import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.SearchOptions;
import nl.knaw.dans.lib.dataverse.model.search.DatasetResultItem;
import nl.knaw.dans.lib.dataverse.model.search.ResultItem;
import nl.knaw.dans.lib.dataverse.model.search.SearchItemType;
import nl.knaw.dans.lib.util.DefaultConfigEnvironmentCommand;
import nl.knaw.dans.migration.DdVerifyMigrationConfiguration;
import nl.knaw.dans.migration.core.DataverseLoader;
import nl.knaw.dans.migration.core.FedoraToBagCsv;
import nl.knaw.dans.migration.db.ActualDatasetDAO;
import nl.knaw.dans.migration.db.ActualFileDAO;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;

public class LoadFromDataverseCommand extends DefaultConfigEnvironmentCommand<DdVerifyMigrationConfiguration> {

    private final String destDoi = "doi";
    private final String destCsv = "csv";
    private final String destUuids = "UUIDs";
    private final String destMode = "mode";
    private enum Mode {ALL, FILES, DATASETS};
    private static final Logger log = LoggerFactory.getLogger(LoadFromDataverseCommand.class);
    private final HibernateBundle<DdVerifyMigrationConfiguration> verificationBundle;

    /**
     * Creates a new environment command.
     *
     * @param application the application providing this command
     */
    public LoadFromDataverseCommand(
        Application<DdVerifyMigrationConfiguration> application,
        HibernateBundle<DdVerifyMigrationConfiguration> verificationBundle
    ) {
        super(application, "load-from-dataverse", "Load actual tables with info from dataverse");
        this.verificationBundle = verificationBundle;
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        MutuallyExclusiveGroup g = subparser.addMutuallyExclusiveGroup();
        g.addArgument("-d", "--" + destDoi)
            .dest(destDoi)
            .help("The DOI for which to load the files, for example: 'doi:10.17026/dans-xtz-qa6j'");

        g.addArgument("--" + destCsv)
            .dest(destCsv)
            .type(File.class)
            .help("CSV file produced by easy-fedora-to-bag");

        g.addArgument("--" + destMode)
            .dest(destMode)
            .setDefault(Mode.ALL)
            .type(Mode.class)
            .help("files require more writing, dataset require more reading");

        g.addArgument("--" + destUuids)
            .dest(destUuids)
            .type(File.class)
            .help(".txt file with bag ids");
    }

    @Override
    protected void run(Environment environment, Namespace namespace, DdVerifyMigrationConfiguration configuration) throws Exception {
        // https://www.dropwizard.io/en/stable/manual/hibernate.html#transactional-resource-methods-outside-jersey-resources
        DataverseClient client = configuration.getDataverse().build();
        SessionFactory verificationBundleSessionFactory = verificationBundle.getSessionFactory();
        DataverseLoader proxy = new UnitOfWorkAwareProxyFactory(verificationBundle)
            .create(
                DataverseLoader.class,
                new Class[] { DataverseClient.class, ActualFileDAO.class , ActualDatasetDAO.class},
                new Object[] {
                        client,
                        new ActualFileDAO(verificationBundleSessionFactory),
                        new ActualDatasetDAO(verificationBundleSessionFactory)
                }
            );
        String doi = namespace.getString(destDoi);
        String file = namespace.getString(destCsv);
        String uuids = namespace.getString(destUuids);
        Mode mode = Mode.valueOf(namespace.getString(destMode));
        boolean doFiles = Arrays.asList(Mode.FILES, Mode.ALL).contains(mode);
        boolean doDatasets = Arrays.asList(Mode.DATASETS, Mode.ALL).contains(mode);
        if (doi != null)
            proxy.loadFromDataset(doi, doFiles, doDatasets);
        else if (uuids != null) {
            log.info("Loading UUIDs found in {}", uuids);
            FileUtils.readLines(new File(uuids), UTF_8).forEach(line ->
                doFirst(
                    datasetIterator(client, "dansBagId:urn:uuid:" + line),
                    item -> proxy.loadFromDataset(((DatasetResultItem) item).getGlobalId(), doFiles, doDatasets)
                )
            );
        }
        else if (file == null) {
            log.info("No DOI(s)/UUIDs provided, loading all datasets");
            Iterator<ResultItem> iterator = datasetIterator(client, "*");
            String last = "";
            while(iterator.hasNext()) {
                String globalId = ((DatasetResultItem) iterator.next()).getGlobalId();
                if (!globalId.equals(last))
                    proxy.loadFromDataset(globalId, doFiles, doDatasets);
                last = globalId;
                log.trace("done with "+last);
            }
        }
        else {
            log.info("Loading DOIs found in {}", file);
            for(CSVRecord r: FedoraToBagCsv.parse(new File(file))) {
                FedoraToBagCsv fedoraToBagCsv = new FedoraToBagCsv(r);
                if (fedoraToBagCsv.getComment().contains("OK"))
                    proxy.loadFromDataset("doi:" + fedoraToBagCsv.getDoi(), doFiles, doDatasets);
            }
        }
    }

    private void doFirst(Iterator<ResultItem> iterator, Consumer<ResultItem> action) {
        if (iterator.hasNext())
            action.accept(iterator.next());
    }

    private Iterator<ResultItem> datasetIterator(DataverseClient client, String s) {
        log.info("searching " + s);
        SearchOptions searchOptions = new SearchOptions();
        searchOptions.setTypes(singletonList(SearchItemType.dataset));
        return client.search().iterator(s, searchOptions);
    }
}
