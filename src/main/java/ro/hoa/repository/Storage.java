package ro.hoa.repository;

import ro.hoa.model.Apartament;
import ro.hoa.model.FacturaLunara;
import ro.hoa.model.Locatar;
import ro.hoa.model.PlataApartament;
import ro.hoa.model.User;

import java.util.ArrayList;
import java.util.List;

public class Storage {

    private List<Apartament> apartamente = new ArrayList<>();
    private List<Locatar> locatari = new ArrayList<>();
    private List<FacturaLunara> facturi = new ArrayList<>();
    private List<PlataApartament> plati = new ArrayList<>();
    private List<User> users = new ArrayList<>();

    // Settere si gettere
    public List<Apartament> getApartamente() {
        return apartamente;
    }
    public void setApartamente(List<Apartament> apartamente) {
        this.apartamente = apartamente;
    }

    public List<Locatar> getLocatari() {
        return locatari;
    }
    public void setLocatari(List<Locatar> locatari) {
        this.locatari = locatari;
    }

    public List<FacturaLunara> getFacturi() {
        return facturi;
    }
    public void setFacturi(List<FacturaLunara> facturi) {
        this.facturi = facturi;
    }

    public List<PlataApartament> getPlati() {
        return plati;
    }
    public void setPlati(List<PlataApartament> plati) {
        this.plati = plati;
    }

    public List<User> getUsers() {return users;}
    public void setUsers(List<User> users) {this.users = users;}
}