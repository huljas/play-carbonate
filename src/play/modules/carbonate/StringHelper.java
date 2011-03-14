package play.modules.carbonate;

/**
 * @author heikkiu
 */
public class StringHelper {

    public static String camelCaseToUnderscore(String camelCase) {
        String underscore = camelCase.replaceAll("[A-Z]", "_$0");
        if (underscore.startsWith("_")) {
            return underscore.substring(1);
        }
        return underscore;
    }

    public static String underScoreToCamelCase(String underScore) {
        StringBuilder builder = new StringBuilder();
        boolean upper = false;
        for (char c : underScore.toLowerCase().toCharArray()) {
            if (c == '_') {
                upper = true;
                continue;
            }
            if (upper) {
                builder.append(String.valueOf(c).toUpperCase());
            } else {
                builder.append(c);
            }
            upper = false;
        }
        return builder.toString();
    }
}
