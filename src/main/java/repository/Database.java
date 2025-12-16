package repository;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Enkel Database-hjälpare för projektet.
 *
 * - Jag läser konfiguration från src/main/resources/application.properties
 * - Jag försöker läsa både jdbc.* och spring.datasource.* så du kan använda vilket format du vill.
 * - Jag gör enkel substitution av ${key} om någon property innehåller en placeholder.
 *
 * Användning:
 * try (Connection conn = Database.getConnection())
 *     // använd conn
 *
 *
 * Notera: kallande kod ansvarar för att stänga Connection.
 */
public final class Database {

    private static final String PROPS_PATH = "/application.properties";

    private static String url;
    private static String user;
    private static String password;
    private static String driver;

    static {
        loadProperties();
        initDriver();
    }

    private Database() {
        // Ingen instansiering
    }

    /** Läs konfig från resources/application.properties */
    private static void loadProperties() {
        try (InputStream in = Database.class.getResourceAsStream(PROPS_PATH)) {
            if (in == null) {
                throw new IllegalStateException("Kan inte hitta " + PROPS_PATH + " på classpath.");
            }
            Properties p = new Properties();
            p.load(in);

            // Enkel placeholder-resolution: ersätt ${key} med p.getProperty(key) om möjligt
            for (String name : p.stringPropertyNames()) {
                String val = p.getProperty(name);
                if (val != null && val.contains("${")) {
                    // enkel implementation: ersätt ${x} med property x (flera förekomster hanteras)
                    int start;
                    while ((start = val.indexOf("${")) >= 0) {
                        int end = val.indexOf('}', start);
                        if (end < 0) break;
                        String key = val.substring(start + 2, end);
                        String repl = p.getProperty(key, "");
                        val = val.substring(0, start) + repl + val.substring(end + 1);
                    }
                    p.setProperty(name, val);
                }
            }

            // Prioritera jdbc.* om de finns, annars försök spring.datasource.*
            url = firstNonEmpty(
                    p.getProperty("jdbc.url"),
                    p.getProperty("spring.datasource.url")
            );
            user = firstNonEmpty(
                    p.getProperty("jdbc.user"),
                    p.getProperty("spring.datasource.username")
            );
            password = firstNonEmpty(
                    p.getProperty("jdbc.password"),
                    p.getProperty("spring.datasource.password")
            );
            driver = firstNonEmpty(
                    p.getProperty("jdbc.driver"),
                    p.getProperty("spring.datasource.driver-class-name")
            );

            if (url == null || user == null || password == null) {
                throw new IllegalStateException("application.properties saknar minst en av jdbc.url/jdbc.user/jdbc.password eller motsvarande spring.datasource.*");
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Misslyckades läsa application.properties: " + e.getMessage());
        }
    }

    private static String firstNonEmpty(String... vals) {
        for (String v : vals) {
            if (v != null && !v.isBlank()) return v;
        }
        return null;
    }

    /** Ladda JDBC-drivrutinen om driver-namn finns angivet */
    private static void initDriver() {
        if (driver != null && !driver.isBlank()) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                // Jag kastar en Error-liknande exception så det syns tidigt vid appstart
                throw new ExceptionInInitializerError("Kunde inte ladda JDBC-driver: " + driver + " — " + e.getMessage());
            }
        }
    }

    /**
     * Returnerar en ny Connection. Anroparen måste stänga den.
     *
     * @return java.sql.Connection
     * @throws SQLException vid fel att öppna connection
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Enkel testmetod som försöker öppna en connection och returnerar true om det lyckas.
     * Användbar för snabba health-checks i appen eller vid debug.
     */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
