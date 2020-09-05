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

import java.sql.Types;
import java.util.Properties;

import com.export.db2.main.ExportProp;

class SQLUtil {

	static boolean isChar(int typeC) {
		return typeC == Types.CHAR || typeC == Types.NCHAR;
	}

	static boolean isVarChar(int typeC) {
		return typeC == Types.VARCHAR || typeC == Types.NVARCHAR;
	}

	static boolean isTinyInt(int typeC) {
		return typeC == Types.TINYINT || typeC == Types.BIT;
	}

	static boolean isSmallInt(int typeC) {
		return typeC == Types.SMALLINT || typeC == Types.TIME || typeC == Types.TIME_WITH_TIMEZONE;
	}

	static boolean isInt(int typeC) {
		return typeC == Types.INTEGER;
	}

	static boolean isBigInt(int typeC) {
		return typeC == Types.BIGINT;
	}

	static boolean isBoolean(int typeC) {
		return typeC == Types.BOOLEAN;
	}

	static boolean isFloat(int typeC) {
		return typeC == Types.FLOAT || typeC == Types.REAL || typeC == Types.NUMERIC;
	}

	static boolean isDecimal(int typeC) {
		return typeC == Types.DECIMAL;
	}

	static boolean isDouble(int typeC) {
		return typeC == Types.DOUBLE;
	}

	static boolean isDate(int typeC) {
		return typeC == Types.DATE;
	}

	static boolean isDateTime(int typeC) {
		return typeC == Types.TIMESTAMP || typeC == Types.TIMESTAMP_WITH_TIMEZONE;
	}

	static boolean isHiveType(int typeC) {
		return isChar(typeC) || isVarChar(typeC) || isTinyInt(typeC) || isSmallInt(typeC) || isInt(typeC)
				|| isBigInt(typeC) || isBoolean(typeC) || isFloat(typeC) || isDouble(typeC) || isDate(typeC)
				|| isDateTime(typeC) || isDecimal(typeC);
	}

	static class OutputFileName {
		String schemaName;
		String tableName;
		String exporttableName;
	}

	static OutputFileName tranformFileName(Properties prop, String fulltableName) {

		OutputFileName out = new OutputFileName();
		String[] t = fulltableName.split("[.]");
		out.schemaName = null;
		out.tableName = t[0].trim();
		if (t.length == 2) {
			out.schemaName = out.tableName;
			out.tableName = t[1].trim();
		}
		out.exporttableName = fulltableName;
		// determine the table name
		if (ExportProperties.isHiveDest(prop))
			// only if hive dest
			if (ExportProperties.getHiveDB(prop) != null)
				if (ExportProperties.getHiveDB(prop).equals(ExportProp.HIVENODB))
					out.exporttableName = out.tableName;
				else
					out.exporttableName = ExportProperties.getHiveDB(prop) + "." + out.tableName;
		return out;
	}

}
