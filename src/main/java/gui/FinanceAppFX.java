package gui;

import service.FinanceManager;
import domain.Transaction;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * JavaFX-GUI för min Personal Finance-app.
 * Jag visar först en enkel login-dialog och fortsätter endast om inloggning lyckas.
 */
public class FinanceAppFX extends Application {

    private FinanceManager financeManager;
    private TableView<Transaction> transactionTable;
    private Label balanceLabel;

    @Override
    public void start(Stage primaryStage) {
        financeManager = new FinanceManager();

        // --- Enkel login-dialog före resten ---
        boolean loggedIn = showLoginDialog();
        if (!loggedIn) {
            // Avsluta appen om användaren inte loggar in
            primaryStage.close();
            return;
        }

        primaryStage.setTitle("Personal Finance App");

        VBox leftPanel = new VBox(15);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setPrefWidth(200);

        Button btnAdd = new Button("Lägg till transaktion");
        btnAdd.setPrefWidth(180);
        btnAdd.setOnAction(e -> showAddTransactionDialog());

        Button btnRemove = new Button("Ta bort transaktion");
        btnRemove.setPrefWidth(180);
        btnRemove.setOnAction(e -> removeSelectedTransaction());

        Button btnRefresh = new Button("Uppdatera lista");
        btnRefresh.setPrefWidth(180);
        btnRefresh.setOnAction(e -> refreshTable());

        Button btnReports = new Button("Visa rapporter");
        btnReports.setPrefWidth(180);
        btnReports.setOnAction(e -> showReportsDialog());

        Button btnSave = new Button("Spara data");
        btnSave.setPrefWidth(180);
        btnSave.setOnAction(e -> {
            financeManager.saveToFile();
            showAlert("Data sparad!", Alert.AlertType.INFORMATION);
        });

        Button btnExit = new Button("Logga ut och avsluta");
        btnExit.setPrefWidth(180);
        btnExit.setOnAction(e -> {
            financeManager.logout();
            primaryStage.close();
        });

        balanceLabel = new Label();
        balanceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        updateBalanceLabel();

        leftPanel.getChildren().addAll(
                new Label("=== MENY ==="),
                btnAdd, btnRemove, btnRefresh, btnReports, btnSave, btnExit,
                new Separator(),
                balanceLabel
        );

        transactionTable = new TableView<>();
        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Datum");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Belopp");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<Transaction, String> descCol = new TableColumn<>("Beskrivning");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        transactionTable.getColumns().addAll(dateCol, amountCol, descCol);
        refreshTable();

        BorderPane root = new BorderPane();
        root.setLeft(leftPanel);
        root.setCenter(transactionTable);

        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean showLoginDialog() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Logga in");
        dialog.setHeaderText("Ange användarnamn och lösenord");

        ButtonType loginButtonType = new ButtonType("Logga in", ButtonBar.ButtonData.OK_DONE);
        ButtonType registerButtonType = new ButtonType("Registrera", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, registerButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Användarnamn");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Lösenord");

        grid.add(new Label("Användarnamn:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Lösenord:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == loginButtonType) {
                String u = usernameField.getText().trim();
                String p = passwordField.getText();
                if (financeManager.login(u, p)) {
                    return true;
                } else {
                    showAlert("Inloggning misslyckades.", Alert.AlertType.ERROR);
                }
            } else if (btn == registerButtonType) {
                String u = usernameField.getText().trim();
                String p = passwordField.getText();
                if (u.isEmpty() || p.isEmpty()) {
                    showAlert("Ange användarnamn och lösenord för registrering.", Alert.AlertType.WARNING);
                } else {
                    var user = financeManager.register(u, p);
                    if (user != null) {
                        showAlert("Registrering lyckades. Logga in nu.", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Registrering misslyckades.", Alert.AlertType.ERROR);
                    }
                }
            }
            return null;
        });

