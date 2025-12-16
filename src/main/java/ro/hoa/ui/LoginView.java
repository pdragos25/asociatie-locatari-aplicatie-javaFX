package ro.hoa.ui;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ro.hoa.model.User;
import ro.hoa.repository.Storage;

import java.util.Optional;

public class LoginView {

    private final Stage stage;
    private final Storage storage;
    private final AuditLogger auditLogger;
    private final Runnable onLoginSuccess;

    public LoginView(Stage stage, Storage storage, AuditLogger auditLogger, Runnable onLoginSuccess) {
        this.stage = stage;
        this.storage = storage;
        this.auditLogger = auditLogger;
        this.onLoginSuccess = onLoginSuccess;
    }

    public void show() {
        // Panoul principal care tine cele doua jumatati
        HBox root = new HBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        VBox loginPanel = createLoginPanel();

        Separator separator = new Separator();
        separator.setOrientation(Orientation.VERTICAL);

        VBox changePassPanel = createChangePassPanel();

        root.getChildren().addAll(loginPanel, separator, changePassPanel);

        // Setam scena
        Scene scene = new Scene(root, 650, 400);
        stage.setTitle("Acces Aplicație HOA");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private VBox createLoginPanel() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(280);

        Label title = new Label("Autentificare");
        title.setFont(new Font(18));
        title.setStyle("-fx-font-weight: bold");

        TextField userField = new TextField();
        userField.setPromptText("Utilizator (ex: admin1)");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Parola");

        Button loginBtn = new Button("Intră în aplicație");
        loginBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        loginBtn.setDefaultButton(true); // Se activeaza la Enter

        Label msgLabel = new Label();
        msgLabel.setStyle("-fx-text-fill: red;");

        loginBtn.setOnAction(e -> {
            String u = userField.getText().trim();
            String p = passField.getText().trim();

            // Cautam utilizatorul in lista
            Optional<User> found = storage.getUsers().stream()
                    .filter(user -> user.getUsername().equals(u) && user.getPassword().equals(p))
                    .findFirst();

            if (found.isPresent()) {
                // Succes
                auditLogger.setCurrentUser(u);
                auditLogger.log("Autentificare reușită.");
                onLoginSuccess.run();
            } else {
                // Eșec
                auditLogger.setCurrentUser("UNKNOWN"); // Resetam userul
                auditLogger.log("Incercare esuata de login pentru: " + u);
                msgLabel.setText("Date incorecte!");
            }
        });

        box.getChildren().addAll(title, new Label("User:"), userField, new Label("Parola:"), passField, loginBtn, msgLabel);
        return box;
    }

    private VBox createChangePassPanel() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefWidth(280);

        Label title = new Label("Schimbare Parolă");
        title.setFont(new Font(18));
        title.setStyle("-fx-font-weight: bold");

        TextField userField = new TextField();
        userField.setPromptText("Utilizator");

        PasswordField oldPassField = new PasswordField();
        oldPassField.setPromptText("Parola Veche");

        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("Parola Nouă");

        Button changeBtn = new Button("Actualizează Parola");
        Label msgLabel = new Label();

        changeBtn.setOnAction(e -> {
            String u = userField.getText().trim();
            String oldP = oldPassField.getText().trim();
            String newP = newPassField.getText().trim();

            if (newP.isEmpty()) {
                msgLabel.setText("Parola nouă nu poate fi goală.");
                msgLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            Optional<User> found = storage.getUsers().stream()
                    .filter(user -> user.getUsername().equals(u) && user.getPassword().equals(oldP))
                    .findFirst();

            if (found.isPresent()) {
                // Schimbam parola
                found.get().setPassword(newP);

                // Logam actiunea
                String oldUser = auditLogger.getCurrentUser();
                auditLogger.setCurrentUser(u);
                auditLogger.log("Utilizatorul și-a schimbat parola din ecranul de login.");
                auditLogger.setCurrentUser(oldUser);

                msgLabel.setText("Parolă schimbată cu succes!");
                msgLabel.setStyle("-fx-text-fill: green;");

                userField.clear(); oldPassField.clear(); newPassField.clear();
            } else {
                msgLabel.setText("User sau parola veche incorecte.");
                msgLabel.setStyle("-fx-text-fill: red;");
            }
        });

        //Buton pentru resetarea parolelor
        Separator sep = new Separator();
        Button resetBtn = new Button("RESETARE PAROLE");
        resetBtn.setStyle("-fx-background-color: #ffccccc; -fx-text-fill: red; -fx-border-color: red");
        resetBtn.setOnAction(e -> {
            // 1. Cerem confirmare
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmare Resetare");
            alert.setHeaderText("ATENȚIE: Această acțiune va șterge toate parolele!");
            alert.setContentText("Sigur vrei să revii la parolele implicite?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // 2. Stergem lista curenta
                storage.getUsers().clear();

                // 3. Adaugam userii default
                storage.getUsers().add(new User("admin1", "user1"));
                storage.getUsers().add(new User("admin2", "user2"));

                // 4. Logam actiunea
                auditLogger.setCurrentUser("SYSTEM");
                auditLogger.log("S-a efectuat RESETAREA TOTALĂ a parolelor la valorile implicite.");

                msgLabel.setText("Resetare efectuată!");
                msgLabel.setStyle("-fx-text-fill: blue;");
            }
        });

        box.getChildren().addAll(title,
                new Label("User:"), userField,
                new Label("Parola Veche:"), oldPassField,
                new Label("Parola Nouă:"), newPassField,
                changeBtn, msgLabel, sep, resetBtn
        );
        return box;
    }
}
