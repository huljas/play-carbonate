package com.carbonfive.db.migration;

import com.carbonfive.jdbc.DatabaseType;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;

public interface Migration extends Comparable<Migration>
{
    String getVersion();

    String getFilename();

    void migrate(DatabaseType dbType, Connection connection);

    class MigrationVersionPredicate implements Predicate
    {
        private final String version;

        public MigrationVersionPredicate(String version)
        {
            this.version = version;
        }

        public boolean evaluate(Object object)
        {
            return StringUtils.equalsIgnoreCase(((Migration) object).getVersion(), version);
        }
    }
}
