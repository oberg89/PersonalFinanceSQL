package app;

import service.FinanceManager;     // Min backend-logik (just nu direkt mot fil, senare via Repository)
import domain.Transaction;         // Min modell för en transaktion

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;


public class PersonalFinanceApp {
    private FinanceManager financeManager;  // Min logik/”service”
    private Scanner scanner;                // Läser in användarens val/inputs

    // Konstruktor som initialiserar applikationen
    public PersonalFinanceApp() {
        financeManager = new FinanceManager();
        scanner = new Scanner(System.in);
    }

    // Startar huvudmenyn för applikationen
    public void start() {
        System.out.println("=== Välkommen till Personal Finance App ===");

        boolean running = true;
        while (running) {
            showMainMenu();
            int choice = getIntInput("Välj alternativ (1-7): ");

            switch (choice) {
                case -1 -> { // exit från prompt
                    System.out.println("Avbryter och stänger ner programmet...");
                    running = false;
                }
                case 1 -> addTransaction();
                case 2 -> removeTransaction();
                case 3 -> showBalance();
                case 4 -> showAllTransactions();
                case 5 -> showReports();
                case 6 -> financeManager.saveToFile();
                case 7 -> {
                    financeManager.saveToFile();
                    System.out.println("Tack för att du använde Personal Finance App!");
                    running = false;
                }
                default -> System.out.println("Ogiltigt val. Försök igen.");
            }
        }

        scanner.close();
    }

    // Visar huvudmenyn
    private void showMainMenu() {
        System.out.println("\n=== HUVUDMENY ===");
        System.out.println("1. Lägg till transaktion");
        System.out.println("2. Ta bort transaktion");
        System.out.println("3. Visa kontobalans");
        System.out.println("4. Visa alla transaktioner");
        System.out.println("5. Visa rapporter");
        System.out.println("6. Spara data");
        System.out.println("7. Avsluta");
    }

    // Lägger till en ny transaktion
    private void addTransaction() {
        System.out.println("\n=== Lägg till transaktion ===");

        LocalDate date = getDateInput("Ange datum (YYYY-MM-DD eller YYYYMMDD) eller tryck Enter för idag: ");
        if (date == null) return; // exit skrivet

        double amount = getDoubleInput("Ange belopp (positivt för inkomst, negativt för utgift): ");
        if (amount == Double.MIN_VALUE) return; // användaren skrev exit

        System.out.print("Ange beskrivning: ");
        String description = scanner.nextLine().trim();
        if (description.equalsIgnoreCase("exit")) {
            System.out.println("Avbryter...");
            return;
        }

        if (description.isEmpty()) {
            description = amount > 0 ? "Inkomst" : "Utgift";
        }

        // Skapar min transaktion och skickar till FinanceManager
        Transaction transaction = new Transaction(date, amount, description);
        financeManager.addTransaction(transaction);
    }

    // Tar bort en transaktion
    private void removeTransaction() {
        System.out.println("\n=== Ta bort transaktion ===");

        if (financeManager.getTransactionCount() == 0) {
            System.out.println("Inga transaktioner att ta bort.");
            return;
        }

        financeManager.printAllTransactions();
        int index = getIntInput(
                "Ange index för transaktion att ta bort (eller 'exit' för att avbryta): "
        );
        if (index == -1) return;

// Hämta alla transaktioner
        var all = financeManager.getAllTransactions();

        if (index < 0 || index >= all.size()) {
            System.out.println("Ogiltigt index.");
            return;
        }

// Hämta rätt Transaction och ta bort via ID
        Transaction selected = all.get(index);
        financeManager.removeTransaction(selected);

        System.out.println("Transaktion borttagen.");

    }

    // Visar nuvarande kontobalans
    private void showBalance() {
        double balance = financeManager.getBalance();
        System.out.println("\n=== Kontobalans ===");
        System.out.printf("Nuvarande balans: %.2f kr%n", balance);

        if (balance > 0) {
            System.out.println("Du har ett positivt saldo! 💰");
        } else if (balance < 0) {
            System.out.println("Du har ett negativt saldo. ⚠️");
        } else {
            System.out.println("Du har noll i saldo.");
        }
    }

    // Visar alla transaktioner
    private void showAllTransactions() {
        financeManager.printAllTransactions();
    }

    // Visar rapporter för olika tidsperioder
    private void showReports() {
        System.out.println("\n=== RAPPORTER ===");
        System.out.println("1. Årsrapport");
        System.out.println("2. Månadsrapport");
        System.out.println("3. Veckorapport");
        System.out.println("4. Dagsrapport");

        int choice = getIntInput("Välj rapport (1-4): ");
        if (choice == -1) return;

        switch (choice) {
            case 1 -> showYearlyReport();
            case 2 -> showMonthlyReport();
            case 3 -> showWeeklyReport();
            case 4 -> showDailyReport();
            default -> System.out.println("Ogiltigt val.");
        }
    }

