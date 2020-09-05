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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

import com.export.db2.main.util.CreateHiveTable;

public class ExportListCreateHive {

	private static Logger log = Logger.getLogger(ExportListCreateHive.class.getName());
	
	private static void P(String s) {
		System.out.println(s);
	}

	private static void printHelp() {
		P("Parameters:");
		P(" <property file> <file with tables> <output file>  ");
		P("Example: ");
		P(" /home/sb/export/prop.properties table.list file.hive");
	}

	public static void main(String[] args) {
		if (args.length != 3) {
			P("Incorrect parameters.");
			printHelp();
			System.exit(10);
		}
		log.info("Extract tables from list " + args[1] + " and create Hive tables in " + args[2]);
		RunMain.doMain(args[0], new RunMain.RunTask() {

			@Override
			public void doTask(Connection con, Properties prop) throws SQLException, IOException {
				Scanner s = new Scanner(new File(args[1]));
				while (s.hasNext()) {
					String table = s.next();
					CreateHiveTable.create(con, prop, table, args[2]);
				}
				s.close();

			}
		});
	}

}
