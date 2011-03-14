package com.carbonfive.db.migration;

import static org.apache.commons.io.FilenameUtils.*;

public class MigrationFactory
{
    public Migration create(String version, Resource resource)
    {
        final String extension = getExtension(resource.getFilename()).toLowerCase();

        if ("sql".equals(extension))
        {
            return new SQLScriptMigration(version, resource);
        }
        else if ("groovy".equals(extension))
        {
            return new GroovyMigration(version, resource);
        }

        throw new MigrationException("Can't determine migration type for " + resource);
    }

}
