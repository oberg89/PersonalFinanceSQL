package domain;

/**
 * Jag representerar en användare i systemet.
 * Jag har id, användarnamn och en password-hash (BCrypt).
 * Jag använder enkel getters/setters så jag kan lagra och läsa från DB.
 */
public class User {
    private Integer id;
    private String username;
    private String passwordHash;

    public User() {}

    public User(Integer id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public User(String username, String passwordHash) {
        this(null, username, passwordHash);
    }

    // Jag returnerar id
    public Integer getId() {
        return id;
    }

    // Jag sätter id (används efter insert)
    public void setId(Integer id) {
        this.id = id;
    }

    // Jag returnerar användarnamnet
    public String getUsername() {
        return username;
    }

    // Jag sätter användarnamnet
    public void setUsername(String username) {
        this.username = username;
    }

    // Jag returnerar password-hashen (inte plain)
    public String getPasswordHash() {
        return passwordHash;
    }

    // Jag sätter password-hashen
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
