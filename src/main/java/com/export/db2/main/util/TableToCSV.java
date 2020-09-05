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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import java.util.logging.Logger;

import com.export.db2.main.ExportProp;

public class TableToCSV {

	private static Logger log = Logger.getLogger(TableToCSV.class.getName());

	private static void clearOutput(File f, File bdir) throws IOException {

		if (f.exists()) {
			log.info("Remove existing " + f.getAbsolutePath());
			f.delete();
		}

		if (bdir.isFile())
			throw new IOException(bdir.getAbsolutePath() + " not a directory");
		if (bdir.exists()) {
			File[] files = bdir.listFiles();
			for (File fl : files)
				fl.delete();
			bdir.delete();
		}
	}

	private static boolean isString(int typeC) {
		return (typeC == Types.CHAR || typeC == Types.NCHAR || typeC == Types.NVARCHAR || typeC == Types.VARCHAR);
	}

	private static boolean isToString(int typeC) {
		return (typeC == Types.BIGINT || typeC == Types.BIT || typeC == Types.BOOLEAN || typeC == Types.DATE
				|| typeC == Types.DECIMAL || typeC == Types.DOUBLE || typeC == Types.FLOAT || typeC == Types.INTEGER
				|| typeC == Types.NUMERIC || typeC == Types.REAL || typeC == Types.TIME || typeC == Types.SMALLINT
				|| typeC == Types.TIME_WITH_TIMEZONE || typeC == Types.TIMESTAMP
				|| typeC == Types.TIMESTAMP_WITH_TIMEZONE || typeC == Types.TINYINT);
	}

	private static boolean isBLOB(int typeC) {
		return typeC == Types.BLOB;
	}

	private static boolean isCLOB(int typeC) {
		return typeC == Types.CLOB;
	}

	private static String retrieveBlob(InputStream blo, File bdir, String columnName, int rowno) throws IOException {
		String filename = columnName + "-" + rowno + ".lob";
		File blobFile = new File(bdir, filename);
		try (FileOutputStream writer = new FileOutputStream(blobFile)) {
			byte[] buffer = new byte[1024];
			int bread = blo.read(buffer);
			boolean empty = true;
			while (bread > 0) {
				empty = false;
				for (int i = 0; i < bread; i++)
					writer.write(buffer[i]);
				bread = blo.read(buffer);
			}
			if (empty)
				return null;
		}
		return filename;
	}

	public static void exportTable(Connection con, Properties prop, String tableName, String outputDir)
			throws SQLException, IOException {

		String SEPARATOR = ExportProperties.getSeparator(prop);
		String QUOTECHAR = ExportProperties.getQuoteChar(prop);
		String ESCAPECHAR = ExportProperties.getEscapeChar(prop);

		SQLUtil.OutputFileName pout = SQLUtil.tranformFileName(prop, tableName);
		String filenamebase = pout.exporttableName.toLowerCase().replace('.', '_');
		
		// stored and closed

		File f = new File(outputDir, filenamebase + ".txt");
		if (! new File(outputDir).exists() || ! new File(outputDir).isDirectory()) {
			String mess = "Directory " + outputDir + " does not exist or is not directory";
			throw new SQLException(mess);
		}
		File bdir = new File(outputDir, filenamebase);
		clearOutput(f, bdir);
		boolean blobDirCreated = false;
		try (ResultSet res = con.prepareStatement("SELECT * FROM " + tableName).executeQuery();
				OutputTextFile out = new OutputTextFile()) {
			out.open(f, false);
			// FileWriter writer = new FileWriter(f);
			ResultSetMetaData resData = res.getMetaData();
			int no = 0;
			int blobno = 0;
			while (res.next()) {
				no++;
				StringBuilder b = new StringBuilder();
				// columns starting from ! so <= limitation
				for (int i = 1; i <= resData.getColumnCount(); i++) {
					int typeC = resData.getColumnType(i);
					// ignore columns not recognized by Hive
					if (ExportProperties.isHiveDest(prop) && !SQLUtil.isHiveType(typeC))
						continue;
					String valS = null;
					boolean addQuote = false;
					InputStream blobS = null;
					if (isString(typeC)) {
						addQuote = true;
						valS = res.getString(i);
						if (valS != null) {
							valS = valS.replace(QUOTECHAR, ESCAPECHAR + QUOTECHAR);
							// remove LF
							valS = valS.replace("\n", "");
						}
					} else if (isToString(typeC))
						valS = res.getString(i);
					else if (isBLOB(typeC) && !ExportProperties.isHiveDest(prop)) {
						Blob blo = res.getBlob(i);
						if (blo != null && !res.wasNull() && blo.length() != 0)
							blobS = blo.getBinaryStream();
					} else if (isCLOB(typeC) && !ExportProperties.isHiveDest(prop)) {
						Clob clo = res.getClob(i);
						if (clo != null && !res.wasNull() && clo.length() != 0)
							blobS = clo.getAsciiStream();
					} else
						valS = null;
					if (blobS != null) {
						if (!blobDirCreated)
							bdir.mkdirs();
						blobDirCreated = true;
						valS = retrieveBlob(blobS, bdir, resData.getColumnName(i), no);
						if (valS != null) {
							blobno++;
							if (blobno % 100 == 0)
								log.info("lobs: " + blobno);
						}
					}

					if (valS != null && res.wasNull())
						valS = null;
					// else all others ignore
					if (i > 1)
						b.append(SEPARATOR);
					// Hive, do not add string quotes
					if (ExportProperties.isHiveDest(prop))
						addQuote = false;
					if (valS != null) {
						if (addQuote)
							b.append(QUOTECHAR);
						b.append(valS);
						if (addQuote)
							b.append(QUOTECHAR);
					}
				} // for
				if (no % 1000 == 0)
					log.info("rows: " + no);
				// writer.write(b.toString());
				// writer.append('\n');
				out.writeline(b.toString());
			} // while
			res.getStatement().close(); // very important, close cursor !!
			log.info(no + " rows exported, " + blobno + " lobs exported.");
		}
	}

}
