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
 * FinanceManager - hanterar applikationens affärslogik.
 *
 * - Pratar med JdbcUserRepository för register/login.
 * - Pratar med JdbcTransactionRepository för transaktioner (per-användare).
 * - Håller currentUserId och currentUsername internt efter inloggning.
 *
 * Filväg: src/main/java/service/FinanceManager.java
 */
public class FinanceManager {

    private final JdbcTransactionRepository txRepository;
    private final JdbcUserRepository userRepository;

    private Integer currentUserId = null;
    private String currentUsername = null;

    public FinanceManager() {
        this.txRepository = new JdbcTransactionRepository();
        this.userRepository = new JdbcUserRepository();
    }

    // ===== Auth / account =====

    /**
     * Registrera och returnera Optional<User> vid framgång.
     */
    public Optional<User> register(String username, String password) {
        var opt = userRepository.registerUser(username, password);
        return opt;
    }

    /**
     * Registrera och logga in (boolean).
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
     * Autentisera och returnera Optional<User>.
     */
    public Optional<User> authenticate(String username, String password) {
        return userRepository.authenticate(username, password);
    }

    /**
     * Logga in och returnera boolean.
     * Denna metod finns för att GUI-koden som anropar financeManager.login(...) ska kompilera.
     */
    public boolean login(String username, String password) {
        return loginUser(username, password);
    }

    /**
     * Logga in (boolean-variant).
     */
    public boolean loginUser(String username, String password) {
        var opt = authenticate(username, password);
        if (opt.isPresent()) {
            loginAs(opt.get());
            return true;
        }
        return false;
    }

    private void loginAs(User user) {
        if (user == null) return;
        this.currentUserId = user.getId();
        this.currentUsername = user.getUsername();
    }

    public void logout() {
        this.currentUserId = null;
        this.currentUsername = null;
    }

    public boolean isAuthenticated() {
        return this.currentUserId != null;
    }

    public Integer getCurrentUserId() {
        return this.currentUserId;
    }

    public String getCurrentUsername() {
        return this.currentUsername;
    }

    // ===== Transaktioner (per-användare) =====

    public List<Transaction> getAllTransactions() {
        if (!isAuthenticated()) return new ArrayList<>();
        return txRepository.findAllForUser(this.currentUserId);
    }

    public void addTransaction(Transaction tx) {
        if (!isAuthenticated()) throw new IllegalStateException("Ingen användare inloggad");
        txRepository.saveForUser(tx, this.currentUserId);
    }

    public boolean removeTransaction(int index) {
        if (!isAuthenticated()) return false;
        return txRepository.deleteByIndexForUser(index);
    }

    public int getTransactionCount() {
        if (!isAuthenticated()) return 0;
        return txRepository.countForUser(this.currentUserId);
    }

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
     * Synka aktuell lista till DB (ersätter allt för användaren).
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

    public double getBalance() {
        var all = getAllTransactions();
        return all.stream().mapToDouble(Transaction::getAmount).sum();
    }

    public double getYearlyIncome(int year) {
        var all = getAllTransactions();
        return all.stream()
                .filter(t -> t.getDate() != null && t.getDate().getYear() == year)
                .mapToDouble(t -> t.getAmount() > 0 ? t.getAmount() : 0.0)
                .sum();
    }

    public double getYearlyExpenses(int year) {
        var all = getAllTransactions();
        return all.stream()
                .filter(t -> t.getDate() != null && t.getDate().getYear() == year)
                .mapToDouble(t -> t.getAmount() < 0 ? Math.abs(t.getAmount()) : 0.0)
                .sum();
    }

    public double getMonthlyIncome(int year, int month) {
        var all = getAllTransactions();
        return all.stream()
                .filter(t -> t.getDate() != null && t.getDate().getYear() == year && t.getDate().getMonthValue() == month)
                .mapToDouble(t -> t.getAmount() > 0 ? t.getAmount() : 0.0)
                .sum();
    }

    public double getMonthlyExpenses(int year, int month) {
        var all = getAllTransactions();
        return all.stream()
                .filter(t -> t.getDate() != null && t.getDate().getYear() == year && t.getDate().getMonthValue() == month)
                .mapToDouble(t -> t.getAmount() < 0 ? Math.abs(t.getAmount()) : 0.0)
                .sum();
    }

    public double getWeeklyIncome(int year, int week) {
        var all = getAllTransactions();
        return all.stream()
                .filter(t -> t.getDate() != null && t.getDate().getYear() == year && weekOfYear(t.getDate()) == week)
                .mapToDouble(t -> t.getAmount() > 0 ? t.getAmount() : 0.0)
                .sum();
    }

    public double getWeeklyExpenses(int year, int week) {
        var all = getAllTransactions();
        return all.stream()
                .filter(t -> t.getDate() != null && t.getDate().getYear() == year && weekOfYear(t.getDate()) == week)
                .mapToDouble(t -> t.getAmount() < 0 ? Math.abs(t.getAmount()) : 0.0)
                .sum();
    }

    public double getDailyIncome(LocalDate date) {
        var all = getAllTransactions();
        return all.stream()
                .filter(t -> t.getDate() != null && t.getDate().isEqual(date))
                .mapToDouble(t -> t.getAmount() > 0 ? t.getAmount() : 0.0)
                .sum();
    }

    public double getDailyExpenses(LocalDate date) {
        var all = getAllTransactions();
        return all.stream()
                .filter(t -> t.getDate() != null && t.getDate().isEqual(date))
                .mapToDouble(t -> t.getAmount() < 0 ? Math.abs(t.getAmount()) : 0.0)
                .sum();
    }

    private int weekOfYear(LocalDate date) {
        return date.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
    }
}
