/*
 * Copyright 2020 stanislawbartkowski@gmail.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.export.db2.main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

import com.export.db2.main.util.ArgOptions;
import com.export.db2.main.util.ExtractSchemas;

import com.export.db2.main.util.OptNames;
import org.apache.commons.cli.*;

// (params) -p=/home/sbartkowski/work/ExportDB/src/test/testres/export.properties -s=/tmp/schemas

public class ExportSchemas {

    private static Logger log = Logger.getLogger(ExportSchemas.class.getName());

    public static void main(String[] args) {

        Optional<CommandLine> pars = ArgOptions.runOpt(args, (Options options) -> {
            ArgOptions.addProp(options);
            ArgOptions.addOutput(options);
        }, "Extract schemas");

        if (!pars.isPresent()) System.exit(4);
        String prop = ArgOptions.getProp(pars.get());
        String sche = ArgOptions.getOuput(pars.get());

        log.info("Extract schemas to file " + sche);
        RunMain.doMain(prop, new RunMain.RunTask() {

            @Override
            public void doTask(Connection con, Properties prop) throws SQLException, IOException {
                ExtractSchemas.exportList(con, prop, sche);
            }
        });
    }

}
