package fr.upjv.miage2025.carnetdevoyage;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import com.google.firebase.firestore.FirebaseFirestore;


import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.List;

import fr.upjv.miage2025.carnetdevoyage.database.VoyageDatabaseHelper;
import fr.upjv.miage2025.carnetdevoyage.model.Voyage;

public class CreerVoyageActivity extends AppCompatActivity {

    private EditText etNomVoyage, etDateDebut, etDateFin;
    private Button btnEnregistrer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creer_voyage);

        TextView titre = findViewById(R.id.header_title);
        titre.setText("Map My Trip");
        HeaderUtils.setupHeaderMenu(this);

        // Champs
        etNomVoyage = findViewById(R.id.et_nom_voyage);
        etDateDebut = findViewById(R.id.et_date_debut);
        etDateFin = findViewById(R.id.et_date_fin);
        btnEnregistrer = findViewById(R.id.btn_enregistrer);

        // Sélection de date
        etDateDebut.setOnClickListener(v -> showDatePicker(etDateDebut));
        etDateFin.setOnClickListener(v -> showDatePicker(etDateFin));

        // Enregistrement du voyage
        btnEnregistrer.setOnClickListener(v -> {
            String nom = etNomVoyage.getText().toString();
            String debut = etDateDebut.getText().toString();
            String fin = etDateFin.getText().toString();

            if (nom.isEmpty() || debut.isEmpty() || fin.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else {
                VoyageDatabaseHelper dbHelper = new VoyageDatabaseHelper(this);
                dbHelper.ajouterVoyage(nom, debut, fin);
                // Récupérer l’objet Voyage avec l'ID auto-incrémenté
                List<Voyage> voyages = dbHelper.getAllVoyages();
                Voyage dernierVoyage = voyages.get(voyages.size() - 1); // dernier ajouté

                // Envoi Firestore
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("voyages")
                        .document(String.valueOf(dernierVoyage.getId()))
                        .set(dernierVoyage)
                        .addOnSuccessListener(aVoid -> Log.d("FIRESTORE", "Voyage envoyé avec succès"))
                        .addOnFailureListener(e -> Log.e("FIRESTORE", "Erreur envoi", e));

                Toast.makeText(this, "Voyage enregistré", Toast.LENGTH_SHORT).show();

                // Réinitialisation des champs
                etNomVoyage.setText("");
                etDateDebut.setText("");
                etDateFin.setText("");

                // Redirection
                Intent intent = new Intent(CreerVoyageActivity.this, ListeVoyagesActivity.class);
                startActivity(intent);
                finish();
            }
        });



    }

    private void showDatePicker(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        int annee = calendar.get(Calendar.YEAR);
        int mois = calendar.get(Calendar.MONTH);
        int jour = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                    targetEditText.setText(date);
                }, annee, mois, jour);

        datePickerDialog.show();
    }
}
