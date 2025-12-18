package repository.storage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Enkel implementation av DataStore som jobbar med CSV-liknande textfiler.
 * Den sparar varje objekt som en rad text, och använder en LineConverter
 * för att omvandla mellan objektet (T) och strängraden.
 */
public class CsvDataStore<T> implements DataStore<T> {

    private final File file;                   // Själva filen skriver till/läser från
    private final LineConverter<T> converter;  // Hanterar konvertering mellan objekt <-> text-rad

    /**
     * Skapar en CsvDataStore
     * @param file filen jag vill använda
     * @param converter omvandlar mellan objekt och text
     */
    public CsvDataStore(File file, LineConverter<T> converter) {
        this.file = file;
        this.converter = converter;

        // Se till att filen finns, slipper FileNotFoundException
        ensureFile();
    }

    // Ser till att filer/mappstruktur finns
    private void ensureFile() {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                if (parent.mkdirs()) {
                    System.out.println("Mapp skapad: " + parent.getAbsolutePath());
                }
            }

            if (!file.exists()) {
                if (file.createNewFile()) {
                    System.out.println("Fil skapad: " + file.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            System.out.println("Kunde inte skapa fil: " + e.getMessage());
        }
    }

    /**
     * Läser alla rader från filen och försöker omvandla till objekt.
     * Hoppar över rader som inte går att läsa.
     */
    @Override
    public List<T> readAll() {
        List<T> items = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                try {
                    T item = converter.fromLine(line);
                    items.add(item);
                } catch (Exception e) {
                    System.out.println("Kunde inte läsa rad: " + line);
                }
            }

        } catch (IOException e) {
            System.out.println("Fel vid läsning av fil: " + e.getMessage());
        }

        return items;
    }


    @Override
    public void writeAll(List<T> items) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {

            for (T item : items) {
                writer.write(converter.toLine(item));
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Fel vid skrivning till fil: " + e.getMessage());
        }
    }
}