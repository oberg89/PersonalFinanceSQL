package repository;

import domain.Transaction;
import repository.storage.CsvDataStore;
import repository.storage.DataStore;
import repository.storage.LineConverter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Min fil-baserade implementation av TransactionRepository.
 *
 * Jag vill att appen ska använda en stabil plats på disk (inte src/main/resources)
 * så att användardata överlever bygg/clean och fungerar både i IDE och med Maven.
 *
 * Standardplats: %USERPROFILE%/.personalfinance/transactions.csv (Windows)
 * Kan skickas in som filePath i konstruktorn (för test eller custom).
 */
public class FileTransactionRepository implements TransactionRepository {

    // Min interna lista (cache i minnet)
    private final List<Transaction> transactions;

    // DataStore som hanterar själva fil-läsningen/skrivningen
    private final DataStore<Transaction> dataStore;

    // Standardfil: i användarens hemkatalog under .personalfinance
    private static final Path DEFAULT_FOLDER = Paths.get(System.getProperty("user.home"), ".personalfinance");
    private static final Path DEFAULT_FILE = DEFAULT_FOLDER.resolve("transactions.csv");

    /**
     * Skapar ett repository som använder en specifik fil.
     * Om filePath är null eller tom används standardplatsen i användarens hemkatalog.
     *
     * @param filePath sökväg till CSV-filen (kan vara null för default)
     */
    public FileTransactionRepository(String filePath) {
        Path path;
        if (filePath == null || filePath.isBlank()) {
            path = DEFAULT_FILE;
        } else {
            path = Paths.get(filePath);
        }

        // Se till att katalogen finns
        try {
            Files.createDirectories(path.getParent());
        } catch (Exception e) {
            throw new IllegalStateException("Kunde inte skapa katalog för datafil: " + path.getParent(), e);
        }

        File file = path.toFile();

        // Skapar en CsvDataStore med en converter som kan läsa/skriva Transaction
        this.dataStore = new CsvDataStore<>(file, new TransactionLineConverter());

        // Laddar in alla transaktioner från fil direkt vid start
        this.transactions = new ArrayList<>(dataStore.readAll());

        if (!transactions.isEmpty()) {
            System.out.println("Laddade " + transactions.size() + " transaktioner från fil: " + file.getAbsolutePath());
        } else {
            System.out.println("Inga transaktioner funna. Fil: " + file.getAbsolutePath());
        }
    }

    /**
     * Enkel konstruktor som använder standardfil.
     */
    public FileTransactionRepository() {
        this(null);
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

    private static class TransactionLineConverter implements LineConverter<Transaction> {

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

        @Override
        public String toLine(Transaction item) {
            return item.toFileFormat();
        }
    }
}
