package com.carbonfive.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.sql.*;

import static org.apache.commons.lang.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang.StringUtils.startsWithIgnoreCase;

public class ScriptRunner
{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String DEFAULT_DELIMITER = ";";
    private static final String DEFAULT_DELIMITER_KEYWORD = "DELIMITER";
    
    final private DatabaseType dbType;

    public ScriptRunner(DatabaseType dbType)
    {
        this.dbType = dbType;
    }

    public void execute(Connection connection, Reader reader) throws IOException, SQLException
    {
        try
        {
            final boolean originalAutoCommit = connection.getAutoCommit();
            try
            {
                if (originalAutoCommit)
                {
                    connection.setAutoCommit(false);
                }
                doExecute(connection, reader);
            }
            finally
            {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (SQLException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error running script.  Cause: " + e, e);
        }
    }

    private void doExecute(Connection connection, Reader reader) throws IOException, SQLException
    {
        StringBuffer command = null;
        try
        {
            String delimiter = DEFAULT_DELIMITER;
            LineNumberReader lineReader = new LineNumberReader(reader);
            String line;
            while ((line = lineReader.readLine()) != null)
            {
                if (command == null)
                {
                    command = new StringBuffer();
                }

                line = line.trim(); // Strip extra whitespace too?

                if (line.length() < 1)
                {
                    // Do nothing, it's an empty line.
                }
                else if (line.startsWith("--") || line.startsWith("#") || line.startsWith("//"))
                {
                    logger.debug(line);
                }
                else
                {
                    if (startsWithIgnoreCase(line, DEFAULT_DELIMITER_KEYWORD))
                    {
                        delimiter = line.substring(10).trim();
                    }
                    else if ((command.length() == 0) && startsWithIgnoreCase(line, "create ") && containsIgnoreCase(line, " as "))
                    {
                        delimiter = line.substring(line.toLowerCase().lastIndexOf(" as ") + 4);
                        command.append(line);
                        command.append(" ");
                    }
                    else if (line.contains(delimiter))
                    {
                        if (line.startsWith(delimiter))
                        {
                            delimiter = DEFAULT_DELIMITER;
                        }

                        if (line.endsWith(delimiter))
                        {
                            command.append(line.substring(0, line.lastIndexOf(delimiter)));
                            executeStatement(connection, command.toString());
                            command = null;
                        }
                    }
                    else
                    {
                        command.append(line);
                        command.append(" ");
                    }
                }
            }

            // Check to see if we have an unexecuted statement in command.
            if (command != null && command.length() > 0)
            {
                logger.info("Last statement in script is missing a terminating delimiter, executing anyway.");
                executeStatement(connection, command.toString());
            }
        }
        catch (SQLException e)
        {
            e.fillInStackTrace();
            logger.error("Error executing: " + command, e);
            throw e;
        }
        catch (IOException e)
        {
            e.fillInStackTrace();
            logger.error("Error executing: " + command, e);
            throw e;
        }
    }

    private void executeStatement(Connection conn, String command) throws SQLException
    {
        Statement statement = conn.createStatement();

        logger.debug(command);

        boolean hasResults = statement.execute(command);

        ResultSet rs = statement.getResultSet();

        if (hasResults && rs != null)
        {
            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();
            for (int i = 1; i <= cols; i++)
            {
                String name = md.getColumnName(i);
                logger.debug(name + "\t");
            }
            while (rs.next())
            {
                for (int i = 1; i <= cols; i++)
                {
                    String value = rs.getString(i);
                    logger.debug(value + "\t");
                }
            }
        }

        try
        {
            statement.close();
        }
        catch (Exception e)
        {
            // Ignore to workaround a bug in Jakarta DBCP
        }
        Thread.yield();
    }

}
