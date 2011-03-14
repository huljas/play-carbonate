package play.modules.carbonate;

import org.apache.log4j.Logger;
import org.hibernate.cfg.DefaultNamingStrategy;

/**
 * @author heikkiu
 */
public class CamelCaseNamingStrategy extends DefaultNamingStrategy {

    private static Logger logger = Logger.getLogger(CamelCaseNamingStrategy.class);

    public static String camelCaseToUnderscore(String camelCase) {
        String underscore = camelCase.replaceAll("[A-Z]", "_$0");
        if (underscore.startsWith("_")) {
            return underscore.substring(1);
        }
        return underscore;
    }

    public static String upperCaseFirst(String s) {
        if (s.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append(s.substring(0,1).toUpperCase());
            if (s.length() > 1) {
                builder.append(s.substring(1));
            }
            return builder.toString();
        } else {
            return s;
        }
    }

    public String classToTableName(String className) {
        return camelCaseToUnderscore(className).toLowerCase();
    }

    public String propertyToColumnName(String property) {
        return camelCaseToUnderscore(property).toLowerCase();
    }

    @Override
    public String tableName(String tableName) {
        String s = super.tableName(tableName);
        logger.debug("tableName: " + tableName + " => " + s);
        return s;
    }

    @Override
    public String columnName(String columnName) {
        String s = super.columnName(columnName);
        logger.debug("columnName: " + columnName + " => " + s);
        return s;
    }

    @Override
    public String collectionTableName(String ownerEntity, String ownerEntityTable, String associatedEntity, String associatedEntityTable, String propertyName) {
        String s = camelCaseToUnderscore(ownerEntityTable + "To" + upperCaseFirst(associatedEntityTable)).toLowerCase();
        logger.debug("collectionTableName: " + ownerEntity + "," + ownerEntityTable + "," + associatedEntity + "," + associatedEntityTable + "," + propertyName + " => " + s);
        return s;
    }

    @Override
    public String joinKeyColumnName(String joinedColumn, String joinedTable) {
        String s = super.joinKeyColumnName(joinedColumn, joinedTable);
        logger.debug("joinKeyColumnName: " + joinedColumn + "," + joinedTable + " => " + s);
        return s;
    }

    @Override
    public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
        String s = camelCaseToUnderscore(propertyTableName + "_" + referencedColumnName).toLowerCase();
        logger.debug("foreignKeyColumnName: " + propertyName + "," + propertyEntityName + "," + propertyTableName + "," + referencedColumnName + " => " + s);
        return s;
    }

    @Override
    public String logicalColumnName(String columnName, String propertyName) {
        String s = super.logicalColumnName(columnName, propertyName);
        logger.debug("logicalColumnName: " + columnName + "," + propertyName + " => " + s);
        return s;
    }

    @Override
    public String logicalCollectionTableName(String tableName, String ownerEntityTable, String associatedEntityTable, String propertyName) {
        String s = super.logicalCollectionTableName(tableName, ownerEntityTable, associatedEntityTable, propertyName);
        logger.debug("logicalCollectionTableName: " + tableName + "," + ownerEntityTable + "," + associatedEntityTable + "," + propertyName + " => " + s);
        return s;
    }

    @Override
    public String logicalCollectionColumnName(String columnName, String propertyName, String referencedColumn) {
        String s = super.logicalCollectionColumnName(columnName, propertyName, referencedColumn);
        logger.debug("logicalCollectionColumnName: " + columnName + "," + propertyName + "," + referencedColumn + " => " + s);
        return s;
    }
}
