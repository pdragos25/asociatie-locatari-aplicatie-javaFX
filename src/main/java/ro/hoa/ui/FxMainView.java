package ro.hoa.ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import ro.hoa.model.Apartament;
import ro.hoa.model.Locatar;
import ro.hoa.model.PlataApartament;
import ro.hoa.service.AsociatieService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FxMainView {

    private final AsociatieService service;
    private final AuditLogger audit;
    private final BorderPane root = new BorderPane();

    private final TableView<Apartament> apartamenteTable = new TableView<>();
    private final TableView<Locatar> locatariTable = new TableView<>();
    private final TableView<PlataApartament> platiTable = new TableView<>();
    private final TextArea raportArea = new TextArea();

    private int lastLuna = -1;
    private int lastAn = -1;

    public FxMainView(AsociatieService service, AuditLogger auditLogger) {
        this.service = service;
        this.audit = auditLogger;
        root.setPadding(new Insets(12));

        TabPane tabs = new TabPane();
        tabs.getTabs().add(createTabApartamente());
        tabs.getTabs().add(createTabIntretinere());
        tabs.getTabs().add(createTabAudit());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        root.setCenter(tabs);
        configureTables();
        refreshApartamente();
        refreshLocatari();
        refreshRaport("Bine ai venit! Folosește tab-urile pentru operațiile de administrare.");
    }

    public Parent getRoot() {
        return root;
    }

    private Tab createTabApartamente() {
        VBox content = new VBox(12);
        content.setPadding(new Insets(12));

        Label title = sectionLabel("Apartamente & Locatari");

        VBox addApCard = wrapCard(buildAddApartamentForm(), "Adăugare apartament");
        VBox addLocCard = wrapCard(buildAddLocatarForm(), "Adăugare locatar");
        VBox searchCard = wrapCard(buildSearchFilterPanel(), "Căutări și filtre");

        HBox formsRow = new HBox(12, addApCard, addLocCard, searchCard);
        HBox.setHgrow(addApCard, Priority.ALWAYS);
        HBox.setHgrow(addLocCard, Priority.ALWAYS);
        HBox.setHgrow(searchCard, Priority.ALWAYS);
        formsRow.setAlignment(Pos.TOP_LEFT);

        VBox apartCard = wrapTable(apartamenteTable, "Apartamente");
        VBox locCard = wrapTable(locatariTable, "Locatari (sortați după nume)");
        HBox tablesRow = new HBox(12, apartCard, locCard);
        HBox.setHgrow(apartCard, Priority.ALWAYS);
        HBox.setHgrow(locCard, Priority.ALWAYS);
        tablesRow.setAlignment(Pos.TOP_LEFT);
        tablesRow.setPrefHeight(380);

        content.getChildren().addAll(title, formsRow, tablesRow);

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        Tab tab = new Tab("Apartamente");
        tab.setContent(sp);
        return tab;
    }

    private Tab createTabIntretinere() {
        VBox content = new VBox(12);
        content.setPadding(new Insets(12));

        Label title = sectionLabel("Întreținere & Plăți");

        VBox intCard = wrapCard(buildIntretinereForm(), "Generează întreținere");
        VBox platiLunaCard = wrapCard(buildPlatiLunaForm(), "Afișează plățile pe lună");
        HBox topRow = new HBox(12, intCard, platiLunaCard);
        HBox.setHgrow(intCard, Priority.ALWAYS);
        HBox.setHgrow(platiLunaCard, Priority.ALWAYS);
        topRow.setAlignment(Pos.TOP_LEFT);

        VBox platesteCard = wrapCard(buildInregistrarePlataForm(), "Înregistrare plată");
        VBox restanteCard = wrapCard(buildRestanteForm(), "Restanțe & clasare");
        HBox middleRow = new HBox(12, platesteCard, restanteCard);
        HBox.setHgrow(platesteCard, Priority.ALWAYS);
        HBox.setHgrow(restanteCard, Priority.ALWAYS);
        middleRow.setAlignment(Pos.TOP_LEFT);

        VBox platiCard = wrapTable(platiTable, "Plăți curente");
        VBox raportCard = wrapTextArea(raportArea, "Raport / Mesaje");
        VBox exportCard = wrapCard(buildExportForm(), "Export CSV");
        VBox bottomRow = new VBox(8, platiCard, exportCard, raportCard);
        VBox.setVgrow(platiCard, Priority.ALWAYS);
        VBox.setVgrow(exportCard, Priority.NEVER);
        VBox.setVgrow(raportCard, Priority.ALWAYS);

        content.getChildren().addAll(title, topRow, middleRow, bottomRow);

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        Tab tab = new Tab("Întreținere");
        tab.setContent(sp);
        return tab;
    }

    private Tab createTabAudit() {
        VBox content = new VBox(12);
        content.setPadding(new Insets(12));
        Label title = sectionLabel("Audit / Istoric operații");

        TextArea auditArea = new TextArea();
        auditArea.setEditable(false);
        auditArea.setWrapText(true);

        Button reload = new Button("Reîncarcă audit.log");
        reload.setOnAction(e -> auditArea.setText(audit.readAll()));

        Button openFile = new Button("Deschide audit.log în editor implicit");
        openFile.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().open(java.nio.file.Path.of("data", "audit.log").toFile());
            } catch (Exception ex) {
                auditArea.setText("Nu am putut deschide audit.log: " + ex.getMessage());
            }
        });

        HBox buttons = new HBox(8, reload, openFile);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(auditArea, Priority.ALWAYS);
        auditArea.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(auditArea, Priority.ALWAYS);
        content.getChildren().addAll(title, buttons, wrapTextArea(auditArea, "Conținut audit.log"));

        // încarcă inițial
        auditArea.setText(audit.readAll());

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);

        Tab tab = new Tab("Audit");
        tab.setContent(sp);
        return tab;
    }

    private GridPane buildExportForm() {
        GridPane grid = formGrid();

        CheckBox cbApart = new CheckBox("Apartamente");
        cbApart.setSelected(true);
        CheckBox cbLoc = new CheckBox("Locatari");
        cbLoc.setSelected(true);
        CheckBox cbPlati = new CheckBox("Plăți curente");
        cbPlati.setSelected(true);

        Button exportBtn = new Button("Exportă CSV");
        exportBtn.setOnAction(e -> {
            if (!cbApart.isSelected() && !cbLoc.isSelected() && !cbPlati.isSelected()) {
                warn("Selectați cel puțin un tabel de exportat.");
                return;
            }
            Window win = root.getScene() != null ? root.getScene().getWindow() : null;
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Alege folderul unde să creez exportul");
            File baseDir = chooser.showDialog(win);
            if (baseDir == null) {
                return;
            }

            Path exportDir = baseDir.toPath().resolve("hoa-export-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
            try {
                Files.createDirectories(exportDir);
            } catch (IOException ex) {
                warn("Nu pot crea folderul de export: " + ex.getMessage());
                return;
            }

            StringBuilder msg = new StringBuilder("Export efectuat în ").append(exportDir);
            try {
                if (cbApart.isSelected()) {
                    exportApartamente(exportDir.resolve("apartamente.csv"));
                    msg.append("\n- apartamente.csv");
                }
                if (cbLoc.isSelected()) {
                    exportLocatari(exportDir.resolve("locatari.csv"));
                    msg.append("\n- locatari.csv");
                }
                if (cbPlati.isSelected()) {
                    exportPlati(exportDir.resolve("plati.csv"));
                    msg.append("\n- plati.csv");
                }
                info("Export reușit.\n" + msg);
                audit.log("Export CSV la " + exportDir);
            } catch (IOException ex) {
                warn("Eroare la export: " + ex.getMessage());
            }
        });

        HBox checkRow = new HBox(8, cbApart, cbLoc, cbPlati);
        checkRow.setAlignment(Pos.CENTER_LEFT);
        addRowRegion(grid, 0, "Tabele", checkRow);
        addButton(grid, 1, exportBtn);
        return grid;
    }

    // ----- Form builders -----
    private GridPane buildAddApartamentForm() {
        GridPane grid = formGrid();
        TextField bloc = new TextField();
        TextField scara = new TextField();
        TextField numar = new TextField();
        TextField suprafata = new TextField();

        addRow(grid, 0, "Bloc", bloc);
        addRow(grid, 1, "Scara", scara);
        addRow(grid, 2, "Număr ap.", numar);
        addRow(grid, 3, "Suprafață (ex: 55.5)", suprafata);

        Button add = new Button("Adaugă apartament");
        add.setOnAction(e -> {
            String b = bloc.getText().trim();
            int s = parseInt(scara.getText());
            int n = parseInt(numar.getText());
            double sup = parseDouble(suprafata.getText());
            if (b.isEmpty() || s <= 0 || n <= 0 || sup <= 0) {
                warn("Date invalide pentru apartament.");
                return;
            }
            service.adaugaApartament(b, s, n, sup);
            info("Apartament adăugat.");
            audit.log("Apartament adăugat: bloc=" + b + ", scara=" + s + ", număr=" + n + ", suprafață=" + sup);
            refreshApartamente();
            bloc.clear(); scara.clear(); numar.clear(); suprafata.clear();
        });
        addButton(grid, 4, add);
        return grid;
    }

    private GridPane buildAddLocatarForm() {
        GridPane grid = formGrid();
        TextField idAp = new TextField();
        TextField nume = new TextField();
        TextField prenume = new TextField();

        addRow(grid, 0, "ID apartament", idAp);
        addRow(grid, 1, "Nume", nume);
        addRow(grid, 2, "Prenume", prenume);

        Button add = new Button("Adaugă locatar");
        add.setOnAction(e -> {
            int id = parseInt(idAp.getText());
            String n = nume.getText().trim();
            String p = prenume.getText().trim();
            if (id <= 0 || n.isEmpty() || p.isEmpty()) {
                warn("Completați corect toate câmpurile.");
                return;
            }
            service.adaugaLocatar(id, n, p);
            info("Locatar adăugat (verificați consola dacă ID-ul nu există).");
            audit.log("Locatar adăugat: idAp=" + id + ", nume=" + n + ", prenume=" + p);
            refreshLocatari();
        });
        addButton(grid, 3, add);
        return grid;
    }

    private VBox buildSearchFilterPanel() {
        VBox box = new VBox(8);
        box.getChildren().add(wrapCard(buildCautaLocatarForm(), "Caută locatar după nume"));
        box.getChildren().add(wrapCard(buildApartamentePersForm(), "Apartamente cu > X persoane"));
        box.getChildren().add(wrapCard(buildToateApartForm(), "Afișează toate apartamentele"));
        return box;
    }

    private GridPane buildCautaLocatarForm() {
        GridPane grid = formGrid();
        TextField nume = new TextField();
        Button cauta = new Button("Caută");
        cauta.setOnAction(e -> {
            String n = nume.getText().trim();
            if (n.isEmpty()) {
                warn("Introduceți numele căutat.");
                return;
            }
            List<Locatar> rezultate = service.cautaLocatarDupaNume(n);
            if (rezultate.isEmpty()) {
                locatariTable.setItems(FXCollections.emptyObservableList());
                raportArea.setText("Niciun locatar găsit cu numele '" + n + "'.");
            } else {
                locatariTable.setItems(FXCollections.observableArrayList(rezultate));
                raportArea.setText("Rezultate pentru numele '" + n + "'.");
            }
        });
        addRow(grid, 0, "Nume", nume);
        addButton(grid, 1, cauta);
        return grid;
    }

    private GridPane buildApartamentePersForm() {
        GridPane grid = formGrid();
        TextField limita = new TextField("0");
        Button afis = new Button("Afișează");
        afis.setOnAction(e -> {
            int lim = parseInt(limita.getText());
            if (lim < 0) {
                warn("Introduceți un număr valid.");
                return;
            }
            List<Apartament> rez = service.getApartamenteCuPesteXPersoane(lim);
            apartamenteTable.setItems(FXCollections.observableArrayList(rez));
            raportArea.setText(rez.isEmpty()
                    ? "Niciun apartament cu mai mult de " + lim + " persoane."
                    : "Filtru: apartamente cu mai mult de " + lim + " persoane.");
        });
        addRow(grid, 0, "Număr persoane", limita);
        addButton(grid, 1, afis);
        return grid;
    }

    private GridPane buildToateApartForm() {
        GridPane grid = formGrid();
        Button toate = new Button("Afișează toate");
        toate.setOnAction(e -> refreshApartamente());
        addButton(grid, 0, toate);
        return grid;
    }

    private GridPane buildIntretinereForm() {
        GridPane grid = formGrid();
        TextField luna = new TextField("11");
        TextField an = new TextField("2025");
        Button gen = new Button("Generează întreținerea");
        gen.setOnAction(e -> {
            int l = parseInt(luna.getText());
            int a = parseInt(an.getText());
            if (!validLunaAn(l, a)) {
                warn("Lună/An invalide.");
                return;
            }
            boolean ok = service.genereazaTabelIntretinere(l, a);
            if (ok) {
                info("Întreținerea a fost generată.");
                audit.log("Întreținere generată pentru " + l + "/" + a);
            } else {
                warn("Nu s-a putut genera (lipsă factură sau deja generată).");
            }
            refreshPlati(l, a);
        });
        addRow(grid, 0, "Lună", luna);
        addRow(grid, 1, "An", an);
        addButton(grid, 2, gen);
        return grid;
    }

    private GridPane buildPlatiLunaForm() {
        GridPane grid = formGrid();
        TextField luna = new TextField("11");
        TextField an = new TextField("2025");
        Button afis = new Button("Afișează plățile");
        afis.setOnAction(e -> {
            int l = parseInt(luna.getText());
            int a = parseInt(an.getText());
            refreshPlati(l, a);
        });
        addRow(grid, 0, "Lună", luna);
        addRow(grid, 1, "An", an);
        addButton(grid, 2, afis);
        return grid;
    }

    private GridPane buildInregistrarePlataForm() {
        GridPane grid = formGrid();
        TextField idPlata = new TextField();
        TextField suma = new TextField();
        Button plateste = new Button("Înregistrează plată");
        plateste.setOnAction(e -> {
            int id = parseInt(idPlata.getText());
            double s = parseDouble(suma.getText());
            if (id <= 0 || s <= 0) {
                warn("ID sau sumă invalidă.");
                return;
            }
            boolean ok = service.inregistreazaPlata(id, s);
            if (!ok) warn("Plata nu a putut fi înregistrată (ID inexistent).");
            else {
                info("Plata înregistrată.");
                audit.log("Plată înregistrată: idPlată=" + id + ", sumă=" + s);
            }
            refreshCurrentPlati();
        });
        addRow(grid, 0, "ID plată", idPlata);
        addRow(grid, 1, "Sumă", suma);
        addButton(grid, 2, plateste);
        return grid;
    }

    private GridPane buildRestanteForm() {
        GridPane grid = formGrid();
        TextField prag = new TextField("0");
        Button restantieri = new Button("Afișează restanțieri");
        restantieri.setOnAction(e -> {
            double p = parseDouble(prag.getText());
            List<Apartament> rez = service.getApartamenteCuRestantaPeste(p);
            if (rez.isEmpty()) {
                raportArea.setText("Niciun apartament cu restanță > " + p);
            } else {
                raportArea.setText("Restanțieri (> " + p + "):\n" +
                        rez.stream().map(Apartament::toString).collect(Collectors.joining("\n")));
            }
        });

        Button clasare = new Button("Clasare buni/răi platnici");
        clasare.setOnAction(e -> {
            Map<Boolean, List<Apartament>> map = service.clasarePlatnici();
            List<Apartament> rai = map.get(true);
            List<Apartament> buni = map.get(false);
            String text = "Răi platnici:\n" +
                    (rai.isEmpty() ? " - nimeni" : rai.stream().map(Apartament::toString).collect(Collectors.joining("\n"))) +
                    "\n\nBuni platnici:\n" +
                    (buni.isEmpty() ? " - nimeni" : buni.stream().map(Apartament::toString).collect(Collectors.joining("\n")));
            raportArea.setText(text);
        });

        addRow(grid, 0, "Prag restanță", prag);
        addButton(grid, 1, restantieri);
        addButton(grid, 2, clasare);
        return grid;
    }

    // ----- Export CSV -----
    private static final String CSV_SEP = ";"; // separator compatibil Excel RO

    private void exportApartamente(Path target) throws IOException {
        List<Apartament> data = apartamenteTable.getItems();
        String header = String.join(CSV_SEP, "id", "bloc", "scara", "numar", "suprafata");
        String body = data.stream()
                .map(a -> String.join(CSV_SEP,
                        String.valueOf(a.getId()),
                        csvText(a.getBloc()),
                        String.valueOf(a.getScara()),
                        String.valueOf(a.getNumar()),
                        String.valueOf(a.getSuprafata())))
                .collect(Collectors.joining(lineSep()));
        writeCsv(target, header, body);
    }

    private void exportLocatari(Path target) throws IOException {
        List<Locatar> data = locatariTable.getItems();
        String header = String.join(CSV_SEP, "id", "idApartament", "nume", "prenume");
        String body = data.stream()
                .map(l -> String.join(CSV_SEP,
                        String.valueOf(l.getId()),
                        String.valueOf(l.getIdApartament()),
                        csvText(l.getNume()),
                        csvText(l.getPrenume())))
                .collect(Collectors.joining(lineSep()));
        writeCsv(target, header, body);
    }

    private void exportPlati(Path target) throws IOException {
        List<PlataApartament> data = platiTable.getItems();
        String header = String.join(CSV_SEP, "id", "idApartament", "luna", "an", "totalDePlata", "sumaPlatita", "restanta");
        String body = data.stream()
                .map(p -> String.join(CSV_SEP,
                        String.valueOf(p.getId()),
                        String.valueOf(p.getIdApartament()),
                        String.valueOf(p.getLuna()),
                        String.valueOf(p.getAn()),
                        String.valueOf(p.getTotalDePlata()),
                        String.valueOf(p.getSumaPlatita()),
                        String.valueOf(p.getRestanta())))
                .collect(Collectors.joining(lineSep()));
        writeCsv(target, header, body);
    }

    private void writeCsv(Path target, String header, String body) throws IOException {
        String ls = lineSep();
        String content = header + (body.isEmpty() ? "" : ls + body) + ls;
        Files.writeString(target, content, StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String csvText(String text) {
        if (text == null) {
            return "\"\"";
        }
        String escaped = text.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private String lineSep() {
        return System.lineSeparator();
    }
    // ----- Helpers -----
    private void configureTables() {
        // Apartamente
        TableColumn<Apartament, Integer> apId = new TableColumn<>("ID");
        apId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        TableColumn<Apartament, String> apBloc = new TableColumn<>("Bloc");
        apBloc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBloc()));
        TableColumn<Apartament, Integer> apScara = new TableColumn<>("Scara");
        apScara.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getScara()).asObject());
        TableColumn<Apartament, Integer> apNumar = new TableColumn<>("Număr");
        apNumar.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumar()).asObject());
        TableColumn<Apartament, Double> apSup = new TableColumn<>("Suprafață");
        apSup.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSuprafata()).asObject());
        apartamenteTable.getColumns().setAll(java.util.List.of(apId, apBloc, apScara, apNumar, apSup));
        apartamenteTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Locatari
        TableColumn<Locatar, Integer> locId = new TableColumn<>("ID");
        locId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        TableColumn<Locatar, Integer> locAp = new TableColumn<>("Ap.");
        locAp.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdApartament()).asObject());
        TableColumn<Locatar, String> locNume = new TableColumn<>("Nume");
        locNume.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNume()));
        TableColumn<Locatar, String> locPrenume = new TableColumn<>("Prenume");
        locPrenume.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPrenume()));
        locatariTable.getColumns().setAll(java.util.List.of(locId, locAp, locNume, locPrenume));
        locatariTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Plăți
        TableColumn<PlataApartament, Integer> pId = new TableColumn<>("ID");
        pId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        TableColumn<PlataApartament, Integer> pAp = new TableColumn<>("Ap.");
        pAp.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdApartament()).asObject());
        TableColumn<PlataApartament, Integer> pLuna = new TableColumn<>("Lună");
        pLuna.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getLuna()).asObject());
        TableColumn<PlataApartament, Integer> pAn = new TableColumn<>("An");
        pAn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getAn()).asObject());
        TableColumn<PlataApartament, Double> pTotal = new TableColumn<>("Total");
        pTotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTotalDePlata()).asObject());
        TableColumn<PlataApartament, Double> pPlatita = new TableColumn<>("Plătit");
        pPlatita.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSumaPlatita()).asObject());
        TableColumn<PlataApartament, Double> pRest = new TableColumn<>("Restanță");
        pRest.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getRestanta()).asObject());
        platiTable.getColumns().setAll(java.util.List.of(pId, pAp, pLuna, pAn, pTotal, pPlatita, pRest));
        platiTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void refreshApartamente() {
        apartamenteTable.setItems(FXCollections.observableArrayList(service.getApartamente()));
        raportArea.setText("Toate apartamentele încărcate.");
    }

    private void refreshLocatari() {
        locatariTable.setItems(FXCollections.observableArrayList(service.getLocatariSortatiDupaNume()));
    }

    private void refreshPlati(int luna, int an) {
        List<PlataApartament> plati = service.getPlatiPeLuna(luna, an);
        platiTable.setItems(FXCollections.observableArrayList(plati));
        lastLuna = luna; lastAn = an;
        raportArea.setText(plati.isEmpty()
                ? "Nu există plăți generate pentru " + luna + "/" + an + "."
                : "Plăți pentru " + luna + "/" + an + " afișate.");
    }

    private void refreshCurrentPlati() {
        if (lastLuna > 0 && lastAn > 0) {
            refreshPlati(lastLuna, lastAn);
        }
    }

    private void refreshRaport(String text) {
        raportArea.setText(text);
    }

    private GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(40);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(60);
        grid.getColumnConstraints().addAll(c1, c2);
        return grid;
    }

    private void addRow(GridPane grid, int row, String label, Control field) {
        grid.add(new Label(label + ":"), 0, row);
        grid.add(field, 1, row);
    }

    private void addRowRegion(GridPane grid, int row, String label, Region field) {
        grid.add(new Label(label + ":"), 0, row);
        grid.add(field, 1, row);
    }

    private void addButton(GridPane grid, int row, Button button) {
        HBox box = new HBox(button);
        box.setAlignment(Pos.CENTER_RIGHT);
        grid.add(box, 1, row);
    }

    private VBox wrapCard(Region node, String title) {
        Label l = new Label(title);
        l.setFont(Font.font(l.getFont().getFamily(), 14));
        VBox box = new VBox(6, l, node);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #dcdcdc; -fx-border-radius: 8; -fx-background-radius: 8;");
        box.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(node, Priority.ALWAYS);
        return box;
    }

    private VBox wrapTable(TableView<?> table, String title) {
        table.setPrefHeight(300);
        VBox box = wrapCard(table, title);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private VBox wrapTextArea(TextArea area, String title) {
        area.setEditable(false);
        area.setWrapText(true);
        VBox box = wrapCard(area, title);
        VBox.setVgrow(area, Priority.ALWAYS);
        VBox.setVgrow(box, Priority.ALWAYS);
        area.setMaxHeight(Double.MAX_VALUE);
        box.setMaxHeight(Double.MAX_VALUE);

        return box;
    }


    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font(l.getFont().getFamily(), 18));
        return l;
    }

    private boolean validLunaAn(int luna, int an) {
        return luna >= 1 && luna <= 12 && an >= 2000;
    }

    private int parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private double parseDouble(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void warn(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    private void info(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}

