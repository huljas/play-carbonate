package com.carbonfive.db.migration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author huljas
 */
public interface ConnectionCallback<T> {
    T doInConnection(Connection connection) throws SQLException;
}
