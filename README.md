PersonalFinance (OOP + Repository Edition)
Ett vidareutvecklat personal finance‑program skrivet i Java, byggt som en skoluppgift men strukturerat enligt riktig OOP‑design och Repository‑mönster.
Programmet använder JavaFX för sitt grafiska gränssnitt, och sparar data till filer på ett objektorienterat sätt.

✨ Funktioner
Lägg till transaktioner (inkomst eller utgift)
Ta bort transaktioner
Visa nuvarande kontobalans
Visa rapporter:
Årsvis
Månadsvis
Veckovis
Dagsvis
Automatisk sparning till fil (src/resources/transactions.csv)
Data läses in direkt vid programstart
Förberett för framtida databashantering
🧠 System & OOP‑Design (VG‑nivå)
Den tidigare filhanteringen har nu byggts om till ett Repository‑lager
som gör systemet fullständigt objektorienterat och redo för databasimplementation i framtiden.

🧩 Layers (skiktad arkitektur)
text
Copy
GUI (JavaFX)        →  FinanceManager (Service)  →  TransactionRepository (Interface)
                                               ↳  FileTransactionRepository (Implementation)
                                                  ↳  CsvDataStore (Filhantering)
                                                        ↳  transactions.csv
📦 Paketstruktur
Paket	Innehåll
gui	JavaFX‑gränssnitt (FinanceAppFX.java)
domain	Domänmodell (Transaction.java)
service	Affärslogik (FinanceManager.java)
repository	Interface för lagring + implementationer
repository.storage	Generisk filhantering (DataStore, CsvDataStore, LineConverter)
resources	CSV‑filen (transactions.csv)
⚙️ Klassöversikt
Transaction.java
Representerar en transaktion.
Varje post innehåller datum, belopp och beskrivning.
Har hjälpfunktioner som toFileFormat() (till CSV‑rad) och logiska metoder som isIncome() / isExpense().

FinanceManager.java
Ansvarar för all logik runt transaktioner, balans och rapporter.
Kommunicerar enbart via TransactionRepository.
Innehåller inga direkta filoperationer längre.

TransactionRepository.java
Interface (abstraktion) som definierar kontrakt för all lagring:


public interface TransactionRepository {
   Transaction save(Transaction tx);
   boolean deleteByIndex(int index);
   List<Transaction> findAll();
   int count();
}
FileTransactionRepository.java
Filbaserad implementation av TransactionRepository.
Sparar data i CSV‑format med hjälp av CsvDataStore.

CsvDataStore.java
Generisk klass för att läsa/skriva listor av objekt till en textfil.
Jobbar tillsammans med LineConverter<T> som översätter mellan objekt och text.

FinanceAppFX.java
JavaFX‑GUI som pratar med FinanceManager.
Visar tabell, balans, knappar och rapportfönster.

🧱 OOP‑principer som uppfylls
Princip	Hur den används
Abstraktion	TransactionRepository & DataStore visar vad som ska göras – inte hur.
Inkapsling	Filhantering dold i FileTransactionRepository och CsvDataStore.
Arv	Transaction fungerar som en basklass, redo för subklasser (t.ex. IncomeTransaction).
Polymorfism	Samma interface kan ha flera implementationer (fil, minne, databas).
Separation of Concerns	GUI ↔ Service ↔ Repository ↔ Storage – tydligt separerade lager.
💾 Repository Pattern
Tidigare låg filhanteringen direkt i FinanceManager.
Nu används repository‑mönstret enligt OOP:


TransactionRepository repo = new FileTransactionRepository("src/resources/transactions.csv");
FinanceManager manager = new FinanceManager(repo);
Vill du växla till databas i framtiden?


TransactionRepository repo = new DatabaseTransactionRepository();  // Samma interface!
👉 Ingen annan kod i appen behöver ändras – tack vare polymorfism.

🗃️ Framtida utveckling (Databasförberedelse)
Strukturen är redan klar för att spara i databas i nästa kursmodul.
Det enda du behöver göra är att skapa:


public class DatabaseTransactionRepository implements TransactionRepository {
   // Implementera med JDBC eller JPA
}
FinanceManager och GUI:t fortsätter fungera oförändrat 🎯

🏗️ UML‑översikt (klassdiagram)

                 ┌────────────────────────┐
                 │      Transaction       │
                 │────────────────────────│
                 │ - date : LocalDate     │
                 │ - amount : double      │
                 │ - description : String │
                 │────────────────────────│
                 │ + toFileFormat()       │
                 │ + isIncome()           │
                 │ + isExpense()          │
                 └────────▲───────────────┘
                          │
                          │ används av
                          │
          ┌───────────────────────────────────┐
          │          FinanceManager           │
          │───────────────────────────────────│
          │ - repository : TransactionRepo... │
          │───────────────────────────────────│
          │ + addTransaction()                │
          │ + removeTransaction()             │
          │ + getBalance()                    │
          │ + getReports()                    │
          └───────▲───────────────────────────┘
                  │ använder
                  │
     ┌────────────────────────────────────────────┐
     │           TransactionRepository             │
     │────────────────────────────────────────────│
     │ + save(tx)                                 │
     │ + deleteByIndex(i)                         │
     │ + findAll()                                │
     │ + count()                                  │
     └──────▲─────────────┬───────────────────────┘
            │              │
            │ implements   │ implements
            │              │
 ┌──────────────────┐      ┌───────────────────────┐
 │ FileTransaction  │      │ DatabaseTransaction   │
 │ Repository       │      │ Repository (framtida) │
 │──────────────────│      │───────────────────────│
 │ - dataStore      │      │ - JDBC / SQL logik    │
 └────────▲──────────┘      └───────────────────────┘
          │ använder
          │
 ┌──────────────────────────────────────────────────┐
 │                 CsvDataStore<T>                   │
 │──────────────────────────────────────────────────│
 │ - file : File                                    │
 │ - converter : LineConverter<T>                   │
 │──────────────────────────────────────────────────│
 │ + readAll()                                      │
 │ + writeAll()                                     │
 └──────────────────────────────────────────────────┘
🖥️ Så kör du
1. Klona projektet

git clone https://github.com/oberg89/PersonalFinance.git
2. Lägg till JavaFX‑bibliotek i IntelliJ
Project Structure → Libraries → Lägg till din javafx-sdk/lib‑mapp.

3. VM options (Run → Edit Configurations)

--module-path /din/sökväg/till/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
4. Kör
Starta gui.FinanceAppFX

Data sparas automatiskt i:

src/resources/transactions.csv
🧑‍💻 Författare
Stefan Öberg
Refaktorering och OOP‑utveckling 


📄 Licens
MIT License