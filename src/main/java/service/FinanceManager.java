package service;

import domain.Transaction;
import domain.User;
import repository.JdbcTransactionRepository;
import repository.JdbcUserRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service-klass som innehåller applikationens affärslogik.
 * Ansvar:
 * - Hanterar inloggning, registrering och utloggning
 * - Håller reda på aktuell inloggad användare
 * - Anropar repository-klasser för databasåtkomst
 * - Utför beräkningar som balans och rapporter
 */
public class FinanceManager {

    // Repository för transaktioner (kopplade till användare)
    private final JdbcTransactionRepository txRepository;

    // Repository för användare (login/registrering)
    private final JdbcUserRepository userRepository;

    // Inloggad användares id (null om ingen är inloggad)
    private Integer currentUserId = null;

    // Inloggad användares användarnamn
    private String currentUsername = null;

    // Skapar repositories vid start av applikationen
    public FinanceManager() {
        this.txRepository = new JdbcTransactionRepository();
        this.userRepository = new JdbcUserRepository();
    }

    // ===== Autentisering / konto =====

    /**
     * Registrerar en ny användare.
     * Returnerar Optional<User> om det lyckas.
     */
    public Optional<User> register(String username, String password) {
        var opt = userRepository.registerUser(username, password);
        return opt;
    }

    /**
     * Registrerar användare och loggar in direkt vid lyckad registrering.
     */
    public boolean registerUser(String username, String password) {
        var opt = register(username, password);
        if (opt.isPresent()) {
            loginAs(opt.get());
            return true;
        }
        return false;
    }

    /**
     * Autentiserar användare och returnerar Optional<User>.
     */
    public Optional<User> authenticate(String username, String password) {
        return userRepository.authenticate(username, password);
    }

    /**
     * Loggar in användare (boolean-variant för GUI:t).
     */
    public boolean login(String username, String password) {
        return loginUser(username, password);
    }

    /**
     * Loggar in användare om användarnamn och lösenord är korrekta.
     */
    public boolean loginUser(String username, String password) {
        var opt = authenticate(username, password);
        if (opt.isPresent()) {
            loginAs(opt.get());
            return true;
        }
        return false;
    }

    // Sätter aktuell användare efter lyckad inloggning
    private void loginAs(User user) {
        if (user == null) return;
        this.currentUserId = user.getId();
        this.currentUsername = user.getUsername();
    }

    // Loggar ut aktuell användare
    public void logout() {
        this.currentUserId = null;
        this.currentUsername = null;
    }

    // Kontrollerar om någon användare är inloggad
    public boolean isAuthenticated() {
        return this.currentUserId != null;
    }

    public Integer getCurrentUserId() {
        return this.currentUserId;
    }

    public String getCurrentUsername() {
        return this.currentUsername;
    }

    // ===== Transaktioner (per användare) =====

    /**
     * Hämtar alla transaktioner för inloggad användare.
     */

    public List<Transaction> getAllTransactions() {
        if (!isAuthenticated()) return new ArrayList<>();
        return txRepository.findAllForUser(this.currentUserId);
    }

    /**
     * Lägger till en ny transaktion för inloggad användare.
     */
    public void addTransaction(Transaction tx) {
        if (!isAuthenticated()) throw new IllegalStateException("Ingen användare inloggad");
        txRepository.saveForUser(tx, this.currentUserId);
    }

    /**
     * Tar bort en transaktion baserat på index.
     */
    public boolean removeTransaction(Transaction tx) {
        if (!isAuthenticated()) return false;
        return txRepository.deleteByIdForUser(tx.getId(), currentUserId);
    }



    /**
     * Returnerar antal transaktioner för inloggad användare.
     */
    public int getTransactionCount() {
        if (!isAuthenticated()) return 0;
        return txRepository.countForUser(this.currentUserId);
    }

