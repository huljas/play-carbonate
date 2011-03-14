package play.modules.carbonate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author heikkiu
 */
public class NewMigrationMain {
    private static String path = System.getProperty("migration.path", "conf/migrations");

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("Please write description for you migration:");
            String description = reader.readLine();
            MigrationUtils.generate(path, description);
            System.exit(0);
        }
    }
}