        dialog.showAndWait();
        return financeManager.isAuthenticated();
    }

    // resten av dina metoder (showAddTransactionDialog, removeSelectedTransaction, refreshTable, updateBalanceLabel, showReportsDialog, showAlert)
    // ... (samma kod som du redan skickade, ingen förändring behövs bortsett från att financeManager nu kräver inloggning)
    private void showAddTransactionDialog() {
        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Lägg till transaktion");
        dialog.setHeaderText("Fyll i transaktionsuppgifter");

        ButtonType addButtonType = new ButtonType("Lägg till", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setEditable(true);
        TextField amountField = new TextField();
        amountField.setPromptText("Belopp (+ inkomst, - utgift)");
        TextField descField = new TextField();
        descField.setPromptText("Beskrivning");

        grid.add(new Label("Datum:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Belopp:"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label("Beskrivning:"), 0, 2);
        grid.add(descField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    LocalDate date = datePicker.getValue();
                    double amount = Double.parseDouble(amountField.getText().trim());
                    String desc = descField.getText().trim();
                    if (desc.isEmpty()) {
                        desc = amount > 0 ? "Inkomst" : "Utgift";
                    }
                    return new Transaction(date, amount, desc);
                } catch (NumberFormatException e) {
                    showAlert("Ogiltigt belopp!", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(transaction -> {
            financeManager.addTransaction(transaction);
            refreshTable();
            updateBalanceLabel();
        });
    }

    private void removeSelectedTransaction() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Välj en transaktion att ta bort!", Alert.AlertType.WARNING);
            return;
        }
        int index = financeManager.getAllTransactions().indexOf(selected);
        financeManager.removeTransaction(index);
        refreshTable();
        updateBalanceLabel();
    }

    private void refreshTable() {
        ObservableList<Transaction> data =
                FXCollections.observableArrayList(financeManager.getAllTransactions());
        transactionTable.setItems(data);
        updateBalanceLabel();
    }

    private void updateBalanceLabel() {
        double balance = financeManager.getBalance();
        balanceLabel.setText(String.format("Balans: %.2f kr", balance));
        if (balance > 0) {
            balanceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green;");
        } else if (balance < 0) {
            balanceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: red;");
        } else {
            balanceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");
        }
    }

    private void showReportsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Rapporter");
        dialog.setHeaderText("Välj rapporttyp och datumintervall");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("År", "Månad", "Vecka", "Dag");
        typeBox.setValue("År");

        TextField yearField = new TextField(String.valueOf(LocalDate.now().getYear()));
        TextField monthField = new TextField();
        TextField weekField = new TextField();
        DatePicker dayPicker = new DatePicker(LocalDate.now());

        grid.add(new Label("Rapporttyp:"), 0, 0);
        grid.add(typeBox, 1, 0);
        grid.add(new Label("År:"), 0, 1);
        grid.add(yearField, 1, 1);
        grid.add(new Label("Månad (1-12):"), 0, 2);
        grid.add(monthField, 1, 2);
        grid.add(new Label("Vecka (1-52):"), 0, 3);
        grid.add(weekField, 1, 3);
        grid.add(new Label("Dag:"), 0, 4);
        grid.add(dayPicker, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    String type = typeBox.getValue();
                    int year = Integer.parseInt(yearField.getText().trim());

                    switch (type) {
                        case "År" -> {
                            double inc = financeManager.getYearlyIncome(year);
                            double exp = financeManager.getYearlyExpenses(year);
                            showAlert(
                                    String.format("=== Årsrapport %d ===\nInkomst: %.2f kr\nUtgifter: %.2f kr\nNetto: %.2f kr",
                                            year, inc, exp, inc - exp),
                                    Alert.AlertType.INFORMATION
                            );
                        }
                        case "Månad" -> {
                            int month = Integer.parseInt(monthField.getText().trim());
                            double inc = financeManager.getMonthlyIncome(year, month);
                            double exp = financeManager.getMonthlyExpenses(year, month);
                            showAlert(
                                    String.format("=== Månadsrapport %d-%02d ===\nInkomst: %.2f kr\nUtgifter: %.2f kr\nNetto: %.2f kr",
                                            year, month, inc, exp, inc - exp),
                                    Alert.AlertType.INFORMATION
                            );
                        }
                        case "Vecka" -> {
                            int week = Integer.parseInt(weekField.getText().trim());
                            double inc = financeManager.getWeeklyIncome(year, week);
                            double exp = financeManager.getWeeklyExpenses(year, week);
                            showAlert(
                                    String.format("=== Veckorapport %d - vecka %d ===\nInkomst: %.2f kr\nUtgifter: %.2f kr\nNetto: %.2f kr",
                                            year, week, inc, exp, inc - exp),
                                    Alert.AlertType.INFORMATION
                            );
                        }
                        case "Dag" -> {
                            LocalDate date = dayPicker.getValue();
                            double inc = financeManager.getDailyIncome(date);
                            double exp = financeManager.getDailyExpenses(date);
                            showAlert(
                                    String.format("=== Dagsrapport %s ===\nInkomst: %.2f kr\nUtgifter: %.2f kr\nNetto: %.2f kr",
                                            date, inc, exp, inc - exp),
                                    Alert.AlertType.INFORMATION
                            );
                        }
                    }
                } catch (Exception e) {
                    showAlert("Felaktig inmatning! Kontrollera att du skrivit rätt år/månad/vecka.", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
