package service;

import domain.Transaction;
import repository.TransactionRepository;
import repository.FileTransactionRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;

/**
 * Hanterar alla transaktioner och beräkningar för personal finance-applikationen.
 * Här ligger min logik (lägga till/ta bort, balans, rapporter).
 */
public class FinanceManager {

    // Min lagringsabstraktion (repository pattern – VG-krav)
    private final TransactionRepository repository;

    /**
     * Konstruktor: skapar en FinanceManager med fil-baserad lagring.
     * Repository laddar automatiskt data från fil vid start.
     */
    public FinanceManager() {
        // Använder FileTransactionRepository (samma filväg som innan)
        this.repository = new FileTransactionRepository("src/resources/transactions.csv");
    }

    /**
     * Alternativ konstruktor: låter mig injicera ett annat repository
     * (t.ex. för testning eller annan lagringsmetod).
     */
    public FinanceManager(TransactionRepository repository) {
        this.repository = repository;
    }

    // Lägger till en ny transaktion (repository sparar automatiskt)
    public void addTransaction(Transaction transaction) {
        repository.save(transaction);
        System.out.println("Transaktion tillagd!");
    }

    // Tar bort en transaktion baserat på index (repository sparar automatiskt)
    public void removeTransaction(int index) {
        if (repository.deleteByIndex(index)) {
            System.out.println("Transaktion borttagen!");
        } else {
            System.out.println("Ogiltigt index.");
        }
    }

    // Beräknar och returnerar nuvarande kontobalans (summa av alla belopp)
    public double getBalance() {
        return repository.findAll().stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Skriver ut alla transaktioner i terminalen (hjälp vid felsökning)
    public void printAllTransactions() {
        List<Transaction> all = repository.findAll();
        if (all.isEmpty()) {
            System.out.println("Inga transaktioner finns.");
            return;
        }

        System.out.println("\n=== Alla Transaktioner ===");
        for (int i = 0; i < all.size(); i++) {
            System.out.println(i + ": " + all.get(i));
        }
    }

    // === Rapporter (år, månad, vecka, dag) ===

    // Total inkomst för ett specifikt år
    public double getYearlyIncome(int year) {
        return repository.findAll().stream()
                .filter(t -> t.getDate().getYear() == year && t.isIncome())
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Totala utgifter för ett specifikt år
    public double getYearlyExpenses(int year) {
        return Math.abs(repository.findAll().stream()
                .filter(t -> t.getDate().getYear() == year && t.isExpense())
                .mapToDouble(Transaction::getAmount)
                .sum());
    }

    // Total inkomst för specifik månad
    public double getMonthlyIncome(int year, int month) {
        return repository.findAll().stream()
                .filter(t -> t.getDate().getYear() == year
                        && t.getDate().getMonthValue() == month
                        && t.isIncome())
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Totala utgifter för specifik månad
    public double getMonthlyExpenses(int year, int month) {
        return Math.abs(repository.findAll().stream()
                .filter(t -> t.getDate().getYear() == year
                        && t.getDate().getMonthValue() == month
                        && t.isExpense())
                .mapToDouble(Transaction::getAmount)
                .sum());
    }

    // Total inkomst för specifik vecka (ALIGNED_WEEK_OF_YEAR)
    public double getWeeklyIncome(int year, int week) {
        return repository.findAll().stream()
                .filter(t -> t.getDate().getYear() == year
                        && t.getDate().get(ChronoField.ALIGNED_WEEK_OF_YEAR) == week
                        && t.isIncome())
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Totala utgifter för specifik vecka
    public double getWeeklyExpenses(int year, int week) {
        return Math.abs(repository.findAll().stream()
                .filter(t -> t.getDate().getYear() == year
                        && t.getDate().get(ChronoField.ALIGNED_WEEK_OF_YEAR) == week
                        && t.isExpense())
                .mapToDouble(Transaction::getAmount)
                .sum());
    }

    // Total inkomst för en specifik dag
    public double getDailyIncome(LocalDate date) {
        return repository.findAll().stream()
                .filter(t -> t.getDate().equals(date) && t.isIncome())
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Totala utgifter för en specifik dag
    public double getDailyExpenses(LocalDate date) {
        return Math.abs(repository.findAll().stream()
                .filter(t -> t.getDate().equals(date) && t.isExpense())
                .mapToDouble(Transaction::getAmount)
                .sum());
    }

    // === Publika metoder för GUI/terminal-app ===

    // Sparar alla transaktioner till fil (nu via repository)
    public void saveToFile() {
        // Repository sparar automatiskt vid varje add/remove,
        // men jag behåller denna metod för GUI-knappen "Spara data"
        repository.saveAll(repository.findAll());
        System.out.println("Data sparad!");
    }

    // Returnerar antalet transaktioner
    public int getTransactionCount() {
        return repository.count();
    }

    // Hämtar alla transaktioner (för att visa i JavaFX-tabellen)
    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }
}