package com.carbonfive.db.migration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author huljas
 */
public class JdbcTemplate {
    private DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Object execute(ConnectionCallback connectionCallback) throws MigrationException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            return connectionCallback.doInConnection(connection);
        } catch (Exception e) {
            throw new MigrationException(e);
        } finally {
            try {
                connection.close();
            } catch (Exception e) {

            }
        }
    }
}
