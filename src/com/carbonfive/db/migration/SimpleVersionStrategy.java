package com.carbonfive.db.migration;

import com.carbonfive.jdbc.DatabaseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.carbonfive.jdbc.DatabaseType.HSQL;
import static com.carbonfive.jdbc.DatabaseType.SQL_SERVER;
import static java.lang.String.format;

/**
 * A trivial VersionStrategy which tracks only the minimal information required to note the current state of the database: the current version.
 */
public class SimpleVersionStrategy implements VersionStrategy
{
    public static final String DEFAULT_VERSION_TABLE = "schema_version";
    public static final String DEFAULT_VERSION_COLUMN = "version";
    public static final String DEFAULT_APPLIED_DATE_COLUMN = "applied_on";
    public static final String DEFAULT_DURATION_COLUMN = "duration";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String versionTable = DEFAULT_VERSION_TABLE;
    private String versionColumn = DEFAULT_VERSION_COLUMN;
    private String appliedDateColumn = DEFAULT_APPLIED_DATE_COLUMN;
    private String durationColumn = DEFAULT_DURATION_COLUMN;

    private static final DefaultedMap enableVersioningDDL;

    static
    {
        enableVersioningDDL = new DefaultedMap("create table %s (%s varchar(32) not null unique, %s timestamp not null, %s int not null)");
        enableVersioningDDL.put(HSQL, "create table %s (%s varchar not null, %s datetime not null, %s int not null, constraint %2$s_unique unique (%2$s))");
        enableVersioningDDL.put(SQL_SERVER, "create table %s (%s varchar(32) not null unique, %s datetime not null, %s int not null)");
    }

    public boolean isEnabled(DatabaseType dbType, Connection connection)
    {
        try
        {
            connection.createStatement().executeQuery("select count(*) from " + versionTable);
        }
        catch (SQLException e)
        {
            return false;
        }
        
        return true;
    }

    public void enableVersioning(DatabaseType dbType, Connection connection)
    {
        try
        {
            String ddl = format((String) enableVersioningDDL.get(dbType), versionTable, versionColumn, appliedDateColumn, durationColumn);
            connection.createStatement().executeUpdate(ddl);
        }
        catch (SQLException e)
        {
            throw new MigrationException("Could not create version-tracking table '" + versionTable + "'.", e);
        }
    }

    public Set<String> appliedMigrations(DatabaseType dbType, Connection connection)
    {
        // Make sure migrations is enabled.
        if (!isEnabled(dbType, connection))
        {
            return null;
        }

        Set<String> migrations = new HashSet<String>();

        try
        {
            ResultSet rs = connection.createStatement().executeQuery("select " + versionColumn + " from " + versionTable);
            while (rs.next())
            {
                migrations.add(rs.getString(versionColumn));
            }
        }
        catch (SQLException e)
        {
            throw new MigrationException(e);
        }

        return migrations;
    }

    public void recordMigration(DatabaseType dbType, Connection connection, String version, Date startTime, long duration)
    {
        try
        {
            PreparedStatement statement = connection.prepareStatement("insert into " + versionTable + " values (?, ?, ?)");
            statement.setString(1, version);
            statement.setTimestamp(2, new Timestamp(startTime.getTime()));
            statement.setLong(3, duration);
            statement.execute();
        }
        catch (SQLException e)
        {
            throw new MigrationException(e);
        }
    }

    public void setVersionTable(String versionTable)
    {
        this.versionTable = versionTable;
    }

    public void setVersionColumn(String versionColumn)
    {
        this.versionColumn = versionColumn;
    }

    public void setAppliedDateColumn(String appliedDateColumn)
    {
        this.appliedDateColumn = appliedDateColumn;
    }

    public void setDurationColumn(String durationColumn)
    {
        this.durationColumn = durationColumn;
    }
}
