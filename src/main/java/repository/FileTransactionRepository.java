package repository;

import domain.Transaction;
import repository.storage.CsvDataStore;
import repository.storage.DataStore;
import repository.storage.LineConverter;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Min fil-baserade implementation av TransactionRepository.
 * Använder CsvDataStore för att läsa/skriva transaktioner till en CSV-fil.
 * Håller transaktionerna i minnet (en lista) och synkar mot fil vid behov.
 */
public class FileTransactionRepository implements TransactionRepository {

    // Min interna lista (cache i minnet)
    private final List<Transaction> transactions;

    // DataStore som hanterar själva fil-läsningen/skrivningen
    private final DataStore<Transaction> dataStore;

    /**
     * Skapar ett repository som använder en specifik fil.
     * @param filePath sökväg till CSV-filen (t.ex. "src/resources/transactions.csv")
     */
    public FileTransactionRepository(String filePath) {
        File file = new File(filePath);

        // Skapar en CsvDataStore med en converter som kan läsa/skriva Transaction
        this.dataStore = new CsvDataStore<>(file, new TransactionLineConverter());

        // Laddar in alla transaktioner från fil direkt vid start
        this.transactions = new ArrayList<>(dataStore.readAll());

        if (!transactions.isEmpty()) {
            System.out.println("Laddade " + transactions.size() + " transaktioner från fil.");
        }
    }

    /**
     * Sparar en transaktion (lägger till i listan och skriver till fil).
     */
    @Override
    public Transaction save(Transaction tx) {
        transactions.add(tx);
        dataStore.writeAll(transactions); // autosave
        return tx;
    }

    /**
     * Tar bort en transaktion på ett visst index.
     */
    @Override
    public boolean deleteByIndex(int index) {
        if (index >= 0 && index < transactions.size()) {
            transactions.remove(index);
            dataStore.writeAll(transactions); // autosave
            return true;
        }
        return false;
    }

    /**
     * Hämtar alla transaktioner.
     */
    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>(transactions); // returnerar en kopia så listan inte kan ändras utifrån
    }

    /**
     * Hittar transaktioner mellan två datum (inklusive).
     */
    @Override
    public List<Transaction> findByDateRange(LocalDate from, LocalDate to) {
        return transactions.stream()
                .filter(t -> !t.getDate().isBefore(from) && !t.getDate().isAfter(to))
                .collect(Collectors.toList());
    }

    /**
     * Antal transaktioner.
     */
    @Override
    public int count() {
        return transactions.size();
    }

    /**
     * Skriver ner hela listan till fil (används om jag vill tvinga en save).
     */
    @Override
    public void saveAll(List<Transaction> all) {
        transactions.clear();
        transactions.addAll(all);
        dataStore.writeAll(transactions);
    }

    // === Inre klass: Konverterar mellan Transaction och CSV-rad ===

    /**
     * Hjälper CsvDataStore att omvandla mellan Transaction-objekt och text-rader.
     * Format: "yyyy-MM-dd;belopp;beskrivning"
     */
    private static class TransactionLineConverter implements LineConverter<Transaction> {

        /**
         * Ska en rad text (t.ex. "2024-03-01;1000;Lön") till ett Transaction-objekt.
         */
        @Override
        public Transaction fromLine(String line) throws Exception {
            String[] parts = line.split(";", 3);
            if (parts.length != 3) {
                throw new IllegalArgumentException("Ogiltig rad (fel antal fält): " + line);
            }

            LocalDate date = LocalDate.parse(parts[0]);
            double amount = Double.parseDouble(parts[1]);
            String description = parts[2];

            return new Transaction(date, amount, description);
        }

        /**
         * Gör om ett Transaction-objekt till en CSV-rad.
         */
        @Override
        public String toLine(Transaction item) {
            return item.toFileFormat(); // använder Transaction's egen metod
        }
    }
}