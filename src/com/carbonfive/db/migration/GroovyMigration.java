package com.carbonfive.db.migration;

import com.carbonfive.jdbc.DatabaseType;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class GroovyMigration extends AbstractMigration
{
    private final Resource script;

    public GroovyMigration(String version, Resource script)
    {
        super(version, script.getFilename());
        this.script = script;
    }

    public void migrate(DatabaseType dbType, Connection connection)
    {
        Binding binding = new Binding();
        binding.setVariable("connection", connection);
        GroovyShell shell = new GroovyShell(binding);

        InputStream inputStream = null;
        try
        {
            inputStream = script.getInputStream();
            shell.evaluate(IOUtils.toString(inputStream));
            Validate.isTrue(!connection.isClosed(), "JDBC Connection should not be closed.");
        }
        catch (IOException e)
        {
            throw new MigrationException(e);
        }
        catch (SQLException e)
        {
            throw new MigrationException(e);
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
