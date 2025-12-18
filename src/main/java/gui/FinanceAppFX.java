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

// JavaFX-applikation som ansvarar för hela GUI:t (inloggning, menyer, vyer)
public class FinanceAppFX extends Application {

    // Hanterar all affärslogik: inloggning, transaktioner, balans m.m.
    private FinanceManager financeManager;
    // Tabell som visar alla transaktioner för inloggad användare
    private TableView<Transaction> transactionTable;
    // Visar aktuell balans för inloggad användare
    private Label balanceLabel;

    // Startpunkt för JavaFX-applikationen – bygger upp hela gränssnittet
    @Override
    public void start(Stage primaryStage) {
        financeManager = new FinanceManager();

        // Visa login-dialog (avslutar endast om Cancel)
        if (!showLoginDialog()) {
            primaryStage.close();
            return;
        }

        primaryStage.setTitle("Personal Finance App");

        // Vänster panel som innehåller alla menyknappar
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

        Button btnLogout = new Button("Logga ut");
        btnLogout.setPrefWidth(180);
        btnLogout.setOnAction(e -> {
            financeManager.logout();

            if (!showLoginDialog()) {
                primaryStage.close();
                return;
            }
                refreshTable();
                updateBalanceLabel();

        });

        Button btnExit = new Button("Logga ut och avsluta");
        btnExit.setPrefWidth(180);
        btnExit.setOnAction(e -> {
            financeManager.logout();
            primaryStage.close();
        });

        balanceLabel = new Label();
        updateBalanceLabel();

        leftPanel.getChildren().addAll(
                new Label("=== MENY ==="),
                btnAdd, btnRemove, btnRefresh, btnReports, btnLogout, btnExit,
                new Separator(),
                balanceLabel
        );

        // Tabell som visar transaktioner för inloggad användare
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

        // Huvudlayout där meny ligger till vänster och innehåll i mitten
        BorderPane root = new BorderPane();
        root.setLeft(leftPanel);
        root.setCenter(transactionTable);

        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();
    }

    /**
     * Login / register-dialog.
     * Returnerar false ENDAST om användaren trycker Cancel.
     */
    private boolean showLoginDialog() {

        while (!financeManager.isAuthenticated()) {

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Logga in");
            dialog.setHeaderText("Logga in eller registrera ny användare");

            ButtonType loginBtn = new ButtonType("Logga in", ButtonBar.ButtonData.OK_DONE);
            ButtonType registerBtn = new ButtonType("Registrera", ButtonBar.ButtonData.OTHER);
            dialog.getDialogPane().getButtonTypes()
                    .addAll(loginBtn, registerBtn, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            TextField usernameField = new TextField();
            PasswordField passwordField = new PasswordField();

            grid.add(new Label("Användarnamn:"), 0, 0);
            grid.add(usernameField, 1, 0);
            grid.add(new Label("Lösenord:"), 0, 1);
            grid.add(passwordField, 1, 1);

            dialog.getDialogPane().setContent(grid);

            ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);

            if (result == ButtonType.CANCEL) {
                return false;
            }

            String u = usernameField.getText().trim();
            String p = passwordField.getText();

            if (u.isEmpty() || p.isEmpty()) {
                showAlert("Användarnamn och lösenord krävs.", Alert.AlertType.WARNING);
                continue;
            }

            if (result == loginBtn) {
                if (!financeManager.login(u, p)) {
                    showAlert("Felaktigt användarnamn eller lösenord.", Alert.AlertType.ERROR);
                }
            }

            if (result == registerBtn) {
                if (financeManager.register(u, p) != null) {
                    showAlert("Registrering lyckades. Logga in nu.", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Registrering misslyckades.", Alert.AlertType.ERROR);
                }
            }
        }
        return true;
    }


    // Dialog för att skapa och lägga till en ny transaktion
    private void showAddTransactionDialog() {
        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Lägg till transaktion");

        ButtonType addBtn = new ButtonType("Lägg till", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        DatePicker date = new DatePicker(LocalDate.now());
        TextField amount = new TextField();
        TextField desc = new TextField();

        grid.add(new Label("Datum:"), 0, 0);
        grid.add(date, 1, 0);
        grid.add(new Label("Belopp:"), 0, 1);
        grid.add(amount, 1, 1);
        grid.add(new Label("Beskrivning:"), 0, 2);
        grid.add(desc, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == addBtn) {
                try {
                    return new Transaction(
                            date.getValue(),
                            Double.parseDouble(amount.getText()),
                            desc.getText()
                    );
                } catch (Exception e) {
                    showAlert("Felaktig inmatning.", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(t -> {
            financeManager.addTransaction(t);
            refreshTable();
        });
    }

    // Tar bort den transaktion som användaren markerat i tabellen
    private void removeSelectedTransaction() {
        Transaction t = transactionTable.getSelectionModel().getSelectedItem();
        if (t == null) return;
        financeManager.removeTransaction(
                financeManager.getAllTransactions().indexOf(t)
        );
        refreshTable();
    }


    // Hämtar alla transaktioner och uppdaterar tabellen och balansen
    private void refreshTable() {
        ObservableList<Transaction> data =
                FXCollections.observableArrayList(financeManager.getAllTransactions());
        transactionTable.setItems(data);
        updateBalanceLabel();
    }

    // Uppdaterar och visar aktuell balans för inloggad användare
    private void updateBalanceLabel() {
        balanceLabel.setText(
                String.format("Balans: %.2f kr", financeManager.getBalance())
        );
    }

    // Visar rapportdialog (inkomster/utgifter per tidsperiod)
    private void showReportsDialog() {
        showAlert("Rapporter ej implementerat här.", Alert.AlertType.INFORMATION);
    }

    // Visar ett informations-, varnings- eller felmeddelande
    private void showAlert(String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setContentText(msg);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
