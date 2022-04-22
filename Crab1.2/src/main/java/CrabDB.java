/*
 ***LICENSE***
Copyright 2022 https://github.com/l33pf
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **/

/** @About
 *  CrabDB is the database class for use within Crab, this class
 *  contains method for database connectivity, inserting and selecting
 *  data from a user's given database.
 */

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.*;
import java.sql.*;

public class CrabDB {

    private static String uname = null;
    private static String pwrd = null;

    CrabDB(final String uName, final String pWrd){
            uname = uName;
            pwrd = pWrd;
    }

    private static DataSource initDataSource(final String jbdc_str){
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(jbdc_str);
        ds.setUsername(uname);
        ds.setPassword(pwrd);
        return ds;
    }


}