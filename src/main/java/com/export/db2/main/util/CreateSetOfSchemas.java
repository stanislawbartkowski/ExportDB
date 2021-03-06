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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class CreateSetOfSchemas {

	public static Set<String> create(Connection con) throws SQLException {
		Set<String> sche = new HashSet<String>();
		DatabaseMetaData mData = con.getMetaData();

		try (ResultSet res = mData.getSchemas()) {

			while (res.next())
				sche.add(res.getString("TABLE_SCHEM"));
		}
		return sche;
	}

}
