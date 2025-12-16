# PersonalFinance (OOP + Repository Edition)

Ett vidareutvecklat **personal finance-program** skrivet i **Java**, byggt som en skoluppgift men strukturerat enligt **riktig OOP-design** och **Repository-mönstret**.  
Programmet använder **JavaFX** för sitt grafiska gränssnitt och är byggt för att köras via **Maven eller Gradle**, precis som i riktiga Java-projekt.

Applikationen använder **PostgreSQL** som datalager, men strukturen är byggd så att lagringslösningen enkelt kan bytas ut utan att resten av systemet påverkas.

---

## ✨ Funktioner

- Skapa användare och logga in  
- Lägg till transaktioner (inkomst eller utgift)  
- Ta bort transaktioner  
- Visa aktuell kontobalans  
- Visa rapporter:
  - Årsvis  
  - Månadsvis  
  - Veckovis  
  - Dagsvis  
- All data sparas i databas  
- Data läses in automatiskt vid programstart  
- Tydlig separation mellan GUI, affärslogik och datalager  

---

## 🧠 System & OOP-design

Systemet är uppbyggt enligt **skiktad arkitektur** där varje lager har ett tydligt ansvar.  
Den tidigare filbaserade lagringen har ersatts med ett **Repository-lager**, vilket gör systemet mer flexibelt och lätt att vidareutveckla.

---

## 🧩 Skiktad arkitektur

```
GUI (JavaFX)
   ↓
FinanceManager (Service / affärslogik)
   ↓
Repository-interfaces
   ↓
JDBC-implementation (PostgreSQL)
```

GUI:t kommunicerar endast med service-lagret, och service-lagret använder repository-interfaces för all datalagring.

---

## 📦 Paketstruktur

| Paket | Innehåll |
|------|---------|
| gui | JavaFX-gränssnitt (`FinanceAppFX.java`) |
| domain | Domänmodeller (`User`, `Transaction`) |
| service | Affärslogik (`FinanceManager.java`) |
| repository | Repository-interfaces |
| repository.jdbc | JDBC-implementationer |
| resources | Konfigurationsfiler & SQL-schema |

---

## ⚙️ Klassöversikt (urval)

### Transaction.java
Representerar en transaktion med datum, belopp och beskrivning.  
Innehåller hjälpfunktioner som `isIncome()` och `isExpense()`.

### FinanceManager.java
Ansvarar för all affärslogik:
- användare  
- transaktioner  
- balans  
- rapporter  

Kommunicerar endast via repository-interfaces.

### TransactionRepository.java
Interface som definierar kontraktet för transaktionslagring:

```java
public interface TransactionRepository {
    Transaction save(Transaction tx);
    boolean deleteByIndex(int index);
    List<Transaction> findAllForUser(int userId);
    int countForUser(int userId);
}
```

### JdbcTransactionRepository.java
JDBC-baserad implementation som lagrar data i PostgreSQL.  
All SQL-logik är isolerad till detta lager.

---

## 💾 Repository Pattern

All datalagring sker via repository-interfaces.

```java
TransactionRepository repo = new JdbcTransactionRepository();
FinanceManager manager = new FinanceManager(repo);
```

Byter jag lagringslösning i framtiden kan samma interface användas utan att resten av applikationen ändras.

---

## 🗃️ Databas

Applikationen använder **PostgreSQL**.  
Databasschemat finns definierat i `schema.sql` och innehåller tabeller för:

- users  
- transactions  

---

## 🖥️ Så kör jag projektet

### 🔹 Gradle 

Projektet innehåller **Gradle Wrapper**:

```bash
./gradlew run
```

Gradle hanterar JavaFX, beroenden och JVM-inställningar automatiskt.


---

## 🔧 Krav

- Java 21  
- PostgreSQL  
- Databas: `personalfinance`  

---

## 🧑‍💻 Författare

**Stefan Öberg**

---

## 📄 Licens

MIT License
