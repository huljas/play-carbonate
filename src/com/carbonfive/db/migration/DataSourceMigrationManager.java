package com.carbonfive.db.migration;

import com.carbonfive.jdbc.DatabaseType;
import com.carbonfive.jdbc.DatabaseUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class DataSourceMigrationManager implements MigrationManager {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final JdbcTemplate jdbcTemplate;
    private DatabaseType dbType;
    private VersionStrategy versionStrategy = new SimpleVersionStrategy();
    private MigrationResolver migrationResolver = new ResourceMigrationResolver();

    public DataSourceMigrationManager(DataSource dataSource) {
        this(dataSource, DatabaseType.UNKNOWN);
        this.dbType = determineDatabaseType();
    }

    public DataSourceMigrationManager(DataSource dataSource, DatabaseType dbType) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dbType = dbType;
    }

    public boolean validate() {
        return pendingMigrations().isEmpty();
    }

    public SortedSet<Migration> pendingMigrations() {
        Set<String> appliedMigrations = determineAppliedMigrationVersions();
        Set<Migration> availableMigrations = migrationResolver.resolve(dbType);

        SortedSet<Migration> pendingMigrations = new TreeSet<Migration>();
        CollectionUtils.select(availableMigrations, new PendingMigrationPredicate(appliedMigrations), pendingMigrations);

        return pendingMigrations;
    }

    public void migrate() {
        // Are migrations enabled?
        if (!isMigrationsEnabled()) {
            enableMigrations();
        }

        final Collection<Migration> pendingMigrations = pendingMigrations();

        if (pendingMigrations.isEmpty()) {
            logger.info("Database is up to date; no migration necessary.");
            return;
        }

        StopWatch watch = new StopWatch();
        watch.start();

        logger.info("Migrating database... applying " + pendingMigrations.size() + " migration" + (pendingMigrations.size() > 1 ? "s" : "") + ".");


        jdbcTemplate.execute(new ConnectionCallback<Object>() {
            public Object doInConnection(Connection connection) throws SQLException {
                Migration currentMigration = null;

                final boolean autoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);

                try {
                    for (Migration migration : pendingMigrations) {
                        currentMigration = migration;
                        logger.info("Running migration " + currentMigration.getFilename() + ".");

                        final Date startTime = new Date();
                        StopWatch migrationWatch = new StopWatch();
                        migrationWatch.start();

                        currentMigration.migrate(dbType, connection);
                        versionStrategy.recordMigration(dbType, connection, currentMigration.getVersion(), startTime, migrationWatch.getTime());

                        connection.commit();
                    }
                } catch (Throwable e) {
                    assert currentMigration != null;
                    String message = "Migration for version " + currentMigration.getVersion() + " failed, rolling back and terminating migration.";
                    logger.error(message, e);
                    connection.rollback();
                    throw new MigrationException(message, e);
                } finally {
                    connection.setAutoCommit(autoCommit);
                }
                return null;
            }
        });

        watch.stop();

        logger.info("Migrated database in " + DurationFormatUtils.formatDurationHMS(watch.getTime()) + ".");
    }

    public void setDatabaseType(DatabaseType dbType) {
        this.dbType = dbType;
    }

    public void setMigrationResolver(MigrationResolver migrationResolver) {
        this.migrationResolver = migrationResolver;
    }

    public void setVersionStrategy(VersionStrategy versionStrategy) {
        this.versionStrategy = versionStrategy;
    }

    protected DatabaseType determineDatabaseType() {
        return (DatabaseType) jdbcTemplate.execute(new ConnectionCallback<DatabaseType>() {
            public DatabaseType doInConnection(Connection connection) throws SQLException {
                return DatabaseUtils.databaseType(connection.getMetaData().getURL());
            }
        });
    }

    protected boolean isMigrationsEnabled() {
        try {
            return (Boolean) jdbcTemplate.execute(new ConnectionCallback<Boolean>() {
                public Boolean doInConnection(Connection connection) throws SQLException {
                    return versionStrategy.isEnabled(dbType, connection);
                }
            });
        } catch (MigrationException e) {
            logger.error("Could not enable migrations.", e);
            throw new MigrationException(e);
        }
    }

    protected void enableMigrations() {
        try {
            jdbcTemplate.execute(new ConnectionCallback<Object>() {
                public Object doInConnection(Connection connection) throws SQLException {
                    versionStrategy.enableVersioning(dbType, connection);
                    return null;
                }
            });

            logger.info("Successfully enabled migrations.");
        } catch (MigrationException e) {
            logger.error("Could not enable migrations.", e);
            throw new MigrationException(e);
        }
    }

    protected Set<String> determineAppliedMigrationVersions() {
        return (Set<String>) jdbcTemplate.execute(new ConnectionCallback<Set<String>>() {
            public Set<String> doInConnection(Connection connection) throws SQLException {
                return versionStrategy.appliedMigrations(dbType, connection);
            }
        });
    }

    private static class PendingMigrationPredicate implements Predicate {
        private final Set<String> appliedMigrations;

        public PendingMigrationPredicate(Set<String> appliedMigrations) {
            this.appliedMigrations = appliedMigrations == null ? new HashSet<String>() : appliedMigrations;
        }

        public boolean evaluate(Object input) {
            if (input instanceof Migration) {
                return !appliedMigrations.contains(((Migration) input).getVersion());
            } else {
                return !appliedMigrations.contains(input.toString());
            }
        }
    }
}
