package ro.hoa.service;

import ro.hoa.model.*;
import ro.hoa.repository.Storage;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AsociatieService {

    private final Storage storage;

    public AsociatieService(Storage storage) {
        this.storage = storage;
    }

    // Operații pe Apartamente
    public void adaugaApartament(String bloc, int scara, int numar, double suprafata) {
        int idNou = storage.getApartamente().stream()
                .mapToInt(Apartament::getId)
                .max()
                .orElse(0) + 1;

        Apartament apNou = new Apartament(idNou, bloc, scara, numar, suprafata);
        storage.getApartamente().add(apNou);
    }

    public List<Apartament> getApartamente() {
        return storage.getApartamente();
    }

    // Operații pe Locatari
    public void adaugaLocatar(int idApartament, String nume, String prenume) {
        boolean apartamentExista = storage.getApartamente().stream()
                .anyMatch(ap -> ap.getId() == idApartament);

        if (!apartamentExista) {
            System.err.println("Eroare: Apartamentul cu ID " + idApartament + " nu exista!");
            return;
        }

        int idNou = storage.getLocatari().stream()
                .mapToInt(Locatar::getId)
                .max()
                .orElse(0) + 1;

        Locatar locatarNou = new Locatar(idNou, idApartament, nume, prenume);
        storage.getLocatari().add(locatarNou);
    }

    public List<Locatar> getLocatariSortatiDupaNume() {
        return storage.getLocatari().stream()
                .sorted(Comparator.comparing(Locatar::getNume)
                        .thenComparing(Locatar::getPrenume))
                .collect(Collectors.toList());
    }

    public List<Locatar> cautaLocatarDupaNume(String numeCautat) {
        return storage.getLocatari().stream()
                .filter(locatar -> locatar.getNume().equalsIgnoreCase(numeCautat))
                .collect(Collectors.toList());
    }

    // Calculează numărul de persoane care locuiesc într-un apartament specific
    private long getNumarPersoaneApartament(int idApartament) {
        return storage.getLocatari().stream()
                .filter(l -> l.getIdApartament() == idApartament)
                .count();
    }

    // Calculează numărul total de persoane din TOATĂ asociația
    private long getTotalPersoaneAsociatie() {
        return storage.getLocatari().size();
    }

    // Calculează suprafața totală a asociatiei
    private double getTotalSuprafataAsociatie() {
        return storage.getApartamente().stream()
                .mapToDouble(Apartament::getSuprafata)
                .sum();
    }

    // Găsește un apartament după ID
    private Apartament getApartamentById(int id) {
        return storage.getApartamente().stream()
                .filter(a -> a.getId() == id)
                .findFirst()
                .orElse(null); // Atenție, poate fi null
    }

    // Funcție de test pentru a adăuga o factură
    public void creeazaFacturaTest(int luna, int an) {
        // Verifică dacă există deja o factură pentru luna/anul ăsta
        boolean exista = storage.getFacturi().stream()
                .anyMatch(f -> f.getLuna() == luna && f.getAn() == an);
        if (exista) {
            System.out.println("Serviciu: Factura pentru " + luna + "/" + an + " deja există.");
            return;
        }

        FacturaLunara facturaNoua = new FacturaLunara(luna, an);
        facturaNoua.adaugaCheltuiala("Apa Rece Totală", 5000.0, TipCheltuiala.PERSOANA);
        facturaNoua.adaugaCheltuiala("Încălzire Totală", 10000.0, TipCheltuiala.SUPRAFATA);
        facturaNoua.adaugaCheltuiala("Salariu Administrator", 1500.0, TipCheltuiala.APARTAMENT);

        storage.getFacturi().add(facturaNoua);
        System.out.println("Serviciu: Factura de test pentru " + luna + "/" + an + " a fost creată.");
    }

    // afișare apartamente în care locuiesc mai mult de X persoane
    public List<Apartament> getApartamenteCuPesteXPersoane(int numarMinimPersoane) {
        return storage.getApartamente().stream()
                .filter(apartament -> getNumarPersoaneApartament(apartament.getId()) > numarMinimPersoane)
                .collect(Collectors.toList());
    }

    // Generează plățile (datoriile) pentru fiecare apartament pe baza facturii lunare
    public boolean genereazaTabelIntretinere(int luna, int an) {
        // 1. Găsește factura pentru luna și anul specificat
        FacturaLunara factura = storage.getFacturi().stream()
                .filter(f -> f.getLuna() == luna && f.getAn() == an)
                .findFirst()
                .orElse(null);

        if (factura == null) {
            System.err.println("Serviciu: Nu s-a găsit nicio factură pentru " + luna + "/" + an + ". Generați o factură mai întâi.");
            return false; // Nu am putut genera
        }

        // 2. Verifică dacă nu am generat deja plățile pentru această lună
        boolean dejaGenerat = storage.getPlati().stream()
                .anyMatch(p -> p.getLuna() == luna && p.getAn() == an);
        if (dejaGenerat) {
            System.err.println("Serviciu: Plățile pentru " + luna + "/" + an + " au fost deja generate.");
            return false;
        }

        // 3. Calculează totalurile asociației
        long totalPersoane = getTotalPersoaneAsociatie();
        double totalSuprafata = getTotalSuprafataAsociatie();
        int totalApartamente = storage.getApartamente().size();

        if (totalPersoane == 0 || totalSuprafata == 0 || totalApartamente == 0) {
            System.err.println("Serviciu: Nu se poate împărți la 0 (nu există apartamente/persoane).");
            return false;
        }

        // 4. Iterează prin FIECARE apartament și calculează-i datoria
        for (Apartament ap : storage.getApartamente()) {
            double totalDatoratApartament = 0.0;
            long persoaneAp = getNumarPersoaneApartament(ap.getId());
            double suprafataAp = ap.getSuprafata();

            // 5. Iterează prin fiecare linie de pe factura totală
            for (FacturaLunara.CheltuialaLinie linie : factura.getLiniiCheltuiala()) {
                double valoareLinie = linie.getValoareTotala();
                double costPerApartamentPeLinie = 0.0;

                switch (linie.getModImpartire()) {
                    case PERSOANA:
                        costPerApartamentPeLinie = (valoareLinie / totalPersoane) * persoaneAp;
                        break;
                    case SUPRAFATA:
                        costPerApartamentPeLinie = (valoareLinie / totalSuprafata) * suprafataAp;
                        break;
                    case APARTAMENT:
                        costPerApartamentPeLinie = (valoareLinie / totalApartamente);
                        break;
                }
                totalDatoratApartament += costPerApartamentPeLinie;
            }

            // 6. Găsește un ID nou pentru plată
            int idPlataNou = storage.getPlati().stream().mapToInt(PlataApartament::getId).max().orElse(0) + 1;

            // 7. Creează obiectul PlataApartament (datoria) și salvează-l
            PlataApartament plataNoua = new PlataApartament(idPlataNou, ap.getId(), luna, an, totalDatoratApartament);
            storage.getPlati().add(plataNoua);
        }

        System.out.println("Serviciu: Plățile pentru " + luna + "/" + an + " au fost generate cu succes.");
        return true;
    }

    // Returnează plățile generate pentru o anumită lună
    public List<PlataApartament> getPlatiPeLuna(int luna, int an) {
        return storage.getPlati().stream()
                .filter(p -> p.getLuna() == luna && p.getAn() == an)
                .collect(Collectors.toList());
    }

    // Calculează restanța totală pentru un apartament
    private double getRestantaTotalaApartament(int idApartament) {
        return storage.getPlati().stream()
                .filter(p -> p.getIdApartament() == idApartament)
                .mapToDouble(PlataApartament::getRestanta)
                .sum();
    }

    // Afișare apartamente ce au restanță > sumă dată
    public List<Apartament> getApartamenteCuRestantaPeste(double suma) {
        return storage.getApartamente().stream()
                .filter(ap -> getRestantaTotalaApartament(ap.getId()) > suma)
                .collect(Collectors.toList());
    }

    // Clasarea locatarilor în răi platnici, respectiv buni platnici
    public Map<Boolean, List<Apartament>> clasarePlatnici() {
        // Partitionează lista de apartamente în două:
        // 'false' = Buni platnici (restanța <= 0.01)
        // 'true' = Răi platnici (restanța > 0.01)
        return storage.getApartamente().stream()
                .collect(Collectors.partitioningBy(
                        ap -> getRestantaTotalaApartament(ap.getId()) > 0.01
                ));
    }

    // Inregistrare plata
    public boolean inregistreazaPlata(int idPlata, double suma) {
        PlataApartament plata = storage.getPlati().stream()
                .filter(p -> p.getId() == idPlata)
                .findFirst()
                .orElse(null);

        if (plata == null) {
            System.err.println("Serviciu: Plata cu ID " + idPlata + " nu a fost găsită.");
            return false;
        }

        plata.platesteSuma(suma);
        System.out.println("Serviciu: Suma de " + suma + " a fost înregistrată pentru plata ID " + idPlata);
        System.out.println("Serviciu: Noul rest de plată: " + plata.getRestanta());
        return true;
    }
}