package ro.hoa.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ro.hoa.repository.JsonRepository;
import ro.hoa.repository.Storage;
import ro.hoa.service.AsociatieService;

import javax.swing.*;
import java.io.IOException;

public class Main extends Application {

    private JsonRepository repository;
    private Storage storage;
    private AuditLogger auditLogger;

    @Override
    public void start(Stage primaryStage) {
        // 1. Initializam straturile de date
        repository = new JsonRepository();
        storage = loadStorage(repository);
        auditLogger = new AuditLogger();

        LoginView loginView = new LoginView(primaryStage, storage, auditLogger, () -> {
            launchMainApp(primaryStage);
        });


        loginView.show();
    }

    // Aceasta metoda porneste aplicatia principala
    private void launchMainApp(Stage primaryStage) {
        try {
            AsociatieService service = new AsociatieService(storage);
            FxMainView view = new FxMainView(service, auditLogger);

            // Schimbam scena de pe Stage-ul existent
            Scene scene = new Scene(view.getRoot(), 1150, 780);
            primaryStage.setTitle("HOA - Administrare Asociație");
            primaryStage.setScene(scene);

            primaryStage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Eroare la lansarea aplicației: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (repository != null && storage != null) {
            try {
                repository.save(storage);
                if (auditLogger != null) {
                    auditLogger.log("Aplicația s-a închis.");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "EROARE la salvarea datelor: " + ex.getMessage(),
                        "HOA - Salvare eșuată",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private static Storage loadStorage(JsonRepository repository) {
        try {
            return repository.load();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Eroare la încărcarea datelor (fișiere negăsite?). Se pornește cu date goale.\n" +
                            "Detalii: " + e.getMessage(),
                    "HOA - Eroare la pornire",
                    JOptionPane.WARNING_MESSAGE
            );
            return new Storage();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}