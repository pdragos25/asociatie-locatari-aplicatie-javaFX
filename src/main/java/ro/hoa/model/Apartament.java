package ro.hoa.model;

public class Apartament {
    private int id;
    private String bloc;
    private int scara;
    private int numar;
    private double suprafata;

    // Constructor pentru biblioteca Jackson (JSON)
    public Apartament() {}

    public Apartament(int id, String bloc, int scara, int numar, double suprafata) {
        this.id = id;
        this.bloc = bloc;
        this.scara = scara;
        this.numar = numar;
        this.suprafata = suprafata;
    }

    //Gettere si settere
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getBloc() { return bloc; }
    public void setBloc(String bloc) { this.bloc = bloc; }
    public int getScara() { return scara; }
    public void setScara(int scara) { this.scara = scara; }
    public int getNumar() { return numar; }
    public void setNumar(int numar) { this.numar = numar; }
    public double getSuprafata() { return suprafata; }
    public void setSuprafata(double suprafata) { this.suprafata = suprafata; }

    // Folosit pentru afișarea simplă în consolă
    @Override
    public String toString() {
        return "Apartament{" +
                "id=" + id +
                ", bloc='" + bloc + '\'' +
                ", scara=" + scara +
                ", numar=" + numar +
                ", suprafata=" + suprafata +
                '}';
    }
}