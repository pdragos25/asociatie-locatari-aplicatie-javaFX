package ro.hoa.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ro.hoa.model.Apartament;
import ro.hoa.model.Locatar;
import ro.hoa.model.FacturaLunara;
import ro.hoa.model.PlataApartament;
import ro.hoa.model.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Clasa pentru citire si scriere in JSON
public class JsonRepository {
    private final ObjectMapper mapper;

    // Caile catre fisiere
    private final Path PATH_APARTAMENTE = Paths.get("data/apartamente.json");
    private final Path PATH_LOCATARI = Paths.get("data/locatari.json");
    private final Path PATH_FACTURI = Paths.get("data/facturi.json"); // NOU
    private final Path PATH_PLATI = Paths.get("data/plati.json"); // NOU
    private final Path PATH_USERS = Paths.get("data/users.json");

    public JsonRepository() {
        // Inițializează "unealta" Jackson
        mapper = new ObjectMapper();
        // Formateaza cu identare
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    // Încarcă datele din fișierele JSON în obiectul Storage.
    public Storage load() throws IOException {
        Storage storage = new Storage();

        // Încarcă apartamente
        if (Files.exists(PATH_APARTAMENTE)) {
            // Folosim TypeReference pentru a-i spune lui Jackson să citească o LISTĂ de Apartamente
            List<Apartament> apartamente = mapper.readValue(PATH_APARTAMENTE.toFile(), new TypeReference<>() {});
            storage.setApartamente(apartamente);
        } else {
            // Dacă fișierul nu există, începem cu o listă goală
            storage.setApartamente(new ArrayList<>());
        }

        // Încarcă locatari
        if (Files.exists(PATH_LOCATARI)) {
            List<Locatar> locatari = mapper.readValue(PATH_LOCATARI.toFile(), new TypeReference<>() {});
            storage.setLocatari(locatari);
        } else {
            storage.setLocatari(new ArrayList<>());
        }

        if (Files.exists(PATH_FACTURI)) {
            List<FacturaLunara> facturi = mapper.readValue(PATH_FACTURI.toFile(), new TypeReference<>() {});
            storage.setFacturi(facturi);
        } else {
            storage.setFacturi(new ArrayList<>());
        }

        if (Files.exists(PATH_PLATI)) {
            List<PlataApartament> plati = mapper.readValue(PATH_PLATI.toFile(), new TypeReference<>() {});
            storage.setPlati(plati);
        } else {
            storage.setPlati(new ArrayList<>());
        }

        if (Files.exists(PATH_USERS)) {
            List<User> users = mapper.readValue(PATH_USERS.toFile(), new TypeReference<>() {});
            storage.setUsers(users);
        } else {
            // Creeaza userii default daca nu exista
            List<User> defaultUsers = new ArrayList<>();
            defaultUsers.add(new User("admin1", "user1"));
            defaultUsers.add(new User("admin2", "user2"));
            storage.setUsers(defaultUsers);
        }

        return storage;
    }

    // Salvează datele curente din obiectul Storage în fișierele JSON pe disc.
    public void save(Storage storage) throws IOException {
        // Creaza folderul data daca nu exista
        Files.createDirectories(PATH_APARTAMENTE.getParent());
        // Salvează apartamentele
        mapper.writeValue(PATH_APARTAMENTE.toFile(), storage.getApartamente());
        // Salvează locatarii
        mapper.writeValue(PATH_LOCATARI.toFile(), storage.getLocatari());
        //Salveaza facturile
        mapper.writeValue(PATH_FACTURI.toFile(), storage.getFacturi());
        // Salveaza platile
        mapper.writeValue(PATH_PLATI.toFile(), storage.getPlati());
        //Salveaza useri
        mapper.writeValue(PATH_USERS.toFile(), storage.getUsers());

    }
}