    // === Rapporter ===

    private void showYearlyReport() {
        int year = getIntInput("Ange år (t.ex. 2024): ");
        if (year == -1) return;

        double income = financeManager.getYearlyIncome(year);
        double expenses = financeManager.getYearlyExpenses(year);

        System.out.println("\n=== Årsrapport för " + year + " ===");
        System.out.printf("Total inkomst: %.2f kr%n", income);
        System.out.printf("Totala utgifter: %.2f kr%n", expenses);
        System.out.printf("Netto: %.2f kr%n", income - expenses);
    }

    private void showMonthlyReport() {
        int year = getIntInput("Ange år (t.ex. 2024): ");
        if (year == -1) return;
        int month = getIntInput("Ange månad (1-12): ");
        if (month == -1) return;

        if (month < 1 || month > 12) {
            System.out.println("Ogiltig månad.");
            return;
        }

        double income = financeManager.getMonthlyIncome(year, month);
        double expenses = financeManager.getMonthlyExpenses(year, month);

        System.out.println("\n=== Månadsrapport för " + year + "-" + String.format("%02d", month) + " ===");
        System.out.printf("Total inkomst: %.2f kr%n", income);
        System.out.printf("Totala utgifter: %.2f kr%n", expenses);
        System.out.printf("Netto: %.2f kr%n", income - expenses);
    }

    private void showWeeklyReport() {
        int year = getIntInput("Ange år (t.ex. 2024): ");
        if (year == -1) return;
        int week = getIntInput("Ange vecka (1-53): ");
        if (week == -1) return;

        if (week < 1 || week > 53) {
            System.out.println("Ogiltig vecka.");
            return;
        }

        double income = financeManager.getWeeklyIncome(year, week);
        double expenses = financeManager.getWeeklyExpenses(year, week);

        System.out.println("\n=== Veckorapport för vecka " + week + ", " + year + " ===");
        System.out.printf("Total inkomst: %.2f kr%n", income);
        System.out.printf("Totala utgifter: %.2f kr%n", expenses);
        System.out.printf("Netto: %.2f kr%n", income - expenses);
    }

    private void showDailyReport() {
        LocalDate date = getDateInput("Ange datum (YYYY-MM-DD eller YYYYMMDD): ");
        if (date == null) return;

        double income = financeManager.getDailyIncome(date);
        double expenses = financeManager.getDailyExpenses(date);

        System.out.println("\n=== Dagsrapport för " + date + " ===");
        System.out.printf("Total inkomst: %.2f kr%n", income);
        System.out.printf("Totala utgifter: %.2f kr%n", expenses);
        System.out.printf("Netto: %.2f kr%n", income - expenses);
    }

    // === INPUT-METODER ===

    private int getIntInput(String prompt) {
        int value = -1;
        System.out.print(prompt);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Avbryter...");
                return -1; // exit signal
            }

            try {
                value = Integer.parseInt(input);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ange ett giltigt heltal.");
                System.out.print(prompt);
            }
        }
        return value;
    }

    private double getDoubleInput(String prompt) {
        double value = -1;
        System.out.print(prompt);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Avbryter...");
                return Double.MIN_VALUE; // exit signal
            }

            try {
                value = Double.parseDouble(input);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ange ett giltigt tal (t.ex. 123.45).");
                System.out.print(prompt);
            }
        }
        return value;
    }

    private LocalDate getDateInput(String prompt) {
        LocalDate date = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[yyyy-MM-dd][yyyyMMdd]");

        System.out.print(prompt);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Avbryter...");
                return null;
            }

            if (input.isEmpty()) {
                date = LocalDate.now();
                break;
            }

            try {
                date = LocalDate.parse(input, formatter);
                if (date.isAfter(LocalDate.now())) {
                    System.out.println("Datum får inte vara i framtiden.");
                    System.out.print(prompt);
                    continue;
                }
                break;
            } catch (DateTimeParseException e) {
                System.out.println("Ange datum i format YYYY-MM-DD eller YYYYMMDD (t.ex. 2024-03-15 eller 20240315).");
                System.out.print(prompt);
            }
        }
        return date;
    }

    // Main-metoden som startar programmet (terminal-versionen)
    public static void main(String[] args) {
        PersonalFinanceApp app = new PersonalFinanceApp();
        app.start();
    }
}