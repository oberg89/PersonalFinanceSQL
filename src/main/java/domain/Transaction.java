package domain;

import java.time.LocalDate;

/**
 * Representerar en enskild transaktion
 * (datum, belopp och beskrivning). Används av både GUI och logik.
 */
public class Transaction {

    private final LocalDate date;       // Datum för transaktionen
    private final double amount;        // Belopp (positivt = inkomst, negativt = utgift)
    private final String description;   // Kort text om vad transaktionen gäller

    /**
     * Skapar en ny transaktion
     * @param date datum (får inte vara null)
     * @param amount belopp (positivt = inkomst, negativt = utgift)
     * @param description beskrivning (om null → ersätts med tom sträng)
     */
    public Transaction(LocalDate date, double amount, String description) {
        if (date == null) throw new IllegalArgumentException("Datum får inte vara null");
        if (description == null) description = "";

        // Sätter fält (trim för att slippa onödiga mellanslag)
        this.date = date;
        this.amount = amount;
        this.description = description.trim();
    }

    // Hämtar datum (används bl.a. av JavaFX TableView)
    public LocalDate getDate() {
        return date;
    }

    // Hämtar belopp (positivt = inkomst, negativt = utgift). Också för TableView.
    public double getAmount() {
        return amount;
    }

    // Hämtar beskrivning (visas i tabell och listor)
    public String getDescription() {
        return description;
    }

    // True om transaktionen är inkomst
    public boolean isIncome() {
        return amount > 0;
    }

    // True om transaktionen är utgift
    public boolean isExpense() {
        return amount < 0;
    }

    // Snygg utskrift för listor/terminal (behåller din gamla stil)
    @Override
    public String toString() {
        String type = isIncome() ? "Inkomst" : "Utgift";
        return String.format("%s | %.2f kr | %s | %s", date, Math.abs(amount), type, description);
    }

    // Export till fil (CSV-rad). Samma format som din tidigare kod.
    public String toFileFormat() {
        return date + ";" + amount + ";" + description;
    }
}