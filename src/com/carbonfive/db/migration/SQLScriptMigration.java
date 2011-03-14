package com.carbonfive.db.migration;

import com.carbonfive.jdbc.DatabaseType;
import com.carbonfive.jdbc.ScriptRunner;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

public class SQLScriptMigration extends AbstractMigration
{
    private final Resource script;

    public SQLScriptMigration(String version, Resource script)
    {
        super(version, script.getFilename());
        this.script = script;
    }

    public void migrate(DatabaseType dbType, Connection connection)
    {
        InputStream inputStream = null;
        try
        {
            inputStream = script.getInputStream();
            ScriptRunner scriptRunner = new ScriptRunner(dbType);
            scriptRunner.execute(connection, new InputStreamReader(inputStream, "UTF-8"));
            Validate.isTrue(!connection.isClosed(), "JDBC Connection should not be closed.");
        }
        catch (IOException e)
        {
            throw new MigrationException("Error while reading script input stream.", e);
        }
        catch (SQLException e)
        {
            throw new MigrationException("Error while executing migration script.", e);
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
