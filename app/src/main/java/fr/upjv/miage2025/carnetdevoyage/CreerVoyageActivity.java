package fr.upjv.miage2025.carnetdevoyage;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import fr.upjv.miage2025.carnetdevoyage.database.VoyageDatabaseHelper;
import fr.upjv.miage2025.carnetdevoyage.ListeVoyagesActivity;
import android.content.Intent;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class CreerVoyageActivity extends AppCompatActivity {

    private EditText etNomVoyage, etDateDebut, etDateFin;
    private Button btnEnregistrer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creer_voyage);
        View header = findViewById(R.id.header_title).getRootView(); // ou R.id.header si tu as mis un id sur le RelativeLayout

        ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });


        etNomVoyage = findViewById(R.id.et_nom_voyage);
        etDateDebut = findViewById(R.id.et_date_debut);
        etDateFin = findViewById(R.id.et_date_fin);
        btnEnregistrer = findViewById(R.id.btn_enregistrer);

        // Choix des dates
        etDateDebut.setOnClickListener(v -> showDatePicker(etDateDebut));
        etDateFin.setOnClickListener(v -> showDatePicker(etDateFin));

        btnEnregistrer.setOnClickListener(v -> {
            String nom = etNomVoyage.getText().toString();
            String debut = etDateDebut.getText().toString();
            String fin = etDateFin.getText().toString();

            if (nom.isEmpty() || debut.isEmpty() || fin.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else {
                // Enregistrement dans SQLite
                VoyageDatabaseHelper dbHelper = new VoyageDatabaseHelper(this);
                dbHelper.ajouterVoyage(nom, debut, fin);
                Toast.makeText(this, "Voyage enregistré en base locale", Toast.LENGTH_SHORT).show();

                //vider les champs
                etNomVoyage.setText("");
                etDateDebut.setText("");
                etDateFin.setText("");

                // Redirection vers la liste
                Intent intent = new Intent(CreerVoyageActivity.this, ListeVoyagesActivity.class);
                startActivity(intent);
                finish(); // Facultatif : empêche de revenir en arrière avec le bouton "back"
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
