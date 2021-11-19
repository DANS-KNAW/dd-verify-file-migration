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
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.knaw.dans.filemigration.DdVerifyFileMigrationConfiguration;
import nl.knaw.dans.filemigration.api.EasyFile;
import nl.knaw.dans.filemigration.db.EasyFileDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadFromEasyCommand  extends EnvironmentCommand<DdVerifyFileMigrationConfiguration> {

    private static final Logger log = LoggerFactory.getLogger(LoadFromEasyCommand.class);
    private final HibernateBundle<DdVerifyFileMigrationConfiguration> hibernate;

    /**
     * Creates a new environment command.
     *
     * @param application the application providing this command
     */
    public LoadFromEasyCommand(Application<DdVerifyFileMigrationConfiguration> application, HibernateBundle<DdVerifyFileMigrationConfiguration> hibernate) {
        super(application, "load-from-easy", "Load expected table with info from easy_files in fs-rdb and transformation rules");
        this.hibernate = hibernate;
    }

    @Override
    protected void run(Environment environment, Namespace namespace, DdVerifyFileMigrationConfiguration configuration) throws Exception {
        EasyFileDAO easyFileDAO = new EasyFileDAO(hibernate.getSessionFactory());
        // TODO read IDs as in https://github.com/DANS-KNAW/dd-manage-prestaging/blob/5fb6bd9e163ada89a99ed1342b4454c065528848/src/main/java/nl/knaw/dans/prestaging/cli/LoadFromDataverseCommand.java#L41
        for (EasyFile ef : easyFileDAO.findByDatasetId("easy-dataset:17")) {
            log.trace("ef = {}" , ef);
            // TODO apply transformation rules and add to Expected table
        }
    }
}
