package fr.upjv.miage2025.carnetdevoyage;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


import androidx.appcompat.app.AppCompatActivity;


import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import fr.upjv.miage2025.carnetdevoyage.database.VoyageDatabaseHelper;
import fr.upjv.miage2025.carnetdevoyage.model.PointGps;
import fr.upjv.miage2025.carnetdevoyage.model.Voyage;

public class DetailVoyageActivity extends AppCompatActivity {

    private Voyage voyage;
    private VoyageDatabaseHelper db;
    private int voyageId;

    private FusedLocationProviderClient fusedLocationClient;



    private void envoyerFichierParEmail(File file, String format) {
        String mimeType = format.equals("GPX") ? "application/gpx+xml" : "application/vnd.google-earth.kml+xml";

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType(mimeType);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Fichier " + format + " - Mon Voyage");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Voici les points GPS de mon voyage au format " + format + ".");
        emailIntent.putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
        ));
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(emailIntent, "Envoyer par email"));
    }


    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, y, m, d) -> target.setText(d + "/" + (m + 1) + "/" + y),
                year, month, day);

        datePickerDialog.show();
    }

    private void exporterPoints(String format) {//Walid
        try {
            List<PointGps> points = db.getPointsGpsForVoyage(voyageId);
            if (points.isEmpty()) {
                Toast.makeText(this, "Aucun point GPS à exporter", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuilder content = new StringBuilder();
            String fileName;

            if (format.equals("GPX")) {
                content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<gpx version=\"1.1\" creator=\"MapMyTrip\">\n");
                for (PointGps p : points) {
                    content.append("<wpt lat=\"").append(p.getLatitude()).append("\" lon=\"").append(p.getLongitude()).append("\">")
                            .append("<time>").append(p.getDateHeure()).append("</time>")
                            .append("</wpt>\n");
                }
                content.append("</gpx>");
                fileName = "points_voyage_" + voyageId + ".gpx";
            } else { // KML
                content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                content.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n<Document>\n");
                for (PointGps p : points) {
                    content.append("<Placemark>\n")
                            .append("<name>").append(p.getDateHeure()).append("</name>\n")
                            .append("<Point><coordinates>")
                            .append(p.getLongitude()).append(",").append(p.getLatitude()).append("</coordinates></Point>\n")
                            .append("</Placemark>\n");
                }
                content.append("</Document>\n</kml>");
                fileName = "points_voyage_" + voyageId + ".kml";
            }

            File downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (downloadsDir != null && !downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            File file = new File(downloadsDir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.toString().getBytes());
            fos.close();

            envoyerFichierParEmail(file, format);
            Toast.makeText(this, "Fichier " + format + " exporté dans Téléchargements", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de l'export", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_voyage);
        EditText etDestination = findViewById(R.id.et_destination);
        EditText etDateDepart = findViewById(R.id.et_date_depart);
        EditText etDateRetour = findViewById(R.id.et_date_retour);
        Button btnModifier = findViewById(R.id.btn_modifier_voyage);

        etDateDepart.setOnClickListener(v -> showDatePicker(etDateDepart));
        etDateRetour.setOnClickListener(v -> showDatePicker(etDateRetour));

        TextView titre = findViewById(R.id.header_title);
        titre.setText("Détails du Voyage");
        HeaderUtils.setupHeaderMenu(this);

        db = new VoyageDatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        voyageId = getIntent().getIntExtra("voyageId", -1);
        List<Voyage> allVoyages = db.getAllVoyages();
        for (Voyage v : allVoyages) {
            if (v.getId() == voyageId) {
                voyage = v;
                break;
            }
        }


        if (voyage == null) {
            Toast.makeText(this, "Voyage introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Affichage
        etDestination.setText(voyage.getDestination());
        etDateDepart.setText(voyage.getDateDepart());
        etDateRetour.setText(voyage.getDateRetour());


        Button btnToggleGps = findViewById(R.id.btn_toggle_gps);
        Button btnSavePosition = findViewById(R.id.btn_save_position);
        SeekBar seekBar = findViewById(R.id.slider_periodicite); //Walid
        TextView labelPeriodicite = findViewById(R.id.label_periodicite);

        // Init état suivi
        btnToggleGps.setText(voyage.isSuiviActif() ? "Désactiver l’enregistrement GPS" : "Activer l’enregistrement GPS");

        btnToggleGps.setOnClickListener(v -> {//Walid
            boolean actif = !voyage.isSuiviActif();
            voyage.setSuiviActif(actif);
            db.updateSuiviEtPeriode(voyageId, actif, voyage.getPeriode());
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("voyages")
                    .document(String.valueOf(voyageId))
                    .update("suiviActif", actif, "periode", voyage.getPeriode())
                    .addOnSuccessListener(unused -> Log.d("FIRESTORE", "Suivi/Periode mis à jour"))
                    .addOnFailureListener(e -> Log.e("FIRESTORE", "Erreur MAJ Suivi/Periode", e));

            btnToggleGps.setText(actif ? "Désactiver l’enregistrement GPS" : "Activer l’enregistrement GPS");
            Toast.makeText(this, actif ? "Suivi activé" : "Suivi désactivé", Toast.LENGTH_SHORT).show();
        });

        btnSavePosition.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();
                            String dateHeure = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                                    .format(new Date());

                            // Enregistrement local
                            db.ajouterPointGps(lat, lon, dateHeure, voyageId);

                            // Envoi vers Realtime Database
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("points_gps");
                            String pointId = ref.push().getKey(); // ID auto
                            PointGps point = new PointGps(lat, lon, dateHeure);
                            ref.child(String.valueOf(voyageId)).child(pointId).setValue(point);

                            Toast.makeText(this, "Position enregistrée : " + lat + ", " + lon, Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(this, "Position non disponible", Toast.LENGTH_SHORT).show();
                        }
                    });

        });

        btnModifier.setOnClickListener(v -> {
            Log.d("DEBUG", "Bouton MODIFIER cliqué");
            String nouvelleDestination = etDestination.getText().toString().trim();
            String nouvelleDateDepart = etDateDepart.getText().toString().trim();
            String nouvelleDateRetour = etDateRetour.getText().toString().trim();

            Log.d("DEBUG", "Destination : " + nouvelleDestination);
            Log.d("DEBUG", "Départ : " + nouvelleDateDepart);
            Log.d("DEBUG", "Retour : " + nouvelleDateRetour);

            if (!nouvelleDestination.isEmpty() && !nouvelleDateDepart.isEmpty() && !nouvelleDateRetour.isEmpty()) {
                voyage.setDestination(nouvelleDestination);
                voyage.setDateDepart(nouvelleDateDepart);
                voyage.setDateRetour(nouvelleDateRetour);

                db.updateVoyageInfos(voyage);
                Toast.makeText(this, "Voyage mis à jour", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tous les champs doivent être remplis", Toast.LENGTH_SHORT).show();
            }

            int rowsAffected = db.updateVoyageInfos(voyage);
            if (rowsAffected > 0) {
                // Mise à jour Firestore
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("voyages")
                        .document(String.valueOf(voyage.getId()))
                        .set(voyage)
                        .addOnSuccessListener(aVoid -> Log.d("FIRESTORE", "Voyage mis à jour dans Firestore"))
                        .addOnFailureListener(e -> Log.e("FIRESTORE", "Erreur MAJ Firestore", e));

                Toast.makeText(this, "Voyage mis à jour", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Échec de la mise à jour", Toast.LENGTH_SHORT).show();
            }


        });



        int currentPeriodeMin = voyage.getPeriode() / 60;
        seekBar.setProgress(currentPeriodeMin); //Walid
        labelPeriodicite.setText("Périodicité d’enregistrement : " + currentPeriodeMin + " min");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // walid
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int valueMin = Math.max(progress, 1);
                labelPeriodicite.setText("Périodicité d’enregistrement : " + valueMin + " min");
                voyage.setPeriode(valueMin * 60);
                db.updateSuiviEtPeriode(voyageId, voyage.isSuiviActif(), voyage.getPeriode());
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("voyages")
                        .document(String.valueOf(voyageId))
                        .update("periode", voyage.getPeriode())
                        .addOnSuccessListener(unused -> Log.d("FIRESTORE", "Période mise à jour"))
                        .addOnFailureListener(e -> Log.e("FIRESTORE", "Erreur MAJ période", e));

            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}

        });

        findViewById(R.id.btn_export_gpx).setOnClickListener(v -> { //walid
            String[] formats = {"GPX", "KML"};

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Choisissez un format d'export")
                    .setSingleChoiceItems(formats, 0, null)
                    .setPositiveButton("OK", (dialog, which) -> {
                        android.app.AlertDialog alert = (android.app.AlertDialog) dialog;
                        int selectedPosition = alert.getListView().getCheckedItemPosition();

                        if (selectedPosition == 0) {
                            exporterPoints("GPX");
                        } else {
                            exporterPoints("KML");
                        }
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });


        findViewById(R.id.zone_map).setOnClickListener(v -> {
            List<PointGps> points = db.getPointsGpsForVoyage(voyageId);
            if (points == null || points.size() < 2) {
                Toast.makeText(this, "Au moins deux points sont nécessaires", Toast.LENGTH_SHORT).show();
                return;
            }

            PointGps origin = points.get(0);
            PointGps destination = points.get(points.size() - 1);

            // Générer les waypoints (entre origin et destination)
            StringBuilder waypoints = new StringBuilder();
            for (int i = 1; i < points.size() - 1 && i <= 8; i++) { // Max 8 waypoints (Google Maps API limitation)
                PointGps p = points.get(i);
                waypoints.append(p.getLatitude()).append(",").append(p.getLongitude());
                if (i < points.size() - 2 && i < 8) {
                    waypoints.append("|");
                }
            }

            String uri = "https://www.google.com/maps/dir/?api=1"
                    + "&origin=" + origin.getLatitude() + "," + origin.getLongitude()
                    + "&destination=" + destination.getLatitude() + "," + destination.getLongitude();

            if (waypoints.length() > 0) {
                uri += "&waypoints=" + waypoints;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri));
            startActivity(intent);
        });


    }
}
