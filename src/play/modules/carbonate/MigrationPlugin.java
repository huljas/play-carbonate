package play.modules.carbonate;

import play.Logger;
import play.Play;
import play.PlayPlugin;

/**
 * @author heikkiu
 */
public class MigrationPlugin extends PlayPlugin {

    public void onApplicationStart() {
        String pattern = (String) Play.configuration.get("migrations.pattern");
        if (pattern != null) {
            Logger.info("Running migrations from pattern " + pattern);
            MigrationUtils.runMigrations(pattern);
        } else {
            Logger.info("Missing configuration migrations.pattern, database migrations will be ignored!");
        }
    }

}
