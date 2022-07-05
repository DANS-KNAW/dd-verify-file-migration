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
package nl.knaw.dans.migration.core;

import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.Namespace;

public enum Mode {
    BOTH, FILES, DATASETS, INPUT;

    public boolean doDatasets() {
        return Mode.DATASETS.equals(this) || Mode.BOTH.equals(this);
    }
    public boolean doFiles() {
        return Mode.FILES.equals(this) || Mode.BOTH.equals(this);
    }

    public static Argument configure(Argument argument) {
        return argument
            .dest("mode")
            .setDefault(Mode.DATASETS)
            .type(Mode.class)
            .help("files require more writing, datasets require more reading, BOTH=FILES+DATASETS");
    }

    public static Mode from(Namespace namespace) {
        return Mode.valueOf(namespace.getString("mode"));
    }
}
