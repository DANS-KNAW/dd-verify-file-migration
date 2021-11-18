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
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.knaw.dans.filemigration.DdVerifyFileMigrationConfiguration;

public class LoadFromEasyCommand  extends EnvironmentCommand<DdVerifyFileMigrationConfiguration> {


    /**
     * Creates a new environment command.
     *
     * @param application the application providing this command
     * @param name        the name of the command, used for command line invocation
     * @param description a description of the command's purpose
     */
    protected LoadFromEasyCommand(Application<DdVerifyFileMigrationConfiguration> application, String name, String description) {
        super(application, name, description);
    }

    @Override
    protected void run(Environment environment, Namespace namespace, DdVerifyFileMigrationConfiguration configuration) throws Exception {

    }
}
