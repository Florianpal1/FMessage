
/*
 * Copyright (C) 2022 Florianpal
 *
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 * Last modification : 20/10/2021 19:57
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.florianpal.fmessage.IDatabaseTable;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {
    private final HikariDataSource ds;
    private final ConfigurationManager configurationManager;
    private final ArrayList<IDatabaseTable> repositories = new ArrayList<>();
    public DatabaseManager(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(  configurationManager.getDatabase().getUrl() );
        config.setUsername( configurationManager.getDatabase().getUser() );
        config.setPassword(  configurationManager.getDatabase().getPassword() );
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        ds = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void addRepository(IDatabaseTable repository) {
        repositories.add(repository);
    }

    public void initializeTables() {
        try (Connection connection = getConnection()) {
            for (IDatabaseTable repository : repositories) {
                String[] tableInformation = repository.getTable();

                if (!tableExists(tableInformation[0])) {
                    try {
                        Statement statement = connection.createStatement();
                        statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + tableInformation[0] + "` (" + tableInformation[1] + ") " + tableInformation[2] + ";");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean tableExists(String tableName) throws SQLException {
        Connection connection = getConnection();
        DatabaseMetaData dbm = connection.getMetaData();
        ResultSet tables = dbm.getTables(null, null, tableName, null);
        return tables.next();
    }
}