    /**
     * Skriver ut alla transaktioner till konsolen (främst för debugging).
     */
    public void printAllTransactions() {
        if (!isAuthenticated()) {
            System.out.println("Ingen användare inloggad.");
            return;
        }
        var all = getAllTransactions();
        if (all.isEmpty()) {
            System.out.println("Inga transaktioner för användare: " + currentUsername);
            return;
        }
        System.out.println("\n=== Alla transaktioner för " + currentUsername + " ===");
        for (int i = 0; i < all.size(); i++) {
            System.out.println(i + " : " + all.get(i));
        }
    }

    /**
     * Synkroniserar aktuella transaktioner till databasen.
     */
    public void saveToFile() {
        if (!isAuthenticated()) {
            System.out.println("Ingen användare inloggad: inget att spara.");
            return;
        }
        var all = getAllTransactions();
        txRepository.saveAllForUser(all, this.currentUserId);
    }

    // ===== Rapporter / beräkningar =====

    /**
     * Beräknar aktuell balans (inkomster - utgifter).
     */
    public double getBalance() {
        var all = getAllTransactions();
        return all.stream().mapToDouble(Transaction::getAmount).sum();
    }

    /**
     * Returnerar total inkomst för ett år.
     */
    public double getYearlyIncome(int year) {
        if (!isAuthenticated()) return 0.0;
        return txRepository.sumYearlyIncomeForUser(currentUserId, year);
    }

    public double getYearlyExpenses(int year) {
        if (!isAuthenticated()) return 0.0;
        return txRepository.sumYearlyExpensesForUser(currentUserId, year);
    }


    /**
     * Returnerar inkomst för en specifik månad.
     */
    public double getMonthlyIncome(int year, int month) {
        var all = getAllTransactions();
        return all.stream()
                .filter(t -> t.getDate() != null && t.getDate().getYear() == year && t.getDate().getMonthValue() == month)
                .mapToDouble(t -> t.getAmount() > 0 ? t.getAmount() : 0.0)
                .sum();
    }

    /**
     * Returnerar utgifter för en specifik månad.
     */
    public double getMonthlyExpenses(int year, int month) {
        var all = getAllTransactions();
        return all.stream()
                .filter(t -> t.getDate() != null && t.getDate().getYear() == year && t.getDate().getMonthValue() == month)
                .mapToDouble(t -> t.getAmount() < 0 ? Math.abs(t.getAmount()) : 0.0)
                .sum();
    }

    /**
     * Returnerar inkomst för en specifik vecka.
     */
    public double getWeeklyIncome(int year, int week) {
        var all = getAllTransactions();
        return all.stream()
                .filter(t -> t.getDate() != null && t.getDate().getYear() == year && weekOfYear(t.getDate()) == week)
                .mapToDouble(t -> t.getAmount() > 0 ? t.getAmount() : 0.0)
                .sum();
    }

    /**
     * Returnerar utgifter för en specifik vecka.
     */
    public double getWeeklyExpenses(int year, int week) {
        var all = getAllTransactions();
        return all.stream()
                .filter(t -> t.getDate() != null && t.getDate().getYear() == year && weekOfYear(t.getDate()) == week)
                .mapToDouble(t -> t.getAmount() < 0 ? Math.abs(t.getAmount()) : 0.0)
                .sum();
    }

    /**
     * Returnerar inkomst för ett specifikt datum.
     */
    public double getDailyIncome(LocalDate date) {
        var all = getAllTransactions();
        return all.stream()
                .filter(t -> t.getDate() != null && t.getDate().isEqual(date))
                .mapToDouble(t -> t.getAmount() > 0 ? t.getAmount() : 0.0)
                .sum();
    }

    /**
     * Returnerar utgifter för ett specifikt datum.
     */
    public double getDailyExpenses(LocalDate date) {
        var all = getAllTransactions();
        return all.stream()
                .filter(t -> t.getDate() != null && t.getDate().isEqual(date))
                .mapToDouble(t -> t.getAmount() < 0 ? Math.abs(t.getAmount()) : 0.0)
                .sum();
    }

    // Hjälpmetod för att räkna ut veckonummer från datum
    private int weekOfYear(LocalDate date) {
        return date.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
    }
}
