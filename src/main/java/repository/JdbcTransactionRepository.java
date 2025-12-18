package repository;

import domain.Transaction;
import repository.storage.LineConverter;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *JDBC-baserad implementation av TransactionRepository.
 * - Denna klass tillhandahåller metoder som kräver userId (findAllForUser, saveForUser, osv).
 * - De generiska metoderna save(...) och saveAll(...) I interfacet är implementerade men
 *   antingen kastar UnsupportedOperationException eller är no-op för att undvika oavsiktlig
 *   databasanrop utan user_id.
 */
public final class JdbcTransactionRepository implements repository.TransactionRepository {

    // Mapping index -> id
    private final List<Integer> lastFetchedIds = new ArrayList<>();

    public JdbcTransactionRepository() {
        // Ingen init krävs
    }

    /* ---------- Hjälpmetoder ---------- */

    // Konstruerar en Transaction från ResultSet (hanterar nullable date)
    private Transaction fromResultSet(ResultSet rs) throws SQLException {
        Date d = rs.getDate("date");
        LocalDate date = d != null ? d.toLocalDate() : null;
        double amount = rs.getDouble("amount");
        String description = rs.getString("description");
        return new Transaction(date, amount, description);
    }

    /* ---------- Metoder som arbetar per-user ---------- */

    /**
     *  Hämtar alla transaktioner för en specifik userId (ordnade på created_at).
     *  Uppdaterar lastFetchedIds så att deleteByIndexForUser kan användas.
     */
    public List<Transaction> findAllForUser(int userId) {
        List<Transaction> list = new ArrayList<>();
        lastFetchedIds.clear();

        String sql = "SELECT id, date, amount, description FROM transactions WHERE user_id = ? ORDER BY created_at ASC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Transaction t = fromResultSet(rs);
                    list.add(t);
                    lastFetchedIds.add(id);
                }
            }
        } catch (SQLException e) {
            System.out.println("Jag kunde inte läsa transaktioner: " + e.getMessage());
        }

        return list;
    }

    /**
     * Sparar en transaktion för given userId.
     * Returnerar transaktionen (oförändrad) eller loggar fel.
     */
    public Transaction saveForUser(Transaction tx, int userId) {
        String sql = "INSERT INTO transactions (user_id, type, amount, description, created_at, date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
        String type = tx.getAmount() >= 0 ? "INCOME" : "EXPENSE";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setDouble(3, tx.getAmount());
            ps.setString(4, tx.getDescription());
            ps.setDate(5, Date.valueOf(tx.getDate()));
            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Jag kunde inte spara transaktionen: " + e.getMessage());
        }
        return tx;
    }

    /**
     * Raderar transaktion genom index (index enligt senaste findAllForUser()).
     * Returnerar true om raderingen lyckades.
     */
    public boolean deleteByIndexForUser(int index) {
        if (index < 0 || index >= lastFetchedIds.size()) return false;
        int id = lastFetchedIds.get(index);
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            System.out.println("Jag kunde inte ta bort transaktionen: " + e.getMessage());
            return false;
        }
    }

    /**
     * Hittar transaktioner i ett datumintervall för en user.
     */
    public List<Transaction> findByDateRangeForUser(LocalDate from, LocalDate to, int userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT id, date, amount, description FROM transactions WHERE user_id = ? AND date BETWEEN ? AND ? ORDER BY date ASC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(from));
            ps.setDate(3, Date.valueOf(to));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Jag kunde inte hämta transaktioner i datumintervallet: " + e.getMessage());
        }
        return list;
    }

    /**
     * Rensar och skriver hela listan för en user (ersätter allt).
     * Implementerat med DELETE + batch-insert i en transaktion.
     */
    public void saveAllForUser(List<Transaction> all, int userId) {
        String deleteSql = "DELETE FROM transactions WHERE user_id = ?";
        String insertSql = "INSERT INTO transactions (user_id, type, amount, description, created_at, date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement del = c.prepareStatement(deleteSql)) {
                del.setInt(1, userId);
                del.executeUpdate();
            }

            try (PreparedStatement ins = c.prepareStatement(insertSql)) {
                for (Transaction t : all) {
                    ins.setInt(1, userId);
                    ins.setString(2, t.getAmount() >= 0 ? "INCOME" : "EXPENSE");
                    ins.setDouble(3, t.getAmount());
                    ins.setString(4, t.getDescription());
                    ins.setDate(5, Date.valueOf(t.getDate()));
                    ins.addBatch();
                }
                ins.executeBatch();
            }
            c.commit();
        } catch (SQLException e) {
            System.out.println("Jag kunde inte spara alla transaktioner: " + e.getMessage());
        }
    }

    /**
     * Räknar antalet transaktioner för en user.
     */
    public int countForUser(int userId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE user_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Jag kunde inte räkna transaktioner: " + e.getMessage());
        }
        return 0;
    }

    /* ---------- Implementering av generiska repository-metoder (från interfacet) ---------- */

    /**
     * Generisk save - i denna DB-kodbas ska du alltid använda saveForUser(tx, userId).
     * Kastar UnsupportedOperationException för att undvika att någon skriver row utan user_id.
     */
    @Override
    public Transaction save(Transaction tx) {
        throw new UnsupportedOperationException("Använd saveForUser(tx, userId). transactions-tabellen kräver user_id.");
    }

    /**
     * Generisk saveAll - implementerar som no-op för backward compatibility.
     * Anropa saveAllForUser(all, userId) för riktig persistens.
     */
    @Override
    public void saveAll(List<domain.Transaction> all) {
        // no-op: använd saveAllForUser(all, userId) för riktig skrivning till DB
    }

    /**
     * Generisk deleteByIndex - vidarekopplar till deleteByIndexForUser (kräver att findAllForUser körts).
     */
    @Override
    public boolean deleteByIndex(int index) {
        return deleteByIndexForUser(index);
    }

    /**
     * Generisk findAll - utan user context är detta inte meningsfullt för DB-backend.
     * Returnerar tom lista och rekommenderar findAllForUser(userId).
     */
    @Override
    public List<Transaction> findAll() {
        return Collections.emptyList();
    }

    /// Generisk count (alla users)
    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM transactions";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("Jag kunde inte räkna transaktioner (all): " + e.getMessage());
        }
        return 0;
    }

    /**
     * Generisk findByDateRange - eftersom DB-metoder normalt kräver userId så returnerar tom lista.
     * Använd findByDateRangeForUser(from, to, userId) istället.
     */
    @Override
    public List<Transaction> findByDateRange(LocalDate from, LocalDate to) {
        return Collections.emptyList();
    }
}
