package ro.hoa.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLogger {
    private static final Path LOG_PATH = Path.of("data", "audit.log");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String currentUser = "SYSTEM";

    public AuditLogger() {
        try {
            Files.createDirectories(LOG_PATH.getParent());
            if (Files.notExists(LOG_PATH)) {
                Files.createFile(LOG_PATH);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setCurrentUser(String user) {
        this.currentUser = user;
    }


    public String getCurrentUser() {
        return currentUser;
    }

    public void log(String message) {
        String line = "[" + LocalDateTime.now().format(FMT) + "] [User: " + currentUser + "] " + message + System.lineSeparator();

        try {
            Files.writeString(LOG_PATH, line, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readAll() {
        try {
            return Files.readString(LOG_PATH, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "Nu am putut citi audit.log: " + e.getMessage();
        }
    }
}