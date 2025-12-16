// model/FacturaLunara.java
package ro.hoa.model;

import java.util.ArrayList;
import java.util.List;

public class FacturaLunara {
    private int luna;
    private int an;
    // O factură este o listă de cheltuieli
    private List<CheltuialaLinie> liniiCheltuiala;

    // Constructor gol pentru JSON
    public FacturaLunara() {
        this.liniiCheltuiala = new ArrayList<>();
    }

    public FacturaLunara(int luna, int an) {
        this.luna = luna;
        this.an = an;
        this.liniiCheltuiala = new ArrayList<>();
    }

    // Metodă ajutătoare pentru a adăuga o linie pe factură
    public void adaugaCheltuiala(String nume, double valoareTotala, TipCheltuiala modImpartire) {
        this.liniiCheltuiala.add(new CheltuialaLinie(nume, valoareTotala, modImpartire));
    }

    // Getteri si Setteri
    public int getLuna() { return luna; }
    public void setLuna(int luna) { this.luna = luna; }
    public int getAn() { return an; }
    public void setAn(int an) { this.an = an; }
    public List<CheltuialaLinie> getLiniiCheltuiala() { return liniiCheltuiala; }
    public void setLiniiCheltuiala(List<CheltuialaLinie> liniiCheltuiala) { this.liniiCheltuiala = liniiCheltuiala; }

    public static class CheltuialaLinie {
        private String nume; // ex: "Consum Apa Rece"
        private double valoareTotala; // ex: 5000 (RON)
        private TipCheltuiala modImpartire; // ex: PERSOANA

        public CheltuialaLinie() {} // Gol pt JSON

        public CheltuialaLinie(String nume, double valoareTotala, TipCheltuiala modImpartire) {
            this.nume = nume;
            this.valoareTotala = valoareTotala;
            this.modImpartire = modImpartire;
        }

        // Getteri si Setteri
        public String getNume() { return nume; }
        public void setNume(String nume) { this.nume = nume; }
        public double getValoareTotala() { return valoareTotala; }
        public void setValoareTotala(double valoareTotala) { this.valoareTotala = valoareTotala; }
        public TipCheltuiala getModImpartire() { return modImpartire; }
        public void setModImpartire(TipCheltuiala modImpartire) { this.modImpartire = modImpartire; }
    }
}