package repository;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Ansvarar för all databaskonfiguration och skapande av JDBC-connection.
 * Klassen:
 * - Läser databasinställningar från application.properties
 * - Laddar JDBC-drivrutin vid uppstart
 * - Tillhandahåller Connection till databasen
 */
public final class Database {

    // Filnamn för konfigurationsfilen som ligger i src/main/resources
    private static final String PROPS_PATH = "application.properties";

    // Databasinställningar som laddas från properties-filen
    private static String url;
    private static String user;
    private static String password;
    private static String driver;

    // Körs automatiskt när klassen laddas första gången
    static {
        loadProperties();
        loadDriver();
    }

    // Privat konstruktor förhindrar att klassen instansieras
    private Database() {

    }

    // Läser application.properties från classpath och laddar databasinställningar
    private static void loadProperties() {
        try (InputStream in =
                     Database.class.getClassLoader().getResourceAsStream(PROPS_PATH)) {

            // Säkerställer att properties-filen finns på classpath
            if (in == null) {
                throw new IllegalStateException(
                        "Kan inte hitta " + PROPS_PATH + " på classpath."
                );
            }

            Properties p = new Properties();
            p.load(in);

            url = p.getProperty("jdbc.url");
            user = p.getProperty("jdbc.user");
            password = p.getProperty("jdbc.password");
            driver = p.getProperty("jdbc.driver");

            // Grundläggande validering av obligatoriska inställningar
            if (url == null || user == null || password == null) {
                throw new IllegalStateException(
                        "application.properties saknar jdbc.url/jdbc.user/jdbc.password"
                );
            }

        } catch (Exception e) {
            // Stoppar applikationen om databaskonfigurationen inte kan laddas
            throw new ExceptionInInitializerError(
                    "Misslyckades läsa application.properties: " + e.getMessage()
            );
        }
    }

    // Laddar JDBC-drivrutinen manuellt om den anges i properties
    private static void loadDriver() {
        if (driver != null && !driver.isBlank()) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new ExceptionInInitializerError(
                        "Kunde inte ladda JDBC-driver: " + driver
                );
            }
        }
    }

    // Skapar och returnerar en ny databas-connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    // Används för att testa om databaskopplingen fungerar
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
