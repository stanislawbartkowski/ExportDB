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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import com.export.db2.main.ExportProp;

public class ExportProperties {

    private static Logger log = Logger.getLogger(ExportProperties.class.getName());

    public static Properties readProp(String fileName) throws FileNotFoundException, IOException {
        log.info("Read properties from file " + fileName);
        Properties prop = new Properties();
        prop.load(new FileInputStream(new File(fileName)));
        validate(prop);
        return prop;
    }

    private static void checkProp(Properties prop, String key) throws IOException {
        String vals = prop.getProperty(key);
        if (vals == null)
            throw new IOException("Property file does not contain " + key + " property");
        if (vals.trim().equals(""))
            throw new IOException("Property " + key + " is empty");
    }

    private static void validate(Properties prop) throws IOException {
        checkProp(prop, ExportProp.PARAMSOURCEURL);
        checkProp(prop, ExportProp.PARAMUSER);
        checkProp(prop, ExportProp.PARAMPASSWORD);
        String[] pList = {ExportProp.PARAMSOURCEURL, ExportProp.PARAMSEPARATOR, ExportProp.PARAMQUOTECHAR,
                ExportProp.PARAMESCAPECHAR, ExportProp.PARAMSOURCEDB, ExportProp.PARAMDESTDB, ExportProp.PARAMSOURCEURL,
                ExportProp.PARAMUSER, ExportProp.PARAMPASSWORD, ExportProp.HIVEDBNAME, ExportProp.PARAMDESTURL,
                ExportProp.PARAMDESTUSER, ExportProp.PARAMDESTPASSWORD};
        prop.keySet().stream().forEach(key -> {
            boolean found = false;
            for (String k : pList)
                if (k.equals(key)) {
                    found = true;
                    break;
                }
            if (!found) {
                String pS = null;
                for (String v : pList)
                    if (pS == null)
                        pS = v;
                    else
                        pS += "," + v;
                throw new RuntimeException(key + " not valid. Expected " + pS);
            }
        });
    }

    static boolean isOracle(Properties prop) {
        String dbtype = prop.getProperty(ExportProp.PARAMSOURCEDB, ExportProp.ORACLEDB);
        return ExportProp.ORACLEDB.equals(dbtype);
    }

    static boolean isHiveDest(Properties prop) {
        String dbtype = prop.getProperty(ExportProp.PARAMDESTDB);
        if (dbtype == null)
            return false;
        return ExportProp.HIVEDB.equals(dbtype);
    }

    static String getSeparator(Properties prop) {
        return prop.getProperty(ExportProp.PARAMSEPARATOR, ExportProp.DEFASEPARATOR);
    }

    static String getQuoteChar(Properties prop) {
        return prop.getProperty(ExportProp.PARAMQUOTECHAR, ExportProp.DEFAQUOTECHAR);
    }

    static String getEscapeChar(Properties prop) {
        return prop.getProperty(ExportProp.PARAMESCAPECHAR, ExportProp.DEFAESCAPECHAR);
    }

    static String getHiveDB(Properties prop) {
        return prop.getProperty(ExportProp.HIVEDBNAME);
    }

}
