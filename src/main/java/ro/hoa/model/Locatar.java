package ro.hoa.model;

public class Locatar {
    private int id;
    private int idApartament;
    private String nume;
    private String prenume;

    // Constructor pentru biblioteca Jackson (JSON)
    public Locatar() {}

    public Locatar(int id, int idApartament, String nume, String prenume) {
        this.id = id;
        this.idApartament = idApartament;
        this.nume = nume;
        this.prenume = prenume;
    }

    //Gettere si Settere
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdApartament() { return idApartament; }
    public void setIdApartament(int idApartament) { this.idApartament = idApartament; }
    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }
    public String getPrenume() { return prenume; }
    public void setPrenume(String prenume) { this.prenume = prenume; }

    @Override
    public String toString() {
        return "Locatar{" +
                "id=" + id +
                ", idApartament=" + idApartament +
                ", nume='" + nume + '\'' +
                ", prenume='" + prenume + '\'' +
                '}';
    }
}