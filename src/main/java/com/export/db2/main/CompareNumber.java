package com.export.db2.main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

import com.export.db2.main.util.ArgOptions;
import com.export.db2.main.util.NumberOfRecords;
import com.export.db2.main.util.OptNames;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public class CompareNumber {

	private static Logger log = Logger.getLogger(CompareNumber.class.getName());


	public static void main(String[] args) {

		Optional<CommandLine> pars = ArgOptions.runOpt(args, (Options options) -> {
			ArgOptions.addProp(options);
			ArgOptions.addListOfTables(options);

		}, "Compare number of records");

		if (!pars.isPresent()) System.exit(4);
		String prop = ArgOptions.getProp(pars.get());
		String listoftables = pars.get().getOptionValue(OptNames.LIST);
		log.info("Compare number of records " + listoftables);

		RunMain.doMain(prop, new RunMain.RunTask() {

			@Override
			public void doTask(Connection con, Properties prop) throws SQLException, IOException {
				Connection dest = JDBCConnection.getDestConnection(prop);
				NumberOfRecords.compare(
						prop.getProperty(ExportProp.PARAMSOURCEURL),con,
						prop.getProperty(ExportProp.PARAMDESTURL),dest,
						listoftables);
			}
		});

	}

}
