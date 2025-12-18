package repository;

import domain.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Gränssnitt för att lagra och hämta transaktioner.
 * (Abstraktion – jag bestämmer VAD som ska göras, inte HUR.)
 * Senare kan jag byta implementation (fil, databas, minne) utan att GUI/service behöver ändras.
 */
public interface TransactionRepository {

    /**
     * Sparar en transaktion (lägger till i lagret).
     * @param tx transaktionen jag vill spara
     * @return samma transaktion (ev. med genererat id i andra implementationer)
     */
    Transaction save(Transaction tx);

    /**
     * Tar bort en transaktion på en viss position/index (fil-implementationen speglar min lista).
     * @param index position i listan (0-baserat)
     * @return true om något togs bort, annars false
     */
    boolean deleteByIndex(int index);

    /**
     * Hämtar alla transaktioner (i den ordning de finns lagrade).
     * @return lista med transaktioner
     */
    List<Transaction> findAll();

    /**
     * Hittar transaktioner mellan två datum (inklusive).
     * @param from startdatum
     * @param to slutdatum
     * @return lista med transaktioner i intervallet
     */
    List<Transaction> findByDateRange(LocalDate from, LocalDate to);

    /**
     * För framtidsbruk (om jag inför id): hitta en transaktion utifrån nyckel.
     * Här lämnar den valfri – fil-varianten använder inte id just nu.
     */
    default Optional<Transaction> findById(String id) {
        return Optional.empty();
    }

    /**
     * Antal transaktioner i lagret.
     */
    int count();

    /**
     * Skriver ner hela nuvarande listan till permanent lagring.
     * (I fil-implementationen → skriv till CSV.)
     */
    void saveAll(List<Transaction> all);
}