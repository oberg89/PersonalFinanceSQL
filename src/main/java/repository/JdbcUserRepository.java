package repository;

import domain.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Optional;

/**
 * Repository-klass som ansvarar för all databashantering av användare.
 * Sköter registrering, autentisering och hämtning av användare.
 * Lösenord lagras säkert med BCrypt.
 */
public class JdbcUserRepository {

    /**
     * Registrerar en ny användare.
     * Returnerar Optional<User> om registreringen lyckas,
     * Annars Optional.empty() (t.ex. om användarnamnet redan finns).
     * */
    public Optional<User> registerUser(String username, String plainPassword) {
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?) RETURNING id, created_at";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, hash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    User u = new User(id, username, hash);
                    return Optional.of(u);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            // Om unik-constraint träffas (duplicate key) så returnerar den empty
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                return Optional.empty();
            }
            throw new RuntimeException("Kunde inte registrera användare: " + e.getMessage(), e);
        }
    }

    /**
     * Autentiserar en användare. Returnerar Optional<User> om inloggning lyckas.
     */
    public Optional<User> authenticate(String username, String plainPassword) {
        String sql = "SELECT id, password_hash FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String storedHash = rs.getString("password_hash");
                    if (storedHash != null && BCrypt.checkpw(plainPassword, storedHash)) {
                        User u = new User(id, username, storedHash);
                        return Optional.of(u);
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Kunde inte autentisera användare: " + e.getMessage(), e);
        }
    }

    /**
     * Hittar användare efter username.
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, password_hash FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String hash = rs.getString("password_hash");
                    return Optional.of(new User(id, username, hash));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Kunde inte hämta användare: " + e.getMessage(), e);
        }
    }

    /**
     * Hittar användare efter id.
     */
    public Optional<User> findById(int id) {
        String sql = "SELECT username, password_hash FROM users WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    String hash = rs.getString("password_hash");
                    return Optional.of(new User(id, username, hash));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Kunde inte hämta användare: " + e.getMessage(), e);
        }
    }
}
