package play.modules.carbonate;

import org.apache.commons.lang.StringUtils;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import play.Logger;
import play.Play;
import play.db.DB;
import play.db.DBPlugin;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.libs.IO;
import play.utils.Utils;

import javax.persistence.Entity;
import java.io.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author heikkiu
 */
public class NewMigrationMain {

    private static final SimpleDateFormat versionFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please write description for you migration:");
        String description = reader.readLine();

        String id = System.getProperty("play.id", "");
        Play.init(new File("."), id);

        Ejb3Configuration configuration = new Ejb3Configuration();
        String driver = Play.configuration.getProperty("db.driver");
        String migrationBody = "";
        if (driver != null) {
            String dialectName = MigrationUtils.getDefaultDialect(driver);
            configuration.setProperty("hibernate.dialect", dialectName);
            Properties fromConf = (Properties) Utils.Maps.filterMap(Play.configuration, "^hibernate\\..*");
            configuration.addProperties(fromConf);
            List<Class> classes = Play.classloader.getAnnotatedClasses(Entity.class);
            Thread.currentThread().setContextClassLoader(Play.classloader);
            if (classes.isEmpty()) {
                Logger.warn("No entities detected!");
            }
            for (Class clazz : classes) {
                configuration.addAnnotatedClass(clazz);
            }
            configuration.buildEntityManagerFactory();
            DBPlugin plugin = new DBPlugin();
            plugin.onApplicationStart();
            Dialect dialect = (Dialect) Class.forName(dialectName).newInstance();
            DatabaseMetadata metadata = new DatabaseMetadata(DB.getConnection(), dialect);
            String[] content = configuration.getHibernateConfiguration().generateSchemaUpdateScript(dialect, metadata);
            if (content.length == 0) {
                Logger.warn("No changes from schema update!");
            }
            migrationBody = StringUtils.join(content, "\n");
        } else {
            Logger.warn("Property 'db.driver' not defined in Play configuration, ignoring schema update!");
        }
        String path = MigrationUtils.getPath();
        File directory = new File(path);
        if (!directory.exists()) {
            Logger.warn("Creating non-existent directory " + directory.getAbsolutePath());
            directory.mkdirs();
        }
        String version = versionFormat.format(new Date());
        description = StringUtils.replaceChars(description, ' ', '_');
        description = StringUtils.replaceChars(description, ".,:;", "");
        description = description.toLowerCase();
        File migrationFile = new File(directory, version + "_" + description + ".sql");
        migrationFile.createNewFile();
        PrintWriter writer = new PrintWriter(new FileWriter(migrationFile));
        writer.println("-- You can write you comments here ");
        writer.println(migrationBody);
        writer.close();
        Logger.warn("New migration file created " + migrationFile.getAbsolutePath());
    }
}
