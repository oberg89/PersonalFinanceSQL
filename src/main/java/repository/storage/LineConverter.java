package repository.storage;

/**
 * Hjälper mig att konvertera mellan en text-rad (t.ex. CSV)
 * och ett objekt (T). Används av CsvDataStore.
 */
public interface LineConverter<T> {
    /**
     * Skapar ett objekt från en rad text (t.ex. "2024-03-01;1000;Lön").
     */
    T fromLine(String line) throws Exception;

    /**
     * Gör om ett objekt till en text-rad (t.ex. CSV-format).
     */
    String toLine(T item);
}