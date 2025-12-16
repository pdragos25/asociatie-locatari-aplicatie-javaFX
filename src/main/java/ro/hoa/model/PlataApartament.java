// model/PlataApartament.java
package ro.hoa.model;

public class PlataApartament {
    private int id; // ID unic al plății
    private int idApartament;
    private int luna;
    private int an;
    private double totalDePlata;
    private double sumaPlatita;

    public PlataApartament() {}

    public PlataApartament(int id, int idApartament, int luna, int an, double totalDePlata) {
        this.id = id;
        this.idApartament = idApartament;
        this.luna = luna;
        this.an = an;
        this.totalDePlata = totalDePlata;
        this.sumaPlatita = 0.0;
    }

    // return Suma rămasă de plată
    public double getRestanta() {
        return totalDePlata - sumaPlatita;
    }

    // return true dacă mai are de plată
    public boolean esteRestantier() {
        return getRestanta() > 0.01;
    }

    // Înregistrează o plată
    public void platesteSuma(double suma) {
        this.sumaPlatita += suma;
    }

    // Gettere si settere
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdApartament() { return idApartament; }
    public void setIdApartament(int idApartament) { this.idApartament = idApartament; }
    public int getLuna() { return luna; }
    public void setLuna(int luna) { this.luna = luna; }
    public int getAn() { return an; }
    public void setAn(int an) { this.an = an; }
    public double getTotalDePlata() { return totalDePlata; }
    public void setTotalDePlata(double totalDePlata) { this.totalDePlata = totalDePlata; }
    public double getSumaPlatita() { return sumaPlatita; }
    public void setSumaPlatita(double sumaPlatita) { this.sumaPlatita = sumaPlatita; }
}