package fr.upjv.miage2025.carnetdevoyage.model;

public class PointGps {
    private double latitude;
    private double longitude;
    private String dateHeure;

    public PointGps(double latitude, double longitude, String dateHeure) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateHeure = dateHeure;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getDateHeure() { return dateHeure; }
}
