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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

//CREATE TABLE user(id INT, name STRING) ROW FORMAT
//DELIMITED FIELDS TERMINATED BY ','
//LINES TERMINATED BY '\n' STORED AS TEXTFILE;

public class CreateHiveTable {

	private static Logger log = Logger.getLogger(CreateHiveTable.class.getName());

	private final static int MAXVARCHAR = 65535;

	public static void create(Connection con, Properties prop, String fulltableName, String outputFile)
			throws IOException, SQLException {
		DatabaseMetaData mData = con.getMetaData();
		SQLUtil.OutputFileName outp = SQLUtil.tranformFileName(prop, fulltableName);
		log.severe(outp.exporttableName);
		try (ResultSet res = mData.getColumns(null, outp.schemaName, outp.tableName, null);
				OutputTextFile out = new OutputTextFile()) {
			out.open(new File(outputFile), true);
			String SEPARATOR = ExportProperties.getSeparator(prop);
			out.writeline("CREATE TABLE " +outp.exporttableName + "(");
			boolean first = true;
			while (res.next()) {
				int typeC = res.getInt("DATA_TYPE");
				String columnName = res.getString("COLUMN_NAME");
				int decFDigits = res.getInt("DECIMAL_DIGITS");
				int colSize = res.getInt("COLUMN_SIZE");
				if (colSize > MAXVARCHAR) {
					log.warning(columnName + "truncated from " + colSize + " to " + MAXVARCHAR);
					colSize = MAXVARCHAR;
				}
				String typeS = null;
				if (SQLUtil.isChar(typeC))
					typeS = "char(" + colSize + ")";
				if (SQLUtil.isVarChar(typeC))
					typeS = "varchar(" + colSize + ")";
				if (SQLUtil.isInt(typeC))
					typeS = "int";
				if (SQLUtil.isTinyInt(typeC))
					typeS = "tinyint";
				if (SQLUtil.isSmallInt(typeC))
					typeS = "smallint";
				if (SQLUtil.isDecimal(typeC))
					if (decFDigits > 0)
						typeS = "decimal(" + colSize + "," + decFDigits + ")";
					else
						typeS = "decimal(" + colSize + ")";
				if (SQLUtil.isBoolean(typeC))
					typeS = "boolean";
				if (SQLUtil.isFloat(typeC))
					typeS = "float";
				if (SQLUtil.isDouble(typeC))
					typeS = "double";
				if (SQLUtil.isDate(typeC))
					typeS = "date";
				if (SQLUtil.isDateTime(typeC))
					typeS = "timestamp";
				if (typeS == null) {
					log.warning(columnName + " : " + res.getString("TYPE_NAME") + " ignored");
					continue;
				}
				String line = "  ";
				if (!first)
					line = ", ";
				if (columnName.toUpperCase().equals("GROUP"))
					columnName = "groupp";
				line += columnName + " " + typeS;
				out.writeline(line);
				first = false;
			}
			if (first)
				throw new IOException("Cannot find table " + outp.tableName + " or cannot find any columns for Hive");
			out.writeline(")");
			out.writeline("ROW FORMAT DELIMITED FIELDS TERMINATED BY '" + SEPARATOR + "'");
			out.writeline("LINES TERMINATED BY '\\n' STORED AS TEXTFILE;");
			out.writeline();
		} // try, covers closing resources as well

	}

}
