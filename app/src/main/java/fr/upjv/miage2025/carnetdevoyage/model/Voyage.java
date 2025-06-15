package fr.upjv.miage2025.carnetdevoyage.model;

public class Voyage {
    private int id;
    private String destination;
    private String dateDepart;
    private String dateRetour;

    private boolean suiviActif;
    private int periode;

    public Voyage() {
        // Constructeur vide
    }


    public Voyage(String destination, String dateDepart, String dateRetour) {
        this.destination = destination;
        this.dateDepart = dateDepart;
        this.dateRetour = dateRetour;
    }

    public int getId() { return id; }
    public String getDestination() { return destination; }
    public String getDateDepart() { return dateDepart; }
    public String getDateRetour() { return dateRetour; }
    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setDateDepart(String dateDepart) {
        this.dateDepart = dateDepart;
    }

    public void setDateRetour(String dateRetour) {
        this.dateRetour = dateRetour;
    }


    public void setId(int id) { this.id = id; }

    public boolean isSuiviActif() { return suiviActif; }
    public void setSuiviActif(boolean suiviActif) { this.suiviActif = suiviActif; }

    public int getPeriode() { return periode; }
    public void setPeriode(int periode) { this.periode = periode; }

}
