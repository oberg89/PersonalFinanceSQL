package repository.storage;

import java.util.List;

/**
 * En enkel "dataström" för att läsa/skriva en lista av objekt T.
 * Tanken: jag kan byta ut hur jag lagrar (CSV, JSON, DB, etc.)
 * utan att resten av koden bryr sig om detaljerna.
 */
public interface DataStore<T> {

    /**
     * Läser in alla poster från lagret.
     * @return en lista med objekt (kan vara tom, men aldrig null)
     */
    List<T> readAll();

    /**
     * Skriver över lagret med exakt den här listan.
     * @param items alla objekt som ska sparas
     */
    void writeAll(List<T> items);
}