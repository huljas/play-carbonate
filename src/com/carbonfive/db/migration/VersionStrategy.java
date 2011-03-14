package com.carbonfive.db.migration;

import com.carbonfive.jdbc.DatabaseType;

import java.sql.Connection;
import java.util.Date;
import java.util.Set;

/**
 * Abstracts all things related to querying about and recording changes to the current state (i.e. version) of the database.
 * <p/>
 * Connections passed into any instance of a <code>VersionStrategy</code> should not be closed under any circumstances.
 */
public interface VersionStrategy
{
    /**
     * Detects whether or not the database is already migrations enabled (i.e. is there a version tracking table).
     *
     * @param dbType the database type (e.g. mysql, postgresql)
     * @param connection an open connection to the database (which should not be closed)
     * @return true if the database is already migrations enabled, false otherwise
     */
    boolean isEnabled(DatabaseType dbType, Connection connection);

    /**
     * Prepares a virgin database to be versioned using migrations, creating the necessary table(s) and initializing them to the version 0 state.
     *
     * @param dbType the database type (e.g. mysql, postgresql)
     * @param connection an open connection to the database (which should not be closed)
     */
    void enableVersioning(DatabaseType dbType, Connection connection);

    /**
     * Queries the underlying database and returns a set of all migration versions that have already been applied.
     *
     * @param dbType the database type (e.g. mysql, postgresql)
     * @param connection an open connection to the database (which should not be closed)
     * @return a set of migration versions that have already been applied;
     *         an empty set if none have been applied; null if this database has not yet be enabled for migrations.
     */
    Set<String> appliedMigrations(DatabaseType dbType, Connection connection);

    /**
     * Captures in the database that the migration to the specified version has happened.
     *
     * @param dbType the database type (e.g. mysql, postgresql)
     * @param connection an open connection to the database (which should not be closed)
     * @param version    the version of the most recently completed migration (i.e. the now-current version of the database)
     * @param startTime  when the migration was started
     * @param duration   how long in milliseconds the migration took
     */
    void recordMigration(DatabaseType dbType, Connection connection, String version, Date startTime, long duration);
}
