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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.export.db2.main.util.ExportProperties;

class RunMain {

    private static Logger log = Logger.getLogger(RunMain.class.getName());

    interface RunTask {
        void doTask(Connection con, Properties prop) throws SQLException, IOException;
    }

    static void doMain(String propfileName, RunTask task) {
        Properties prop = null;
        try {
            prop = ExportProperties.readProp(propfileName);
        } catch (IOException e1) {
            log.log(Level.SEVERE, "Cannot load property file", e1);
            System.exit(10);
        }
        try (Connection conn = JDBCConnection.getConnection(prop)) {
            task.doTask(conn, prop);
        } catch (SQLException | IOException e) {
            log.log(Level.SEVERE, "Error while connecting or executing query", e);
            System.exit(10);
        }

    }

}
