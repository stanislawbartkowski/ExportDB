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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class NumberOfRecords {

	private static Logger log = Logger.getLogger(NumberOfRecords.class.getName());

	private NumberOfRecords() {

	}

	static private void numberTable(Connection con, String tableName, Map<String,Long> numb) throws SQLException {
		try (ResultSet res = con.prepareStatement("SELECT COUNT(*) FROM " + tableName).executeQuery()) {
			res.next();
			int num = res.getInt(1);
			numb.put(tableName,Integer.toUnsignedLong(num));
		}
	}

	static private void createNumbers(Connection con, String listOfTables, Map<String,Long> numb)
			throws IOException, SQLException {
		try (Scanner s = new Scanner(new File(listOfTables))) {
			while (s.hasNext()) {
				String tableName = s.next();
				if (!tableName.isEmpty()) numberTable(con, tableName, numb);
			}
		}
	}

	static public void compare(String url1, Connection con1, String url2, Connection con2,String listOfTables) throws IOException, SQLException {
		log.info("Compare number of records " + url1 + "  " + url2);
		Map<String,Long> map1 = new HashMap<String,Long>();
		Map<String,Long> map2 = new HashMap<String,Long>();
		createNumbers(con1,listOfTables,map1);
		createNumbers(con2,listOfTables,map2);
		Iterator<Map.Entry<String,Long>> i = map1.entrySet().iterator();
		while(i.hasNext()) {
			Map.Entry<String,Long> e = i.next();
			Long destl = map2.get(e.getKey());
			log.info(e.getKey() + " " + e.getValue() + " - " + destl);
			if (!e.getValue().equals(destl)) {
				String msg = "Number of records does not match";
				log.severe(msg);
				throw new IOException(msg);
			}
		}
	}

}
