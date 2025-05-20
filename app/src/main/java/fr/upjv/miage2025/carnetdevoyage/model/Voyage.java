package fr.upjv.miage2025.carnetdevoyage.model;

public class Voyage {
    private int id;
    private String destination;
    private String dateDepart;
    private String dateRetour;

    public Voyage(String destination, String dateDepart, String dateRetour) {
        this.destination = destination;
        this.dateDepart = dateDepart;
        this.dateRetour = dateRetour;
    }

    public int getId() { return id; }
    public String getDestination() { return destination; }
    public String getDateDepart() { return dateDepart; }
    public String getDateRetour() { return dateRetour; }

    public void setId(int id) { this.id = id; }
}
