package fr.upjv.miage2025.carnetdevoyage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fr.upjv.miage2025.carnetdevoyage.database.VoyageDatabaseHelper;
import fr.upjv.miage2025.carnetdevoyage.model.PointGps;
import fr.upjv.miage2025.carnetdevoyage.model.Voyage;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler = new Handler(Looper.getMainLooper());

    private void startGpsTracking(int voyageId, int periodeSecondes) { //WALID
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                VoyageDatabaseHelper db = new VoyageDatabaseHelper(getApplicationContext());
                Voyage v = null;

                for (Voyage vv : db.getAllVoyages()) {
                    if (vv.getId() == voyageId) {
                        v = vv;
                        break;
                    }
                }

                if (v == null) {
                    Log.d("GPS_AUTO", "Voyage introuvable, arrêt du tracking.");
                    return;
                }

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                    Date now = new Date();
                    Date dateDepart = sdf.parse(v.getDateDepart());
                    Date dateRetour = sdf.parse(v.getDateRetour());

                    if (!v.isSuiviActif() || now.before(dateDepart) || now.after(dateRetour)) {
                        Log.d("GPS_AUTO", "Suivi inactif ou hors période pour voyage " + voyageId + ". Arrêt.");
                        handler.postDelayed(this, periodeSecondes * 1000L);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                // Enregistrement de la position
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) return;

                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        String dateHeure = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                                .format(new Date());

                        db.ajouterPointGps(lat, lon, dateHeure, voyageId);
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("points_gps");
                        String pointId = ref.push().getKey();

                        PointGps point = new PointGps(lat, lon, dateHeure);
                        ref.child(String.valueOf(voyageId)).child(pointId).setValue(point);

                        Log.d("GPS_AUTO", "Position enregistrée pour voyage " + voyageId);
                    }
                });

                // Replanification
                handler.postDelayed(this, periodeSecondes * 1000L);
            }
        }, 0);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        FirebaseApp.initializeApp(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        VoyageDatabaseHelper db = new VoyageDatabaseHelper(this);
        List<Voyage> voyages = db.getAllVoyages();

        for (Voyage v : voyages) {//Walid
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                Date dateDepart = sdf.parse(v.getDateDepart());
                Date dateRetour = sdf.parse(v.getDateRetour());
                Date now = new Date();

                boolean isActive = now.after(dateDepart) && now.before(dateRetour);
                if (isActive && v.isSuiviActif()) {
                    startGpsTracking(v.getId(), v.getPeriode());
                    Log.d("GPS_AUTO", "Suivi démarré pour voyage " + v.getId());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // affichage des points pour test
        List<PointGps> points = db.getPointsGpsForVoyage(1);
        for (PointGps p : points) {
            Log.d("GPS_POINTS", p.getDateHeure() + " → " + p.getLatitude() + ", " + p.getLongitude());
        }

        Button btnNouveau = findViewById(R.id.btn_nouveau_voyage);
        btnNouveau.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreerVoyageActivity.class);
            startActivity(intent);
        });
    }
}
