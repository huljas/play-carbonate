package play.modules.carbonate;

import com.carbonfive.db.migration.DataSourceMigrationManager;
import com.carbonfive.db.migration.ResourceMigrationResolver;
import org.apache.commons.lang.StringUtils;
import play.db.DB;
import play.exceptions.UnexpectedException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author heikkiu
 */
public class MigrationUtils {

    private static final SimpleDateFormat versionFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    public static void generate(String path, String description) {
        try {
            File directory = new File(path);
            if (!directory.exists()) {
                System.out.println("Creating non-existent directory " + directory.getAbsolutePath());
                directory.createNewFile();
            }
            String version = versionFormat.format(new Date());
            description = StringUtils.replaceChars(description, ' ', '_');
            description = StringUtils.replaceChars(description, ".,:;", "");
            description = description.toLowerCase();
            File migrationFile = new File(directory, version + "_" + description + ".sql");
            migrationFile.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(migrationFile));
            writer.println("-- You can write you comments here ");
            writer.close();
            System.out.println("New migration file created " + migrationFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runMigrations(String pattern) {
        try {
            DataSourceMigrationManager manager = new DataSourceMigrationManager(DB.datasource);
            ResourceMigrationResolver resolver = new ResourceMigrationResolver(pattern);
            manager.setMigrationResolver(resolver);
            manager.migrate();
        } catch (Exception e) {
            throw new UnexpectedException("Database migrations failed", e);
        }
    }

}
