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

// (params) -p=/home/sbartkowski/work/ExportDB/src/test/testres/export.properties -t=DB2INST1.MORTGAGE_CUSTOMER -d /tmp/d


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

import com.export.db2.main.util.ArgOptions;
import com.export.db2.main.util.OptNames;
import com.export.db2.main.util.TableToCSV;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public class ExportMain {

	private static Logger log = Logger.getLogger(ExportMain.class.getName());

	public static void main(String[] args) {
		Optional<CommandLine> pars = ArgOptions.runOpt(args, (Options options) -> {
			ArgOptions.addProp(options);
			ArgOptions.addDir(options);
			ArgOptions.addTable(options);
		}, "Export the table as CSV file");

		if (!pars.isPresent()) System.exit(4);
		String prop = ArgOptions.getProp(pars.get());
		String outdir = pars.get().getOptionValue(OptNames.DIR);
		String table = pars.get().getOptionValue(OptNames.TABLE);
		log.info("Extract table " + table);
		RunMain.doMain(prop, new RunMain.RunTask() {

			@Override
			public void doTask(Connection con, Properties prop) throws SQLException, IOException {
				TableToCSV.exportTable(con, prop, table, outdir);
			}
		});
	}

}
