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

package com.export.db2.main.util;

import org.apache.commons.cli.*;

import java.util.Optional;
import java.util.function.Consumer;

public class ArgOptions {

    public static void addProp(Options options) {
        options.addOption(Option.builder(OptNames.PROP)
                .longOpt("prop")
                .required(true)
                .hasArg(true)
                .desc("Property file")
                .build());

    }

    public static void addOutput(Options options) {
        options.addOption(Option.builder(OptNames.OUTFILE)
                .longOpt("out")
                .required(true)
                .hasArg(true)
                .desc("Output file")
                .build());
    }

    public static void addDir(Options options) {
        options.addOption(Option.builder(OptNames.DIR)
                .longOpt("dir")
                .required(true)
                .hasArg(true)
                .desc("Directory name")
                .build());
    }

    public static void addSchema(Options options) {
        options.addOption(Option.builder(OptNames.SCHEMA)
                .longOpt("schema")
                .required(true)
                .hasArg(true)
                .desc("Schema name")
                .build());
    }

    public static void addTable(Options options) {
        options.addOption(Option.builder(OptNames.TABLE)
                .longOpt("table")
                .required(true)
                .hasArg(true)
                .desc("Table name")
                .build());
    }

    public static void addListOfTables(Options options) {
        options.addOption(Option.builder(OptNames.LIST)
                .longOpt("list")
                .required(true)
                .hasArg(true)
                .desc("File with list of tables")
                .build());
    }

    public static Optional<CommandLine> runOpt(String[] args, Consumer<Options> addPar, String helptitle) {

        final Options options = new Options();
        addPar.accept(options);

        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );
            return Optional.of(line);
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( helptitle, options );
            return Optional.empty();
        }
    }

    public static String getProp(CommandLine c) {
        return c.getOptionValue(OptNames.PROP);
    }

    public static String getOuput(CommandLine c) {
        return c.getOptionValue(OptNames.OUTFILE);
    }

}
